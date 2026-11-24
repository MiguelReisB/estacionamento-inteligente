package estacionamento_api.service;

import estacionamento_api.repository.UsuarioRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import estacionamento_api.model.Usuario;

@Service
public class UsuarioService {

    public enum ResultadoPerfil {
        ALTERADO,
        SEM_PERMISSAO,
        NAO_ENCONTRADO,
        PERFIL_INVALIDO,
        PROPRIO_USUARIO
    }

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean emailExiste(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    // retorna null se o e-mail ja estiver cadastrado
    // todo cadastro novo entra como OPERADOR, so um admin muda o perfil depois
    public Usuario cadastrarUsuario(Usuario usuario) {

        if (emailExiste(usuario.getEmail())) {
            return null;
        }

        usuario.setId(null);
        usuario.setSenha(BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt(12)));
        usuario.setPerfil(Usuario.OPERADOR);

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> autenticar(String email, String senha) {

        Optional<Usuario> encontrado = usuarioRepository.findByEmail(email);

        if (encontrado.isEmpty() || senha == null) {
            return Optional.empty();
        }

        Usuario usuario = encontrado.get();
        String salva = usuario.getSenha();

        if (salva != null && salva.startsWith("$2")) {
            try {
                return BCrypt.checkpw(senha, salva) ? Optional.of(usuario) : Optional.empty();
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        // conta antiga com senha sem hash: se bater, ja troca pelo hash
        if (senha.equals(salva)) {
            usuario.setSenha(BCrypt.hashpw(senha, BCrypt.gensalt(12)));
            usuarioRepository.save(usuario);
            return Optional.of(usuario);
        }

        return Optional.empty();
    }

    public boolean validarLogin(String email, String senha) {
        return autenticar(email, senha).isPresent();
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public boolean ehAdmin(Integer idUsuario) {
        if (idUsuario == null) {
            return false;
        }
        return usuarioRepository.findById(idUsuario).map(Usuario::isAdmin).orElse(false);
    }

    public ResultadoPerfil alterarPerfil(Integer idSolicitante, Integer idAlvo, String novoPerfil) {

        if (!ehAdmin(idSolicitante)) {
            return ResultadoPerfil.SEM_PERMISSAO;
        }
        if (!Usuario.ADMIN.equals(novoPerfil) && !Usuario.OPERADOR.equals(novoPerfil)) {
            return ResultadoPerfil.PERFIL_INVALIDO;
        }
        // o admin nao pode tirar o proprio acesso
        if (idSolicitante.equals(idAlvo)) {
            return ResultadoPerfil.PROPRIO_USUARIO;
        }

        Optional<Usuario> alvo = usuarioRepository.findById(idAlvo);
        if (alvo.isEmpty()) {
            return ResultadoPerfil.NAO_ENCONTRADO;
        }

        Usuario usuario = alvo.get();
        usuario.setPerfil(novoPerfil);
        usuarioRepository.save(usuario);
        return ResultadoPerfil.ALTERADO;
    }
}
