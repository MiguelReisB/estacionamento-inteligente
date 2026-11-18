package estacionamento_api.service;

import estacionamento_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import estacionamento_api.model.Usuario;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean emailExiste(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }
    
    public Usuario cadastrarUsuario(Usuario usuario) {

    if (emailExiste(usuario.getEmail())) {
        return null;
    }

    return usuarioRepository.save(usuario);
}
    
    public boolean validarLogin(String email, String senha) {

        return usuarioRepository.findByEmail(email)
                .map(usuario -> usuario.getSenha().equals(senha))
                .orElse(false);
}

}