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
 *
 * @author henri
 */
public class EntradaVeiculoService {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // liberada = false quer dizer que estava lotado
    public record ResultadoEntrada(boolean liberada, String vaga, int ticketId) {
    }

    private final VagaDAO vagaDAO = new VagaDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    // tudo numa transacao so: trava a vaga, cria o ticket e ocupa a vaga
    // se der erro no meio faz rollback e nada fica gravado
    public ResultadoEntrada processarEntrada(String placa, TipoVeiculo tipo, String cancela)
            throws OperacaoInvalidaException, SQLException {

        placa = ValidadorPlaca.normalizar(placa);

        if (tipo == null) {
            throw new OperacaoInvalidaException("Selecione o tipo de veículo (Carro ou Moto).");
        }
        if (!ValidadorPlaca.isValida(placa)) {
            throw new OperacaoInvalidaException("Placa inválida. Use o formato ABC1234 ou ABC1D23.");
        }

        try (Connection conexao = FabricaConexao.getConexaoTransacional()) {
            try {
                if (ticketDAO.buscarAberto(conexao, placa) != null) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("O veículo " + placa + " já está no estacionamento.");
                }

                Vaga vaga = vagaDAO.buscarLivreComLock(conexao, tipo);

                if (vaga == null) {
                    conexao.rollback();
                    log(cancela, "entrada recusada " + placa + ", sem vaga de " + tipo.getDescricao().toLowerCase());
                    return new ResultadoEntrada(false, null, 0);
                }

                int ticketId = ticketDAO.inserir(conexao, placa, tipo, vaga.getId(), LocalDateTime.now(), cancela);
                vagaDAO.atualizarStatus(conexao, vaga.getId(), Vaga.OCUPADA);

                conexao.commit();
                log(cancela, "entrada liberada " + placa + " na vaga " + vaga.getIdentificacao() + " (ticket " + ticketId + ")");
                return new ResultadoEntrada(true, vaga.getIdentificacao(), ticketId);

            } catch (SQLException e) {
                conexao.rollback();
                log(cancela, "ROLLBACK " + placa + ": " + e.getMessage());

                // 1062 = chave duplicada, a mesma placa entrou por outra cancela ao mesmo tempo
                if (e.getErrorCode() == 1062) {
                    throw new OperacaoInvalidaException("O veículo " + placa + " já está no estacionamento.");
                }
                throw e;
            }
        }
    }

    private void log(String cancela, String msg) {
        System.out.println("[" + LocalDateTime.now().format(FORMATO_HORA) + "] [" + cancela + "] " + msg);
    }
}
