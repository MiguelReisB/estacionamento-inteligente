package service;

import dao.TicketDAO;
import dao.VagaDAO;
import model.TipoVeiculo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.EntradaVeiculoService.ResultadoEntrada;
import util.FabricaConexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Integridade ACID, Transações e Concorrência")
public class ConcorrenciaTransacoesTest {

    private EntradaVeiculoService service;
    private VagaDAO vagaDAO;
    private TicketDAO ticketDAO;

    @BeforeEach
    void setup() throws SQLException {
        service = new EntradaVeiculoService();
        vagaDAO = new VagaDAO();
        ticketDAO = new TicketDAO();
        
        // Isola o cenário deixando exatamente 1 vaga livre (C01) antes de cada teste
        prepararCenarioUmaVagaLivre();
    }

    @AfterAll
    static void tearDownFinal() throws SQLException {
        // Resolve a restauração do banco: limpa vestígios e libera as vagas para a GUI
        restaurarBancoAoFinal();
    }

    @Test
    @DisplayName("Cenário 1: Deve realizar COMMIT com sucesso ao registrar entrada válida")
    void testarCenarioSucessoCommit() throws Exception {
        String placa = "ABC1D23";

        ResultadoEntrada resultado = service.processarEntrada(placa, TipoVeiculo.CARRO, "Cancela Norte");

        assertTrue(resultado.liberada(), "A entrada deveria ser liberada.");
        assertEquals("C01", resultado.vaga(), "O veículo deveria ocupar a vaga C01.");
        assertNotNull(ticketDAO.buscarAbertoPorPlaca(placa), "O ticket deve estar persistido no banco de dados.");
        assertEquals(0, vagaDAO.contarLivres(), "Não devem sobrar vagas livres.");
    }

    @Test
    @DisplayName("Cenário 2: Deve executar ROLLBACK garantindo atomicidade quando ocorre falha")
    void testarCenarioFalhaRollback() throws SQLException {
        String placa = "ROL9A99";
        int vagasLivresAntes = vagaDAO.contarLivres();

        // Dispara a falha simulada (força SQLException após insert do ticket)
        assertThrows(SQLException.class, () -> {
            service.processarEntradaComFalhaSimulada(placa, TipoVeiculo.CARRO, "Cancela Norte");
        }, "Uma exceção era esperada para acionar o rollback.");

        int vagasLivresDepois = vagaDAO.contarLivres();
        boolean existeTicketFantasma = ticketDAO.buscarAbertoPorPlaca(placa) != null;

        // Validações ACID
        assertEquals(vagasLivresAntes, vagasLivresDepois, "O total de vagas livres não deve ser alterado após rollback.");
        assertFalse(existeTicketFantasma, "Nenhum ticket fantasma deve ser persistido após o rollback.");
    }

    @Test
    @DisplayName("Cenário 3: Deve garantir exclusão mútua via Lock Pessimista quando duas cancelas disputam a última vaga")
    void testarConcorrenciaSimultaneaUltimaVaga() throws InterruptedException, ExecutionException, SQLException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch largadaSimultanea = new CountDownLatch(1);

        Future<ResultadoEntrada> tarefaCancelaNorte = executor.submit(() -> {
            largadaSimultanea.await(); // aguarda para disparar exatamente junto
            return service.processarEntrada("ABC1D23", TipoVeiculo.CARRO, "Cancela Norte");
        });

        Future<ResultadoEntrada> tarefaCancelaSul = executor.submit(() -> {
            largadaSimultanea.await(); // aguarda para disparar exatamente junto
            return service.processarEntrada("XYZ9K88", TipoVeiculo.CARRO, "Cancela Sul");
        });

        // Libera ambas as threads no mesmo instante
        largadaSimultanea.countDown();

        ResultadoEntrada resNorte = tarefaCancelaNorte.get();
        ResultadoEntrada resSul = tarefaCancelaSul.get();
        executor.shutdown();

        // Operador XOR (^): exatamente uma cancela deve ter liberado a entrada, e a outra bloqueado
        assertTrue(resNorte.liberada() ^ resSul.liberada(), 
                "Exatamente uma cancela deve conseguir a vaga. Ambas não podem entrar e nenhuma pode ser ignorada.");

        // O pátio deve ter ficado com 0 vagas livres
        assertEquals(0, vagaDAO.contarLivres(), "A vaga disponível deve ter sido ocupada.");
    }
    
        @Test
    @DisplayName("Deve recusar a entrada quando todas as vagas estiverem ocupadas (Lotação Esgotada)")
    void testarEntradaComLotacaoEsgotada() throws Exception {
        // Ocupa todas as vagas
        try (Connection conn = FabricaConexao.getConexao();
             PreparedStatement ps = conn.prepareStatement("UPDATE vagas SET status = 'OCUPADA'")) {
            ps.executeUpdate();
        }

        // Tenta entrar com o pátio 100% cheio
        ResultadoEntrada resultado = service.processarEntrada("LOT1A00", TipoVeiculo.CARRO, "Cancela Norte");

        assertFalse(resultado.liberada(), "A entrada não deveria ser liberada.");
        assertNull(resultado.vaga(), "Nenhuma vaga deve ser atribuída.");
        assertEquals(0, resultado.ticketId(), "Nenhum ticket deve ser gerado.");
    }

    // ================= MÉTODOS AUXILIARES =================

    private static void prepararCenarioUmaVagaLivre() throws SQLException {
        try (Connection conn = FabricaConexao.getConexao()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM tickets WHERE placa IN ('ABC1D23', 'XYZ9K88', 'ROL9A99')");
                 PreparedStatement ps2 = conn.prepareStatement("UPDATE vagas SET status = 'OCUPADA'");
                 PreparedStatement ps3 = conn.prepareStatement("UPDATE vagas SET status = 'LIVRE' WHERE identificacao = 'C01'")) {
                ps1.executeUpdate();
                ps2.executeUpdate();
                ps3.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void restaurarBancoAoFinal() throws SQLException {
        try (Connection conn = FabricaConexao.getConexao()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM tickets WHERE placa IN ('ABC1D23', 'XYZ9K88', 'ROL9A99')");
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