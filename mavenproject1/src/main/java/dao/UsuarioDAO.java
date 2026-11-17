package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;
import util.FabricaConexao;

/**
 * Acesso à tabela usuarios. Toda a parte de SQL e de hash de senha
 * fica aqui, fora das telas.
 *
 * As senhas são guardadas com BCrypt (hash de 60 caracteres com salt
 * embutido); a senha em texto puro nunca é gravada no banco.
 */
public class UsuarioDAO {

    private static final int CUSTO_BCRYPT = 12;

    /** Resultado do cadastro, para a tela mostrar a mensagem certa. */
    public enum ResultadoCadastro {
        SUCESSO,
        EMAIL_JA_CADASTRADO
    }

    public boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE email = ?";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, email);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public ResultadoCadastro cadastrar(String nome, String email, String senha) throws SQLException {
        if (emailExiste(email)) {
            return ResultadoCadastro.EMAIL_JA_CADASTRADO;
        }

        String hash = BCrypt.hashpw(senha, BCrypt.gensalt(CUSTO_BCRYPT));
        String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, nome);
            comando.setString(2, email);
            comando.setString(3, hash);
            comando.executeUpdate();
            return ResultadoCadastro.SUCESSO;

        } catch (SQLException e) {
            // 1062 = chave duplicada (outro cadastro com o mesmo e-mail ao mesmo tempo)
            if (e.getErrorCode() == 1062) {
                return ResultadoCadastro.EMAIL_JA_CADASTRADO;
            }
            throw e;
        }
    }

    /**
     * Verifica e-mail e senha. Retorna true só se o usuário existir
     * e a senha conferir com o hash salvo.
     */
    public boolean autenticar(String email, String senha) throws SQLException {
        String sql = "SELECT senha FROM usuarios WHERE email = ?";

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, email);

            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    return false;
                }
                return senhaConfere(senha, resultado.getString("senha"));
            }
        }
    }

    static boolean senhaConfere(String senha, String hash) {
        if (hash == null || !hash.startsWith("$2")) {
            return false; // registro antigo sem hash: não aceita
        }
        try {
            return BCrypt.checkpw(senha, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
