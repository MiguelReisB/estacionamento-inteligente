package util;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converte erros técnicos do banco em mensagens amigáveis para o usuário.
 * O detalhe técnico vai para o log, não para a tela.
 */
public class ErroBanco {

    /** SQLState usado pela FabricaConexao para problemas no db.properties. */
    public static final String SQLSTATE_CONFIG = "CFG01";

    private static final Logger LOGGER = Logger.getLogger(ErroBanco.class.getName());

    private ErroBanco() {
    }

    public static String mensagemAmigavel(SQLException e) {
        LOGGER.log(Level.WARNING, "Erro de banco de dados", e);

        String estado = e.getSQLState();

        if (SQLSTATE_CONFIG.equals(estado)) {
            return e.getMessage();
        }
        if (estado != null && estado.startsWith("08")) {
            return "Não foi possível conectar ao banco de dados.\n"
                    + "Verifique se o MySQL está ligado e se o db.properties está correto.";
        }
        if (e.getErrorCode() == 1045) {
            return "Usuário ou senha do banco de dados incorretos (verifique o db.properties).";
        }
        if (e.getErrorCode() == 1049) {
            return "O banco de dados informado no db.properties não existe.";
        }
        if (e.getErrorCode() == 1146) {
            return "Tabela não encontrada no banco. Execute o script SQL da pasta database/.";
        }
        return "Ocorreu um erro ao acessar o banco de dados. Tente novamente.";
    }
}
