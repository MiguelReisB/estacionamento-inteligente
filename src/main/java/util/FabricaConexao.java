
package util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FabricaConexao {

// credenciais do banco
    private static final String URL = "jdbc:mysql://127.0.0.1:5014/teste";
    private static final String USUARIO = "remoto";
    private static final String SENHA = "Un@2026Un@";
    
    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
    
    public static Connection getConexaoTransacional() throws SQLException {
        Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
        // destiva o autoCommit "gravacao automatica" para controle de Transacao e concorrencia
        conexao.setAutoCommit(false);
        return conexao;
        
    }
    
}
