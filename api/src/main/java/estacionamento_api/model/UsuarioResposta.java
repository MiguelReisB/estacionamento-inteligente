package estacionamento_api.model;

// o que a api devolve sobre um usuario (sem a senha)
public record UsuarioResposta(Integer id, String nome, String email, String perfil) {

    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
