package com.mycompany.mavenproject1;

import javax.swing.JOptionPane;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        Tela_Login login = new Tela_Login();
        login.show();
        
    }
    
    /*(Teste de conexão do banco)
    try (var conexao = BancoDados.conectar()) {
        System.out.println("Banco conectado com sucesso!");
    } 
    catch (java.sql.SQLException e) {
        e.printStackTrace();
    }
    */
    
}



