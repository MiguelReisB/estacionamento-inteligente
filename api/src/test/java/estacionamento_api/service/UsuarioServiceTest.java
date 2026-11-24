package estacionamento_api.service;

import estacionamento_api.model.Usuario;
import estacionamento_api.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UsuarioService - hash de senha e perfis")
class UsuarioServiceTest {

    private UsuarioRepository repositorio;
    private UsuarioService service;

    @BeforeEach
    void setup() {
        repositorio = mock(UsuarioRepository.class);
        when(repositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new UsuarioService(repositorio);
    }

    private Usuario usuario(int id, String email, String senha, String perfil) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Teste " + id);
        u.setEmail(email);
        u.setSenha(senha);
        u.setPerfil(perfil);
        return u;
    }

    @Test
    @DisplayName("Cadastro salva hash BCrypt e sempre como OPERADOR")
    void cadastroSalvaHashEOperador() {
        when(repositorio.findByEmail("novo@x.com")).thenReturn(Optional.empty());

        Usuario novo = new Usuario();
        novo.setNome("Novo");
        novo.setEmail("novo@x.com");
        novo.setSenha("minhaSenha");
        novo.setPerfil(Usuario.ADMIN); // tentativa de se cadastrar como admin

        Usuario salvo = service.cadastrarUsuario(novo);

        assertNotNull(salvo);
        assertTrue(salvo.getSenha().startsWith("$2a$"));
        assertEquals(60, salvo.getSenha().length());
        assertTrue(BCrypt.checkpw("minhaSenha", salvo.getSenha()));
        assertEquals(Usuario.OPERADOR, salvo.getPerfil());
    }

    @Test
    @DisplayName("Cadastro com e-mail repetido retorna null")
    void cadastroEmailRepetido() {
        when(repositorio.findByEmail("a@x.com")).thenReturn(Optional.of(usuario(1, "a@x.com", "x", Usuario.OPERADOR)));
        Usuario novo = new Usuario();
        novo.setNome("A");
        novo.setEmail("a@x.com");
        novo.setSenha("123");

        assertNull(service.cadastrarUsuario(novo));
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("Login confere a senha pelo hash")
    void loginComHash() {
        String hash = BCrypt.hashpw("certa", BCrypt.gensalt(4));
        when(repositorio.findByEmail("a@x.com")).thenReturn(Optional.of(usuario(1, "a@x.com", hash, Usuario.OPERADOR)));

        assertTrue(service.autenticar("a@x.com", "certa").isPresent());
        assertTrue(service.autenticar("a@x.com", "errada").isEmpty());
        assertTrue(service.autenticar("outro@x.com", "certa").isEmpty());
    }

    @Test
    @DisplayName("Conta antiga sem hash entra uma vez e a senha vira hash")
    void loginContaAntigaMigraParaHash() {
        Usuario antigo = usuario(2, "velho@x.com", "1234", Usuario.OPERADOR);
        when(repositorio.findByEmail("velho@x.com")).thenReturn(Optional.of(antigo));

        assertTrue(service.autenticar("velho@x.com", "1234").isPresent());
        assertTrue(antigo.getSenha().startsWith("$2a$"));
        assertTrue(BCrypt.checkpw("1234", antigo.getSenha()));
        verify(repositorio).save(antigo);
    }

    @Test
    @DisplayName("So administrador altera perfil, e nao o proprio")
    void alterarPerfilRegras() {
        Usuario admin = usuario(1, "adm@x.com", "h", Usuario.ADMIN);
        Usuario operador = usuario(2, "op@x.com", "h", Usuario.OPERADOR);
        when(repositorio.findById(1)).thenReturn(Optional.of(admin));
        when(repositorio.findById(2)).thenReturn(Optional.of(operador));
        when(repositorio.findById(99)).thenReturn(Optional.empty());

        assertEquals(UsuarioService.ResultadoPerfil.SEM_PERMISSAO, service.alterarPerfil(2, 1, Usuario.OPERADOR));
        assertEquals(UsuarioService.ResultadoPerfil.SEM_PERMISSAO, service.alterarPerfil(null, 2, Usuario.ADMIN));
        assertEquals(UsuarioService.ResultadoPerfil.PROPRIO_USUARIO, service.alterarPerfil(1, 1, Usuario.OPERADOR));
        assertEquals(UsuarioService.ResultadoPerfil.PERFIL_INVALIDO, service.alterarPerfil(1, 2, "CHEFE"));
        assertEquals(UsuarioService.ResultadoPerfil.NAO_ENCONTRADO, service.alterarPerfil(1, 99, Usuario.ADMIN));

        assertEquals(UsuarioService.ResultadoPerfil.ALTERADO, service.alterarPerfil(1, 2, Usuario.ADMIN));
        assertEquals(Usuario.ADMIN, operador.getPerfil());
    }
}
