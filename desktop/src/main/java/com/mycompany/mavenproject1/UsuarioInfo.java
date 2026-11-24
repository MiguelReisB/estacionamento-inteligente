package com.mycompany.mavenproject1;

/**
 * Dados de um usuario que a API devolve (nunca vem a senha).
 *
 * @author henri
 */
public record UsuarioInfo(int id, String nome, String email, String perfil) {

    public static final String ADMIN = "ADMIN";
    public static final String OPERADOR = "OPERADOR";

    public boolean isAdmin() {
        return ADMIN.equals(perfil);
    }

    public String perfilDescricao() {
        return isAdmin() ? "Administrador" : "Operador";
    }
}
