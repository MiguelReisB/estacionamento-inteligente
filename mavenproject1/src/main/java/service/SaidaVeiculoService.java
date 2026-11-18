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
 *
 * @author henri
 */
public class SaidaVeiculoService {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final VagaDAO vagaDAO = new VagaDAO();

    // so consulta, nao grava nada. se ainda nao pagou ja calcula o valor de agora
    public Ticket consultar(String placa) throws OperacaoInvalidaException, SQLException {
        placa = validarPlaca(placa);
        Ticket ticket = ticketDAO.buscarAbertoPorPlaca(placa);

        if (ticket == null) {
            throw new OperacaoInvalidaException("Nenhum veículo com a placa " + placa + " está no estacionamento.");
        }
        if (ticket.getStatus() == StatusTicket.ATIVO) {
            ticket.setValor(CalculadoraTarifa.calcular(ticket.getDataEntrada(), LocalDateTime.now()));
        }
        return ticket;
    }

    // ATIVO -> PAGO
    public Ticket pagar(String placa) throws OperacaoInvalidaException, SQLException {
        placa = validarPlaca(placa);

        try (Connection conexao = FabricaConexao.getConexaoTransacional()) {
            try {
                Ticket ticket = ticketDAO.buscarAbertoComLock(conexao, placa);

                if (ticket == null) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("Nenhum veículo com a placa " + placa + " está no estacionamento.");
                }
                if (!ticket.getStatus().podeIrPara(StatusTicket.PAGO)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("O ticket do veículo " + placa + " já está pago.");
                }

                LocalDateTime agora = LocalDateTime.now();
                BigDecimal valor = CalculadoraTarifa.calcular(ticket.getDataEntrada(), agora);

                if (!ticketDAO.registrarPagamento(conexao, ticket.getId(), valor, agora)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("Não foi possível registrar o pagamento, tente de novo.");
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

    // PAGO -> ENCERRADO e libera a vaga (na mesma transacao)
    public Ticket registrarSaida(String placa) throws OperacaoInvalidaException, SQLException {
        placa = validarPlaca(placa);

        try (Connection conexao = FabricaConexao.getConexaoTransacional()) {
            try {
                Ticket ticket = ticketDAO.buscarAbertoComLock(conexao, placa);

                if (ticket == null) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("Nenhum veículo com a placa " + placa + " está no estacionamento.");
                }
                if (!ticket.getStatus().podeIrPara(StatusTicket.ENCERRADO)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("O veículo " + placa + " ainda não pagou, saída bloqueada.");
                }

                LocalDateTime agora = LocalDateTime.now();

                if (!ticketDAO.encerrar(conexao, ticket.getId(), agora)) {
                    conexao.rollback();
                    throw new OperacaoInvalidaException("Não foi possível registrar a saída, tente de novo.");
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

    private String validarPlaca(String placa) throws OperacaoInvalidaException {
        placa = ValidadorPlaca.normalizar(placa);
        if (!ValidadorPlaca.isValida(placa)) {
            throw new OperacaoInvalidaException("Placa inválida. Use o formato ABC1234 ou ABC1D23.");
        }
        return placa;
    }
}
