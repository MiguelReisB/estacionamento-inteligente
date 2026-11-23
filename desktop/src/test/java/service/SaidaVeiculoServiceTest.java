package service;

import dao.TicketDAO;
import dao.VagaDAO;
import model.StatusTicket;
import model.Ticket;
import model.TipoVeiculo;
import model.Vaga;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.FabricaConexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Negócio - Pagamento e Saída de Veículos")
public class SaidaVeiculoServiceTest {

    private final EntradaVeiculoService entradaService = new EntradaVeiculoService();
    private final SaidaVeiculoService saidaService = new SaidaVeiculoService();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final VagaDAO vagaDAO = new VagaDAO();

    private final String PLACA_TESTE = "SAI1A23";

    @BeforeEach
    void setup() throws Exception {
        limparDadosTeste();
        // Garante que o carro de teste dê entrada antes de testar a saída
        entradaService.processarEntrada(PLACA_TESTE, TipoVeiculo.CARRO, "Cancela Teste");
    }

    @AfterAll
    static void cleanupFinal() throws SQLException {
        limparDadosTeste();
    }

    @Test
    @DisplayName("Deve consultar o ticket ativo e calcular o valor parcial da estadia")
    void testarConsultaTicketAtivo() throws Exception {
        Ticket ticket = saidaService.consultar(PLACA_TESTE);

        assertNotNull(ticket);
        assertEquals(PLACA_TESTE, ticket.getPlaca());
        assertEquals(StatusTicket.ATIVO, ticket.getStatus());
        assertNotNull(ticket.getValor(), "Deve calcular a tarifa no momento da consulta.");
    }

    @Test
    @DisplayName("Deve bloquear a saída se o veículo ainda não realizou o pagamento")
    void testarBloqueioSaidaSemPagamento() {
        OperacaoInvalidaException ex = assertThrows(OperacaoInvalidaException.class, () -> {
            saidaService.registrarSaida(PLACA_TESTE);
        });

        assertTrue(ex.getMessage().contains("ainda não pagou"), "A mensagem deve alertar falta de pagamento.");
    }

    @Test
    @DisplayName("Deve registrar o pagamento e alterar o status para PAGO")
    void testarPagamentoComSucesso() throws Exception {
        Ticket ticketPago = saidaService.pagar(PLACA_TESTE);

        assertEquals(StatusTicket.PAGO, ticketPago.getStatus());
        assertNotNull(ticketPago.getDataPagamento());

        // Confirma no banco de dados
        Ticket doBanco = ticketDAO.buscarAbertoPorPlaca(PLACA_TESTE);
        assertEquals(StatusTicket.PAGO, doBanco.getStatus());
    }

    @Test
    @DisplayName("Não deve permitir pagar um ticket que já foi pago")
    void testarBloqueioPagamentoDuplicado() throws Exception {
        saidaService.pagar(PLACA_TESTE); // Primeiro pagamento OK

        assertThrows(OperacaoInvalidaException.class, () -> {
            saidaService.pagar(PLACA_TESTE); // Segunda tentativa deve falhar
        });
    }

    @Test
    @DisplayName("Deve registrar a saída após o pagamento e liberar a vaga no pátio")
    void testarRegistroSaidaELiberacaoDeVaga() throws Exception {
        Ticket ticketPago = saidaService.pagar(PLACA_TESTE);
        int vagaId = ticketPago.getVagaId();

        Ticket ticketEncerrado = saidaService.registrarSaida(PLACA_TESTE);

        assertEquals(StatusTicket.ENCERRADO, ticketEncerrado.getStatus());
        assertNotNull(ticketEncerrado.getDataSaida());

        // Confere se o ticket não consta mais como aberto
        assertNull(ticketDAO.buscarAbertoPorPlaca(PLACA_TESTE));

        // Confere se a vaga no banco voltou a ficar LIVRE
        Vaga vagaAtualizada = vagaDAO.listar().stream()
                .filter(v -> v.getId() == vagaId)
                .findFirst()
                .orElseThrow();
        assertEquals(Vaga.LIVRE, vagaAtualizada.getStatus(), "A vaga deve estar livre após a saída.");
    }

    private static void limparDadosTeste() throws SQLException {
        try (Connection conn = FabricaConexao.getConexao()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM tickets WHERE placa = 'SAI1A23'");
                 PreparedStatement ps2 = conn.prepareStatement("UPDATE vagas SET status = 'LIVRE'")) {
                ps1.executeUpdate();
                ps2.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}