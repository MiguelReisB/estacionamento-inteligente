package util;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henri
 */
public class ErroBanco {

    // codigo que eu uso quando o problema e no db.properties
    public static final String ERRO_CONFIG = "CFG01";

    private static final Logger logger = Logger.getLogger(ErroBanco.class.getName());

    // transforma o erro do banco numa mensagem que o usuario entende
    // o erro completo vai pro log
    public static String mensagemAmigavel(SQLException e) {
        logger.log(Level.WARNING, "Erro no banco", e);

        String estado = e.getSQLState();

        if (ERRO_CONFIG.equals(estado)) {
            return e.getMessage();
        }
        if (estado != null && estado.startsWith("08")) { // 08xxx = falha de conexao
            return "Não foi possível conectar ao banco de dados.\nVeja se o MySQL está ligado e se o db.properties está certo.";
        }

        switch (e.getErrorCode()) {
            case 1045:
                return "Usuário ou senha do banco incorretos, confira o db.properties.";
            case 1049:
                return "O banco informado no db.properties não existe.";
            case 1146:
                return "Tabela não encontrada. Rode os scripts da pasta database.";
            default:
                return "Erro ao acessar o banco de dados, tente novamente.";
        }
    }
}
