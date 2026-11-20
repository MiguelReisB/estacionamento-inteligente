package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author henri
 */
public class FabricaConexao {

    // os dados do banco ficam no db.properties (nao vai pro git)
    // usa o db.properties.example como modelo
    private static Properties config;

    private static Properties carregarConfig() throws SQLException {
        if (config != null) {
            return config;
        }

        Properties props = new Properties();
        Path arquivo = Path.of(System.getProperty("db.config", "db.properties"));

        try {
            if (Files.exists(arquivo)) {
                try (InputStream in = Files.newInputStream(arquivo)) {
                    props.load(in);
                }
            } else {
                // se nao achou na pasta, tenta no resources
                try (InputStream in = FabricaConexao.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (in == null) {
                        throw new SQLException("Arquivo db.properties não encontrado. Copie o db.properties.example, "
                                + "renomeie para db.properties e coloque os dados do seu MySQL.", ErroBanco.ERRO_CONFIG);
                    }
                    props.load(in);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Não deu para ler o db.properties.", ErroBanco.ERRO_CONFIG, e);
        }

        if (props.getProperty("db.url", "").isBlank()) {
            throw new SQLException("Faltou preencher o db.url no db.properties.", ErroBanco.ERRO_CONFIG);
        }

        config = props;
        return config;
    }

    public static Connection getConexao() throws SQLException {
        Properties props = carregarConfig();
        return DriverManager.getConnection(props.getProperty("db.url"),
                props.getProperty("db.usuario", ""),
                props.getProperty("db.senha", ""));
    }

    // desativa o autoCommit pra controlar a transacao na mao (commit/rollback)
    public static Connection getConexaoTransacional() throws SQLException {
        Connection conexao = getConexao();
        conexao.setAutoCommit(false);
        return conexao;
    }

    public static void testarConexao() throws SQLException {
        try (Connection conexao = getConexao()) {
            conexao.isValid(2);
        }
    }
}
