package com.mycompany.mavenproject1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Tela exibida enquanto a aplicação verifica/inicia a API. */
public class Tela_Carregamento extends JFrame {

    private final JLabel mensagem = new JLabel("Preparando o sistema...", SwingConstants.CENTER);

    public Tela_Carregamento() {
        setTitle("Estacionamento Inteligente");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(450, 280);
        setResizable(false);

        JPanel painel = new JPanel(new BorderLayout(12, 18));
        painel.setBackground(new Color(245, 247, 250));
        painel.setBorder(BorderFactory.createEmptyBorder(35, 30, 35, 30));

        JLabel titulo = new JLabel("Estacionamento Inteligente", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(35, 55, 80));

        mensagem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mensagem.setForeground(new Color(75, 85, 99));

        JProgressBar progresso = new JProgressBar();
        progresso.setIndeterminate(true);
        progresso.setPreferredSize(new Dimension(300, 12));
        progresso.setBorderPainted(false);

        JPanel centro = new JPanel(new BorderLayout(8, 14));
        centro.setOpaque(false);
        centro.add(mensagem, BorderLayout.CENTER);
        centro.add(progresso, BorderLayout.SOUTH);

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(centro, BorderLayout.CENTER);
        setContentPane(painel);
        setLocationRelativeTo(null);
    }

    public void atualizarMensagem(String texto) {
        SwingUtilities.invokeLater(() -> mensagem.setText(texto));
    }
}
