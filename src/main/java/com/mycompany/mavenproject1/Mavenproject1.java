package com.mycompany.mavenproject1;

import javax.swing.JOptionPane;
import java.sql.SQLException;

public class Mavenproject1 {

    public static void main(String[] args) {

        try {
            BancoDados.inicializar();
            
            Tela_Login login = new Tela_Login();
            login.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Erro ao conectar com o banco: " + e.getMessage()
            );
        }
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



