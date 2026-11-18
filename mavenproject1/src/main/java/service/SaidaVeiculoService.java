package service;

import dao.TicketDAO;
import dao.VagaDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import model.StatusTicket;
import model.Ticket;
import model.Vaga;
import util.FabricaConexao;
import util.ValidadorPlaca;

/**
 * Pagamento e saída de veículos, respeitando a máquina de estados do ticket:
 *   ATIVO --pagar()--> PAGO --registrarSaida()--> ENCERRADO (+ vaga LIVRE)
 *
 * Cada operação roda em transação própria, com o ticket travado (FOR UPDATE)
 * para que duas cancelas não paguem/liberem o mesmo ticket ao mesmo tempo.
 */
public class SaidaVeiculoService {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final VagaDAO vagaDAO = new VagaDAO();

    /**
     * Consulta o ticket aberto da placa. Se ainda não foi pago, o campo
     * valor vem preenchido com o valor que seria cobrado agora (sem gravar).
     */
    public Ticket consultar(String placa) throws OperacaoInvalidaException, SQLException {
        String p = validar(placa);
        Ticket ticket = ticketDAO.buscarAbertoPorPlaca(p);

        if (ticket == null) {
            throw naoEncontrado(p);
        }
        if (ticket.getStatus() == StatusTicket.ATIVO) {
            ticket.setValor(CalculadoraTarifa.calcular(ticket.getDataEntrada(), LocalDateTime.now()));
        }
        return ticket;
    }

    /** ATIVO -> PAGO. Calcula e grava o valor. Retorna o ticket atualizado. */
    public Ticket pagar(String placa) throws OperacaoInvalidaException, SQLException {
        String p = validar(placa);

        try (Connection conexao = FabricaConexao.getConexaoTransacional()) {
            try {
                Ticket ticket = ticketDAO.buscarAbertoComLock(conexao, p);

                if (ticket == null) {
                    conexao.rollback();
                    throw naoEncontrado(p);
                }
                if (!ticket.getStatus().podeIrPara(StatusTicket.PAGO)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("O ticket do veículo " + p + " já está pago.");
                }

                LocalDateTime agora = LocalDateTime.now();
                BigDecimal valor = CalculadoraTarifa.calcular(ticket.getDataEntrada(), agora);

                if (!ticketDAO.registrarPagamento(conexao, ticket.getId(), valor, agora)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("Não foi possível registrar o pagamento. Tente novamente.");
                }

                conexao.commit();
                ticket.setStatus(StatusTicket.PAGO);
                ticket.setValor(valor);
                ticket.setDataPagamento(agora);
                return ticket;

            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    /** PAGO -> ENCERRADO e libera a vaga, na mesma transação. */
    public Ticket registrarSaida(String placa) throws OperacaoInvalidaException, SQLException {
        String p = validar(placa);

        try (Connection conexao = FabricaConexao.getConexaoTransacional()) {
            try {
                Ticket ticket = ticketDAO.buscarAbertoComLock(conexao, p);

                if (ticket == null) {
                    conexao.rollback();
                    throw naoEncontrado(p);
                }
                if (!ticket.getStatus().podeIrPara(StatusTicket.ENCERRADO)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException(
                            "Saída bloqueada: o ticket do veículo " + p + " ainda não foi pago.");
                }

                LocalDateTime agora = LocalDateTime.now();

                if (!ticketDAO.encerrar(conexao, ticket.getId(), agora)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("Não foi possível registrar a saída. Tente novamente.");
                }
                vagaDAO.atualizarStatus(conexao, ticket.getVagaId(), Vaga.LIVRE);

                conexao.commit();
                ticket.setStatus(StatusTicket.ENCERRADO);
                ticket.setDataSaida(agora);
                return ticket;

            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private static String validar(String placa) throws OperacaoInvalidaException {
        String p = ValidadorPlaca.normalizar(placa);
        if (!ValidadorPlaca.isValida(p)) {
            throw new OperacaoInvalidaException(
                    "Placa inválida. Use o formato antigo (ABC1234) ou Mercosul (ABC1D23).");
        }
        return p;
    }

    private static OperacaoInvalidaException naoEncontrado(String placa) {
        return new OperacaoInvalidaException("Nenhum veículo com a placa " + placa + " está no estacionamento.");
    }
}
