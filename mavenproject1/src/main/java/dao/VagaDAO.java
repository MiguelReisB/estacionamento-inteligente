package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.TipoVeiculo;
import model.Vaga;
import util.FabricaConexao;

/**
 * Acesso à tabela vagas.
 *
 * Os métodos que recebem uma Connection são usados dentro de transações
 * (EntradaVeiculoService / SaidaVeiculoService): quem chama faz commit/rollback.
 */
public class VagaDAO {

    // ---------- operações simples (abrem a própria conexão) ----------

    public void cadastrar(String identificacao, TipoVeiculo tipo) throws SQLException {
        String sql = "INSERT INTO vagas (identificacao, tipo, status) VALUES (?, ?, 'LIVRE')";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, identificacao.trim().toUpperCase());
            comando.setString(2, tipo.name());
            comando.executeUpdate();
        }
    }

    public List<Vaga> listar() throws SQLException {
        String sql = "SELECT id, identificacao, tipo, status FROM vagas ORDER BY tipo, identificacao";
        List<Vaga> vagas = new ArrayList<>();

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {

            while (rs.next()) {
                vagas.add(montar(rs));
            }
        }
        return vagas;
    }

    /** Conta vagas por status ("LIVRE" ou "OCUPADA"), de todos os tipos. */
    public int contarPorStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vagas WHERE status = ?";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, status);
            try (ResultSet rs = comando.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int contarLivres() throws SQLException {
        return contarPorStatus(Vaga.LIVRE);
    }

    public int contarOcupadas() throws SQLException {
        return contarPorStatus(Vaga.OCUPADA);
    }

    // ---------- operações transacionais (recebem a conexão) ----------

    /**
     * Procura uma vaga livre do tipo informado e TRAVA a linha (SELECT ... FOR UPDATE).
     * Outra transação que tentar pegar a mesma vaga fica esperando até o commit/rollback.
     * SKIP LOCKED não é usado de propósito, para a disputa ficar visível na demonstração.
     *
     * @return a vaga travada, ou null se não houver vaga livre (lotado)
     */
    public Vaga buscarLivreComLock(Connection conexao, TipoVeiculo tipo) throws SQLException {
        String sql = "SELECT id, identificacao, tipo, status FROM vagas "
                + "WHERE tipo = ? AND status = 'LIVRE' ORDER BY id LIMIT 1 FOR UPDATE";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, tipo.name());
            try (ResultSet rs = comando.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    public void atualizarStatus(Connection conexao, int vagaId, String status) throws SQLException {
        String sql = "UPDATE vagas SET status = ? WHERE id = ?";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, status);
            comando.setInt(2, vagaId);
            comando.executeUpdate();
        }
    }

    private Vaga montar(ResultSet rs) throws SQLException {
        return new Vaga(
                rs.getInt("id"),
                rs.getString("identificacao"),
                TipoVeiculo.valueOf(rs.getString("tipo")),
                rs.getString("status"));
    }
}
