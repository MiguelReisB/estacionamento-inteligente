/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.TicketDAO;
import dao.VagaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.TipoVeiculo;
import service.EntradaVeiculoService.ResultadoEntrada;
import util.FabricaConexao;

/**
 *
 * @author Pstch
 */
public class TesteConcorrenciaTransacoes {

    public static void main(String[] args) throws Exception {
        System.out.println("  TESTES - TRANSACOES E CONCORRENCIA");
        testarRollbackFalhaSimulada();
        System.out.println();
        testarConcorrenciaUltimaVaga();
    }

    /*
     * CENÁRIO 1: Demonstração de ROLLBACK
     * Força uma falha após inserir o ticket e antes de atualizar a vaga,
     * comprovando que nenhum "carro fantasma" é persistido no banco.
     */
    private static void testarRollbackFalhaSimulada() throws SQLException {
        System.out.println(">>> CENARIO 1: TESTE DE ROLLBACK");
        prepararCenarioUmaVagaLivre();

        EntradaVeiculoService service = new EntradaVeiculoService();
        VagaDAO vagaDAO = new VagaDAO();
        TicketDAO ticketDAO = new TicketDAO();

        String placaTeste = "ROL9A99";
        int vagasLivresAntes = vagaDAO.contarLivres();
        System.out.println("Estado inicial -> Vagas livres: " + vagasLivresAntes + " | Ticket aberto para " + placaTeste + ": " + (ticketDAO.buscarAbertoPorPlaca(placaTeste) != null));

        try {
            service.processarEntradaComFalhaSimulada(placaTeste, TipoVeiculo.CARRO, "Cancela Norte");
        } catch (SQLException | OperacaoInvalidaException e) {
            System.out.println("Excecao capturada com sucesso: " + e.getMessage());
        }

        int vagasLivresDepois = vagaDAO.contarLivres();
        boolean existeTicketFantasma = ticketDAO.buscarAbertoPorPlaca(placaTeste) != null;

        System.out.println("Estado pos-ROLLBACK -> Vagas livres: " + vagasLivresDepois + " | Ticket fantasma no banco: " + existeTicketFantasma);
        if (vagasLivresAntes == vagasLivresDepois && !existeTicketFantasma) {
            System.out.println("[SUCESSO] Integridade ACID preservada: o ROLLBACK desfez a insercao do ticket!");
        } else {
            System.out.println("[FALHA] Inconsistencia detectada no banco!");
        }
    }

    /*
     * CENÁRIO 2: Demonstração de CONCORRÊNCIA + LOCK PESSIMISTA (SELECT ... FOR UPDATE)
     * Deixa apenas 1 vaga livre no pátio e dispara Cancela Norte e Cancela Sul
     * no exato mesmo milissegundo usando CountDownLatch.
     */
    private static void testarConcorrenciaUltimaVaga() throws SQLException, InterruptedException, ExecutionException {
        System.out.println(">>> CENARIO 2: CONCORRENCIA SIMULTANEA (CANCELA NORTE VS. CANCELA SUL)");
        prepararCenarioUmaVagaLivre();

        EntradaVeiculoService service = new EntradaVeiculoService();
        VagaDAO vagaDAO = new VagaDAO();

        System.out.println("Estado inicial -> Total de vagas livres no pátio: " + vagaDAO.contarLivres());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch largadaSimultanea = new CountDownLatch(1);

        Future<ResultadoEntrada> tarefaCancelaNorte = executor.submit(() -> {
            largadaSimultanea.await(); // aguarda sinal exato para disparar junto
            return service.processarEntrada("ABC1D23", TipoVeiculo.CARRO, "Cancela Norte");
        });

        Future<ResultadoEntrada> tarefaCancelaSul = executor.submit(() -> {
            largadaSimultanea.await(); // aguarda sinal exato para disparar junto
            return service.processarEntrada("XYZ9K88", TipoVeiculo.CARRO, "Cancela Sul");
        });

        // Libera ambas as threads no mesmo milissegundo
        largadaSimultanea.countDown();

        ResultadoEntrada resNorte = tarefaCancelaNorte.get();
        ResultadoEntrada resSul = tarefaCancelaSul.get();
        executor.shutdown();

        System.out.println("\n--- Resultado Consolidado ---");
        System.out.println("Cancela Norte (ABC1D23): " + (resNorte.liberada() ? "ENTROU na vaga " + resNorte.vaga() + " (Ticket #" + resNorte.ticketId() + ")" : "BLOQUEADA - Lotação Esgotada"));
        System.out.println("Cancela Sul   (XYZ9K88): " + (resSul.liberada() ? "ENTROU na vaga " + resSul.vaga() + " (Ticket #" + resSul.ticketId() + ")" : "BLOQUEADA - Lotação Esgotada"));
        System.out.println("Vagas livres restantes no banco: " + vagaDAO.contarLivres());

        if (resNorte.liberada() ^ resSul.liberada()) {
            System.out.println("[SUCESSO] Exclusao mutua garantida pelo SELECT ... FOR UPDATE: apenas 1 carro entrou e não houve superlotacao");
        } else {
            System.out.println("[FALHA] Ambas entraram ou nenhuma entrou!");
        }
    }

    // Prepara o banco deixando apenas a vaga C01 livre para o teste
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
}
