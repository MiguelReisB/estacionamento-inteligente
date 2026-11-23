package dao;

import dao.UsuarioDAO.ResultadoCadastro;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.FabricaConexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Segurança e Autenticação - UsuarioDAO")
public class UsuarioDAOTest {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final String EMAIL_TESTE = "operador.teste@estacionamento.com";
    private final String SENHA_TESTE = "SenhaForte@123";

    @BeforeEach
    void setup() throws SQLException {
        removerUsuarioTeste();
    }

    @AfterAll
    static void cleanupFinal() throws SQLException {
        removerUsuarioTeste();
    }

    @Test
    @DisplayName("Deve cadastrar um novo usuário com sucesso e salvar senha hasheada")
    void testarCadastroComSucesso() throws SQLException {
        ResultadoCadastro resultado = usuarioDAO.cadastrar("Operador Teste", EMAIL_TESTE, SENHA_TESTE);

        assertEquals(ResultadoCadastro.SUCESSO, resultado);
        assertTrue(usuarioDAO.emailExiste(EMAIL_TESTE));
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com e-mail já existente")
    void testarCadastroEmailDuplicado() throws SQLException {
        // Primeiro cadastro
        usuarioDAO.cadastrar("Operador Teste", EMAIL_TESTE, SENHA_TESTE);

        // Tentativa de cadastrar o mesmo e-mail
        ResultadoCadastro resultadoDuplicado = usuarioDAO.cadastrar("Outro Nome", EMAIL_TESTE, "OutraSenha123");

        assertEquals(ResultadoCadastro.EMAIL_JA_CADASTRADO, resultadoDuplicado);
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando a senha informada for a correta")
    void testarAutenticacaoSenhaCorreta() throws SQLException {
        usuarioDAO.cadastrar("Operador Teste", EMAIL_TESTE, SENHA_TESTE);

        boolean autenticado = usuarioDAO.autenticar(EMAIL_TESTE, SENHA_TESTE);

        assertTrue(autenticado, "A autenticação com a senha correta deve retornar true.");
    }

    @Test
    @DisplayName("Deve rejeitar autenticação quando a senha informada for incorreta")
    void testarAutenticacaoSenhaIncorreta() throws SQLException {
        usuarioDAO.cadastrar("Operador Teste", EMAIL_TESTE, SENHA_TESTE);

        boolean autenticado = usuarioDAO.autenticar(EMAIL_TESTE, "SenhaErrada@999");

        assertFalse(autenticado, "A autenticação com senha incorreta deve retornar false.");
    }

    @Test
    @DisplayName("Deve rejeitar autenticação se o e-mail não existir no banco")
    void testarAutenticacaoEmailInexistente() throws SQLException {
        boolean autenticado = usuarioDAO.autenticar("email.fantasma@inexistente.com", "qualquerSenha");

        assertFalse(autenticado);
    }

    private static void removerUsuarioTeste() throws SQLException {
        try (Connection conn = FabricaConexao.getConexao();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM usuarios WHERE email = 'operador.teste@estacionamento.com'")) {
            ps.executeUpdate();
        }
    }
}