package com.mycompany.mavenproject1;

import javax.swing.JOptionPane;
import java.sql.SQLException;
import util.ErroBanco;
import util.FabricaConexao;

public class Mavenproject1 {

    public static void main(String[] args) {

        try {
            FabricaConexao.testarConexao(); // testa o banco antes de abrir o login

            Tela_Login login = new Tela_Login();
            login.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, ErroBanco.mensagemAmigavel(e));
        }
    }
}
