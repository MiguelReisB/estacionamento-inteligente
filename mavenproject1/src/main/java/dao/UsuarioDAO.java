package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;
import util.FabricaConexao;

/**
 *
 * @author henri
 */
public class UsuarioDAO {

    public enum ResultadoCadastro {
        SUCESSO,
        EMAIL_JA_CADASTRADO
    }

    public boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE email = ?";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, email);
            try (ResultSet rs = comando.executeQuery()) {
                return rs.next();
            }
        }
    }

    public ResultadoCadastro cadastrar(String nome, String email, String senha) throws SQLException {
        if (emailExiste(email)) {
            return ResultadoCadastro.EMAIL_JA_CADASTRADO;
        }

        // salva so o hash da senha (bcrypt ja gera o salt sozinho)
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt(12));
        String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, nome);
            comando.setString(2, email);
            comando.setString(3, hash);
            comando.executeUpdate();
            return ResultadoCadastro.SUCESSO;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // email duplicado
                return ResultadoCadastro.EMAIL_JA_CADASTRADO;
            }
            throw e;
        }
    }

    public boolean autenticar(String email, String senha) throws SQLException {
        String sql = "SELECT senha FROM usuarios WHERE email = ?";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, email);
            try (ResultSet rs = comando.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return senhaConfere(senha, rs.getString("senha"));
            }
        }
    }

    static boolean senhaConfere(String senha, String hash) {
        // usuario antigo com senha sem hash nao entra
        if (hash == null || !hash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(senha, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
