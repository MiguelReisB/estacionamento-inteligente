
package com.mycompany.mavenproject1;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Mavenproject1 {

    public static void main(String[] args) {

        // Abre a tela de carregamento primeiro
        SwingUtilities.invokeLater(() -> {

            Tela_Carregamento carregamento = new Tela_Carregamento();
            carregamento.setVisible(true);

            // Inicia o processo da API em segundo plano
            new Thread(() -> iniciarSistema(carregamento),
                    "InicializacaoSistema").start();

        });
    }

    private static void iniciarSistema(Tela_Carregamento carregamento) {

        Process processoApi = null;

        try {

            carregamento.atualizarMensagem("Verificando conexão com a API...");

            // Localiza a pasta da API
            Path desktopDir = Path.of(System.getProperty("user.dir"));

            Path apiDir = desktopDir
                    .resolve("..")
                    .resolve("api")
                    .normalize();

            Path arquivoJar = apiDir
                    .resolve("target")
                    .resolve("estacionamento-api-0.0.1-SNAPSHOT.jar");

            // Verifica se a API já está funcionando
            if (!apiEstaDisponivel()) {

                carregamento.atualizarMensagem("Iniciando servidor...");

                // Verifica se o JAR existe
                if (!arquivoJar.toFile().exists()) {
                    throw new Exception(
                            "Arquivo JAR da API não encontrado:\n" + arquivoJar
                    );
                }

                // Inicia a API
                processoApi = new ProcessBuilder(
                        "java", "-jar", arquivoJar.toString()
                )
                        .directory(apiDir.toFile())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.appendTo(
                                new File(apiDir.toFile(), "api.log")
                        ))
                        .start();

                // Guarda o processo para encerrá-lo ao fechar o programa
                Process processoFinal = processoApi;

                Runtime.getRuntime().addShutdownHook(
                        new Thread(processoFinal::destroy)
                );

            }

            // Aguarda a API ficar disponível
            int tentativas = 0;

            carregamento.atualizarMensagem("Aguardando resposta do servidor...");

            while (!apiEstaDisponivel() && tentativas < 30) {

                Thread.sleep(1000);
                tentativas++;
            }

            if (!apiEstaDisponivel()) {
                throw new Exception("A API não iniciou corretamente.");
            }

            carregamento.atualizarMensagem("Sistema pronto!");

            // Abre a tela de login
            SwingUtilities.invokeLater(() -> {

                carregamento.dispose();

                Tela_Login login = new Tela_Login();
                login.setVisible(true);

            });

        } catch (Exception e) {

            SwingUtilities.invokeLater(() -> {

                carregamento.dispose();

                JOptionPane.showMessageDialog(
                        null,
                        "Não foi possível iniciar o sistema:\n"
                        + e.getMessage(),
                        "Erro ao iniciar",
                        JOptionPane.ERROR_MESSAGE
                );

            });

        }

    }

    // Verifica se a API está respondendo
    private static boolean apiEstaDisponivel() {

        try {

            URL url = new URL("http://localhost:8080/api/status");

            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(1000);
            conexao.setReadTimeout(1000);

            int resposta = conexao.getResponseCode();

            conexao.disconnect();

            return resposta == 200;

        } catch (Exception e) {

            return false;

        }

    }

}
