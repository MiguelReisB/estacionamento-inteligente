package com.mycompany.mavenproject1;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Conversa com a API de usuarios (login, cadastro, lista e perfil).
 */
public class UsuarioApiClient {

    private static final String BASE = "http://localhost:8080/api/usuarios";
    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(15);

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // 201 = cadastrado, 409 = e-mail ja existe, 400 = dados faltando
    public static int cadastrar(String nome, String email, String senha)
            throws IOException, InterruptedException {

        JSONObject json = new JSONObject();
        json.put("nome", nome);
        json.put("email", email);
        json.put("senha", senha);

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .timeout(TEMPO_LIMITE)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return CLIENTE.send(requisicao, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    // retorna o usuario logado, ou null se o e-mail/senha estiverem errados
    public static UsuarioInfo autenticar(String email, String senha)
            throws IOException, InterruptedException {

        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("senha", senha);

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/login"))
                .timeout(TEMPO_LIMITE)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());

        if (resposta.statusCode() == 401) {
            return null;
        }
        if (resposta.statusCode() != 200) {
            throw new IOException("A API respondeu com o código " + resposta.statusCode() + ".");
        }
        return lerUsuario(new JSONObject(resposta.body()));
    }

    // so funciona para administrador
    public static List<UsuarioInfo> listar() throws IOException, InterruptedException {

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .timeout(TEMPO_LIMITE)
                .header("X-Usuario-Id", idLogado())
                .GET()
                .build();

        HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());

        if (resposta.statusCode() == 403) {
            throw new IOException("Somente administradores podem ver os usuários.");
        }
        if (resposta.statusCode() != 200) {
            throw new IOException("A API respondeu com o código " + resposta.statusCode() + ".");
        }

        JSONArray lista = new JSONArray(resposta.body());
        List<UsuarioInfo> usuarios = new ArrayList<>();
        for (int i = 0; i < lista.length(); i++) {
            usuarios.add(lerUsuario(lista.getJSONObject(i)));
        }
        return usuarios;
    }

    // 200 = alterado, 403 = sem permissao, 404 = nao achou, 409 = tentou mudar o proprio perfil
    public static int alterarPerfil(int idUsuario, String perfil)
            throws IOException, InterruptedException {

        JSONObject json = new JSONObject();
        json.put("perfil", perfil);

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/" + idUsuario + "/perfil"))
                .timeout(TEMPO_LIMITE)
                .header("Content-Type", "application/json")
                .header("X-Usuario-Id", idLogado())
                .PUT(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return CLIENTE.send(requisicao, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    static UsuarioInfo lerUsuario(JSONObject json) {
        return new UsuarioInfo(
                json.getInt("id"),
                json.optString("nome", ""),
                json.optString("email", ""),
                json.optString("perfil", UsuarioInfo.OPERADOR));
    }

    private static String idLogado() {
        UsuarioInfo usuario = Sessao.getUsuario();
        return usuario == null ? "0" : String.valueOf(usuario.id());
    }
}
