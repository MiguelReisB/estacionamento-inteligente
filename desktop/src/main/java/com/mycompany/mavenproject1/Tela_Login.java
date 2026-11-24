package com.mycompany.mavenproject1;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Tela de login. Todo mundo entra por aqui; o perfil do usuario
 * (administrador ou operador) decide o que aparece depois.
 *
 * @author henri
 */
public class Tela_Login extends JFrame {

    private final JTextField campoEmail = Estilo.campo(14);
    private final JPasswordField campoSenha = Estilo.campoSenha(14);
    private final JLabel lblMensagem = Estilo.rotulo(" ", Font.PLAIN, 13);
    private final JButton btnLogin = Estilo.botaoPrimario("Login", 14);

    public Tela_Login() {
        setTitle("Estacionamento Inteligente - Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(Estilo.LARGURA_JANELA, Estilo.ALTURA_JANELA);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Estilo.FUNDO);
        getContentPane().setLayout(new GridBagLayout());

        JPanel formulario = Estilo.painel(new GridBagLayout());
        Estilo.tamanho(formulario, 380, 330);

        JLabel titulo = Estilo.rotulo("LOGIN DE USUÁRIO", Font.PLAIN, 24);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        Estilo.tamanho(campoEmail, 380, 30);
        Estilo.tamanho(campoSenha, 380, 30);
        lblMensagem.setForeground(Estilo.ERRO);
        lblMensagem.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnSair = Estilo.botaoPerigo("Sair", 14);
        Estilo.tamanho(btnLogin, 110, 34);
        Estilo.tamanho(btnSair, 110, 34);
        JPanel botoes = Estilo.painel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        botoes.add(btnLogin);
        botoes.add(btnSair);

        JButton btnCadastrar = Estilo.botaoLink("Cadastrar Usuário", 14);

        int y = 0;
        formulario.add(titulo, Estilo.linha(y++, 0));
        formulario.add(Estilo.rotulo("E-mail", Font.PLAIN, 14), Estilo.linha(y++, 18));
        formulario.add(campoEmail, Estilo.linha(y++, 4));
        formulario.add(Estilo.rotulo("Senha", Font.PLAIN, 14), Estilo.linha(y++, 12));
        formulario.add(campoSenha, Estilo.linha(y++, 4));
        formulario.add(lblMensagem, Estilo.linha(y++, 8));
        formulario.add(botoes, Estilo.linha(y++, 8));
        GridBagConstraints link = Estilo.linha(y++, 10);
        link.fill = GridBagConstraints.NONE;
        formulario.add(btnCadastrar, link);

        getContentPane().add(formulario);
        getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> entrar());
        btnSair.addActionListener(e -> System.exit(0));
        btnCadastrar.addActionListener(e -> {
            new Tela_Cadastro().setVisible(true);
            dispose();
        });
    }

    private void entrar() {
        String email = campoEmail.getText().trim();
        String senha = new String(campoSenha.getPassword());

        if (email.isEmpty() || senha.isEmpty()) {
            lblMensagem.setText("Informe o e-mail e a senha.");
            return;
        }

        btnLogin.setEnabled(false);
        lblMensagem.setForeground(Estilo.TEXTO_SECUNDARIO);
        lblMensagem.setText("Entrando...");

        Tarefa.rodar(
                () -> UsuarioApiClient.autenticar(email, senha),
                usuario -> {
                    btnLogin.setEnabled(true);
                    lblMensagem.setForeground(Estilo.ERRO);
                    if (usuario == null) {
                        lblMensagem.setText("E-mail ou senha inválidos.");
                        campoSenha.setText("");
                        campoSenha.requestFocusInWindow();
                        return;
                    }
                    Sessao.iniciar(usuario);
                    new Tela_Principal().setVisible(true);
                    dispose();
                },
                erro -> {
                    btnLogin.setEnabled(true);
                    lblMensagem.setForeground(Estilo.ERRO);
                    lblMensagem.setText(Tarefa.mensagem(erro));
                });
    }
}
