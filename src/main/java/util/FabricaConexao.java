
package util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FabricaConexao {

// credenciais do banco
    private static final String URL = "jdbc:mysql://0.tcp.sa.ngrok.io:14279/Estacionamento?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000";
    private static final String USUARIO = "admin";
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
