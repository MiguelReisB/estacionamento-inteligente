package service;

import dao.TicketDAO;
import dao.VagaDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import model.TipoVeiculo;
import model.Vaga;
import util.FabricaConexao;
import util.ValidadorPlaca;

/**
 * Entrada de veículo em uma única transação:
 *   1. trava uma vaga LIVRE do tipo do veículo (SELECT ... FOR UPDATE)
 *   2. cria o ticket ATIVO
 *   3. marca a vaga como OCUPADA
 *   4. COMMIT — se qualquer passo falhar, ROLLBACK (nada fica pela metade)
 */
public class EntradaVeiculoService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /** Resultado da tentativa de entrada. */
    public record ResultadoEntrada(boolean liberada, String vaga, int ticketId) {

        static ResultadoEntrada lotado() {
            return new ResultadoEntrada(false, null, 0);
        }
    }

    private final VagaDAO vagaDAO = new VagaDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    /**
     * @param placa  placa digitada (é normalizada: maiúsculas, sem hífen)
     * @param tipo   CARRO ou MOTO
     * @param cancela nome da cancela que pediu a entrada (aparece no log e no ticket)
     * @return liberada=true com a vaga ocupada, ou liberada=false se lotado
     * @throws OperacaoInvalidaException placa inválida, tipo não escolhido ou veículo já estacionado
     */
    public ResultadoEntrada processarEntrada(String placa, TipoVeiculo tipo, String cancela)
            throws OperacaoInvalidaException, SQLException {

        String placaNormalizada = ValidadorPlaca.normalizar(placa);

        if (tipo == null) {
            throw new OperacaoInvalidaException("Selecione o tipo de veículo (Carro ou Moto).");
        }
        if (!ValidadorPlaca.isValida(placaNormalizada)) {
            throw new OperacaoInvalidaException(
                    "Placa inválida. Use o formato antigo (ABC1234) ou Mercosul (ABC1D23).");
        }

        try (Connection conexao = FabricaConexao.getConexaoTransacional()) {
            try {
                if (ticketDAO.buscarAberto(conexao, placaNormalizada) != null) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException(
                            "O veículo " + placaNormalizada + " já está no estacionamento.");
                }

                Vaga vaga = vagaDAO.buscarLivreComLock(conexao, tipo);

                if (vaga == null) {
                    conexao.rollback();
                    log(cancela, "RECUSADA " + placaNormalizada + " — lotação esgotada para " + tipo);
                    return ResultadoEntrada.lotado();
                }

                int ticketId = ticketDAO.inserir(conexao, placaNormalizada, tipo, vaga.getId(),
                        LocalDateTime.now(), cancela);
                vagaDAO.atualizarStatus(conexao, vaga.getId(), Vaga.OCUPADA);

                conexao.commit();
                log(cancela, "LIBERADA " + placaNormalizada + " — vaga " + vaga.getIdentificacao()
                        + " (ticket " + ticketId + ")");
                return new ResultadoEntrada(true, vaga.getIdentificacao(), ticketId);

            } catch (SQLException e) {
                conexao.rollback();
                log(cancela, "ROLLBACK " + placaNormalizada + " — " + e.getMessage());

                // 1062 = UNIQUE violado: outra cancela registrou a mesma placa ao mesmo tempo
                if (e.getErrorCode() == 1062) {
                    throw new OperacaoInvalidaException(
                            "O veículo " + placaNormalizada + " já está no estacionamento.");
                }
                throw e;
            }
        }
    }

    private static void log(String cancela, String mensagem) {
        System.out.println("[" + LocalDateTime.now().format(HORA) + "] [" + cancela + "] " + mensagem);
    }
}
