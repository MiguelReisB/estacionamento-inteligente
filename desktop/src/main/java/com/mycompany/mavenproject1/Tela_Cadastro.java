package com.mycompany.mavenproject1;

import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * Cadastro feito a partir da tela de login.
 *
 * @author henri
 */
public class Tela_Cadastro extends JFrame {

    public Tela_Cadastro() {
        setTitle("Estacionamento Inteligente - Cadastro de Usuário");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(Estilo.LARGURA_JANELA, Estilo.ALTURA_JANELA);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Estilo.FUNDO);
        getContentPane().setLayout(new GridBagLayout());

        PainelCadastro formulario = new PainelCadastro(
                () -> {
                    JOptionPane.showMessageDialog(this,
                            "Usuário cadastrado com sucesso!\nAgora é só fazer o login.",
                            "Cadastro", JOptionPane.INFORMATION_MESSAGE);
                    voltarAoLogin();
                },
                this::voltarAoLogin);

        getContentPane().add(formulario);
        formulario.usarComoPadrao(getRootPane());
    }

    private void voltarAoLogin() {
        new Tela_Login().setVisible(true);
        dispose();
    }
}
