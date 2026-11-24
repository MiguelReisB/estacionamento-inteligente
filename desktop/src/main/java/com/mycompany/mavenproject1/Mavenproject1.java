
package com.mycompany.mavenproject1;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Mavenproject1 {

    private static final String ENDPOINT_STATUS =
            "http://localhost:8080/api/status";

    private static final String NOME_JAR_API =
            "estacionamento-api-0.0.1-SNAPSHOT.jar";

    public static void main(String[] args) {

        // visual padrao do sistema operacional (no Windows, o cinza do Windows)
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // se nao der, fica o visual padrao do Java
        }

        SwingUtilities.invokeLater(() -> {

            Tela_Carregamento carregamento =
                    new Tela_Carregamento();

            carregamento.setVisible(true);

            new Thread(
                    () -> iniciarSistema(carregamento),
                    "InicializacaoSistema"
            ).start();

        });
    }

    private static void iniciarSistema(
            Tela_Carregamento carregamento) {

        Process processoApi = null;

        try {

            if (!apiEstaDisponivel()) {

                Path pastaApi = localizarPastaApi();

                Path jarApi = pastaApi
                        .resolve("target")
                        .resolve(NOME_JAR_API);

                if (!Files.isRegularFile(jarApi)) {
                    throw new IllegalStateException(
                            "Não encontrei o arquivo da API:\n"
                            + jarApi
                            + "\nCompile a pasta api pelo Maven."
                    );
                }

                carregamento.atualizarMensagem(
                        "Iniciando a comunicação com o banco de dados..."
                );

                ProcessBuilder comando =
                        new ProcessBuilder(comandoInicializacao(jarApi));

                comando.directory(pastaApi.toFile());
                comando.redirectErrorStream(true);

                comando.redirectOutput(
                        ProcessBuilder.Redirect.appendTo(
                                pastaApi.resolve("api.log").toFile()
                        )
                );

                processoApi = comando.start();

                final Process processoIniciado = processoApi;

                Runtime.getRuntime().addShutdownHook(
                        new Thread(() -> {

                            if (processoIniciado.isAlive()) {

                                processoIniciado.destroy();

                                try {

                                    if (!processoIniciado.waitFor(
                                            5,
                                            java.util.concurrent.TimeUnit.SECONDS)) {

                                        processoIniciado.destroyForcibly();
                                    }

                                } catch (InterruptedException e) {

                                    Thread.currentThread().interrupt();
                                    processoIniciado.destroyForcibly();
                                }
                            }

                        }, "EncerrarAPI")
                );

            } else {

                carregamento.atualizarMensagem(
                        "API já está em execução. Conectando..."
                );
            }

            long limite = System.nanoTime()
                    + Duration.ofSeconds(60).toNanos();

            while (System.nanoTime() < limite) {

                if (apiEstaDisponivel()) {

                    SwingUtilities.invokeLater(() -> {

                        carregamento.dispose();

                        Tela_Login login = new Tela_Login();
                        login.setVisible(true);

                    });

                    return;
                }

                if (processoApi != null && !processoApi.isAlive()) {

                    throw new IllegalStateException(
                            "A API foi encerrada durante a inicialização. "
                            + "Consulte api/api.log."
                    );
                }

                Thread.sleep(1000);
            }

            throw new IllegalStateException(
                    "A API não respondeu dentro de 60 segundos. "
                    + "Consulte api/api.log."
            );

        } catch (Exception e) {

            String mensagem = e.getMessage() == null
                    ? e.toString()
                    : e.getMessage();

            SwingUtilities.invokeLater(() -> {

                carregamento.dispose();

                JOptionPane.showMessageDialog(
                        null,
                        mensagem,
                        "Não foi possível iniciar o sistema",
                        JOptionPane.ERROR_MESSAGE
                );

            });

            if (processoApi != null && processoApi.isAlive()) {
                processoApi.destroy();
            }
        }
    }

    private static Path localizarPastaApi() {

        Path diretorioAtual = Path.of(
                System.getProperty("user.dir")
        ).toAbsolutePath().normalize();

        Path[] candidatos = new Path[]{
            diretorioAtual.resolve("../api").normalize(),
            diretorioAtual.resolve("api").normalize()
        };

        for (Path candidato : candidatos) {

            if (Files.isDirectory(candidato)
                    && Files.isDirectory(candidato.resolve("src"))) {

                return candidato;
            }
        }

        throw new IllegalStateException(
                "Não encontrei a pasta api ao lado da pasta desktop.\n"
                + "Diretório atual: " + diretorioAtual
        );
    }

    private static java.util.List<String> comandoInicializacao(
            Path jarApi) {

        java.util.List<String> comando =
                new java.util.ArrayList<>();

        comando.add(
                Path.of(
                        System.getProperty("java.home"),
                        "bin",
                        "java"
                ).toString()
        );

        Path truststore = Path.of(
                System.getProperty("user.home"),
                "Downloads",
                "aiven-truststore.jks"
        );

        if (Files.isRegularFile(truststore)) {

            comando.add(
                    "-Djavax.net.ssl.trustStore=" + truststore
            );

            comando.add(
                    "-Djavax.net.ssl.trustStorePassword=changeit"
            );

            comando.add(
                    "-Djavax.net.ssl.trustStoreType=PKCS12"
            );
        }

        comando.add("-jar");
        comando.add(jarApi.toString());

        return comando;
    }

    private static boolean apiEstaDisponivel() {

        HttpURLConnection conexao = null;

        try {

            conexao = (HttpURLConnection) URI
                    .create(ENDPOINT_STATUS)
                    .toURL()
                    .openConnection();

            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(1000);
            conexao.setReadTimeout(1000);

            return conexao.getResponseCode()
                    == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {

            return false;

        } finally {

            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }
}
