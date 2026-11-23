package com.mycompany.mavenproject1;

/**
 * Guarda quem esta logado no sistema. O perfil decide quais telas aparecem.
 *
 * @author henri
 */
public final class Sessao {

    private static UsuarioInfo usuario;

    private Sessao() {
    }

    public static void iniciar(UsuarioInfo usuarioLogado) {
        usuario = usuarioLogado;
    }

    public static UsuarioInfo getUsuario() {
        return usuario;
    }

    public static boolean isAdmin() {
        return usuario != null && usuario.isAdmin();
    }

    public static void encerrar() {
        usuario = null;
    }
}
