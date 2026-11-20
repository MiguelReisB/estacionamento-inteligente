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
 *
 * @author henri
 */
public class VagaDAO {

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
                vagas.add(montarVaga(rs));
            }
        }
        return vagas;
    }

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

    // os metodos abaixo recebem a conexao porque rodam dentro da transacao
    // de entrada/saida, quem chama e que faz o commit ou rollback

    // pega uma vaga livre do tipo e trava a linha com FOR UPDATE,
    // assim outra cancela nao consegue pegar a mesma vaga ao mesmo tempo
    // retorna null se nao tiver vaga
    public Vaga buscarLivreComLock(Connection conexao, TipoVeiculo tipo) throws SQLException {
        String sql = "SELECT id, identificacao, tipo, status FROM vagas "
                + "WHERE tipo = ? AND status = 'LIVRE' ORDER BY id LIMIT 1 FOR UPDATE";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, tipo.name());
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return montarVaga(rs);
                }
                return null;
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

    private Vaga montarVaga(ResultSet rs) throws SQLException {
        return new Vaga(rs.getInt("id"),
                rs.getString("identificacao"),
                TipoVeiculo.valueOf(rs.getString("tipo")),
                rs.getString("status"));
    }
}
