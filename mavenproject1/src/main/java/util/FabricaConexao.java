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
 * Fábrica de conexões com o MySQL.
 *
 * As credenciais NÃO ficam no código: são lidas do arquivo db.properties,
 * que fica fora do Git (veja db.properties.example).
 *
 * Ordem de busca do arquivo:
 *   1. caminho informado em -Ddb.config=...
 *   2. ./db.properties (pasta onde o programa é executado)
 *   3. db.properties dentro do classpath (src/main/resources)
 */
public class FabricaConexao {

    private static final String ARQUIVO = "db.properties";

    private static Properties config;

    private FabricaConexao() {
    }

    private static synchronized Properties getConfig() throws SQLException {
        if (config != null) {
            return config;
        }

        Properties props = new Properties();
        String caminho = System.getProperty("db.config", ARQUIVO);
        Path arquivo = Path.of(caminho);

        try {
            if (Files.exists(arquivo)) {
                try (InputStream in = Files.newInputStream(arquivo)) {
                    props.load(in);
                }
            } else {
                try (InputStream in = FabricaConexao.class.getClassLoader().getResourceAsStream(ARQUIVO)) {
                    if (in == null) {
                        throw new SQLException(
                                "Arquivo de configuração '" + ARQUIVO + "' não encontrado. "
                                + "Copie db.properties.example para db.properties e preencha os dados do MySQL.",
                                ErroBanco.SQLSTATE_CONFIG);
                    }
                    props.load(in);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Não foi possível ler o arquivo " + ARQUIVO + ".", ErroBanco.SQLSTATE_CONFIG, e);
        }

        if (props.getProperty("db.url", "").isBlank()) {
            throw new SQLException("A propriedade 'db.url' não foi preenchida no " + ARQUIVO + ".",
                    ErroBanco.SQLSTATE_CONFIG);
        }

        config = props;
        return config;
    }

    /** Conexão comum (autoCommit ligado). */
    public static Connection getConexao() throws SQLException {
        Properties props = getConfig();
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.usuario", ""),
                props.getProperty("db.senha", ""));
    }

    /**
     * Conexão para transações: autoCommit desligado, quem chama
     * é responsável por fazer commit() ou rollback().
     */
    public static Connection getConexaoTransacional() throws SQLException {
        Connection conexao = getConexao();
        conexao.setAutoCommit(false);
        return conexao;
    }

    /** Abre e fecha uma conexão só para verificar se o banco está acessível. */
    public static void testarConexao() throws SQLException {
        try (Connection conexao = getConexao()) {
            conexao.isValid(2);
        }
    }
}
