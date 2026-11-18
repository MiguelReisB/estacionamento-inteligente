
package com.mycompany.mavenproject1;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UsuarioApiClient {

    public static int cadastrar(String nome, String email, String senha)
            throws Exception {

        String json = """
                {
                    "nome": "%s",
                    "email": "%s",
                    "senha": "%s"
                }
                """.formatted(
                    escaparJson(nome),
                    escaparJson(email),
                    escaparJson(senha)
                );

        HttpClient cliente = HttpClient.newHttpClient();

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/usuarios"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<Void> resposta = cliente.send(
                requisicao,
                HttpResponse.BodyHandlers.discarding()
        );

        return resposta.statusCode();
    }
    
    public static int autenticar(String email, String senha)
            throws Exception {

        String json = """
            {
                "email": "%s",
                "senha": "%s"
            }
            """.formatted(
                escaparJson(email),
                escaparJson(senha)
            );

    HttpClient cliente = HttpClient.newHttpClient();

    HttpRequest requisicao = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/usuarios/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    HttpResponse<Void> resposta = cliente.send(
            requisicao,
            HttpResponse.BodyHandlers.discarding()
    );

    return resposta.statusCode();
}

    private static String escaparJson(String valor) {

        StringBuilder resultado = new StringBuilder();

        for (char caractere : valor.toCharArray()) {

            switch (caractere) {

                case '"':
                    resultado.append("\\\"");
                    break;

                case '\\':
                    resultado.append("\\\\");
                    break;

                case '\b':
                    resultado.append("\\b");
                    break;

                case '\f':
                    resultado.append("\\f");
                    break;

                case '\n':
                    resultado.append("\\n");
                    break;

                case '\r':
                    resultado.append("\\r");
                    break;

                case '\t':
                    resultado.append("\\t");
                    break;

                default:
                    if (caractere < 0x20) {

                        resultado.append("\\u");

                        String hexadecimal =
                                Integer.toHexString(caractere);

                        resultado.append(
                                "0".repeat(4 - hexadecimal.length())
                        );

                        resultado.append(hexadecimal);

                    } else {
                        resultado.append(caractere);
                    }
            }
        }

        return resultado.toString();
    }
}
