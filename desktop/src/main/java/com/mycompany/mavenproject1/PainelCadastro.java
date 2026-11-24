package com.mycompany.mavenproject1;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Formulario de cadastro de usuario. E usado na tela de cadastro (vindo do
 * login) e na janela "Novo usuário" da tela de Usuários do administrador.
 * Toda conta nova nasce como operador.
 *
 * @author henri
 */
public class PainelCadastro extends JPanel {

    private final JTextField campoNome = Estilo.campo(14);
    private final JTextField campoEmail = Estilo.campo(14);
    private final JPasswordField campoSenha = Estilo.campoSenha(14);
    private final JPasswordField campoConfirma = Estilo.campoSenha(14);
    private final JLabel lblMensagem = Estilo.rotulo(" ", Font.PLAIN, 13);
    private final JButton btnCadastrar = Estilo.botaoPrimario("Cadastrar", 14);
    private final JButton btnVoltar = Estilo.botaoPadrao("Voltar", 14);

    private final Runnable aoCadastrar;

    public PainelCadastro(Runnable aoCadastrar, Runnable aoVoltar) {
        super(new GridBagLayout());
        this.aoCadastrar = aoCadastrar;
        setOpaque(false);
        Estilo.tamanho(this, 380, 420);

        JLabel titulo = Estilo.rotulo("CADASTRO DE USUÁRIO", Font.PLAIN, 24);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        for (JTextField campo : new JTextField[]{campoNome, campoEmail, campoSenha, campoConfirma}) {
            Estilo.tamanho(campo, 380, 30);
        }

        JLabel aviso = Estilo.rotulo("A conta é criada com perfil de operador.", Font.PLAIN, 13);
        aviso.setForeground(Estilo.TEXTO_SECUNDARIO);
        lblMensagem.setForeground(Estilo.ERRO);

        Estilo.tamanho(btnCadastrar, 110, 34);
        Estilo.tamanho(btnVoltar, 110, 34);
        JPanel botoes = Estilo.painel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoes.add(btnCadastrar);
        botoes.add(btnVoltar);

        int y = 0;
        add(titulo, Estilo.linha(y++, 0));
        add(Estilo.rotulo("Nome de Usuário", Font.PLAIN, 14), Estilo.linha(y++, 16));
        add(campoNome, Estilo.linha(y++, 4));
        add(Estilo.rotulo("E-mail", Font.PLAIN, 14), Estilo.linha(y++, 10));
        add(campoEmail, Estilo.linha(y++, 4));
        add(Estilo.rotulo("Senha", Font.PLAIN, 14), Estilo.linha(y++, 10));
        add(campoSenha, Estilo.linha(y++, 4));
        add(Estilo.rotulo("Confirme sua senha", Font.PLAIN, 14), Estilo.linha(y++, 10));
        add(campoConfirma, Estilo.linha(y++, 4));
        add(aviso, Estilo.linha(y++, 10));
        add(lblMensagem, Estilo.linha(y++, 6));
        add(botoes, Estilo.linha(y++, 8));

        btnCadastrar.addActionListener(e -> cadastrar());
        btnVoltar.addActionListener(e -> aoVoltar.run());
    }

    // deixa o Enter acionar o botao Cadastrar na janela onde o painel estiver
    public void usarComoPadrao(JRootPane raiz) {
        raiz.setDefaultButton(btnCadastrar);
    }

    private void cadastrar() {
        String nome = campoNome.getText().trim();
        String email = campoEmail.getText().trim();
        String senha = new String(campoSenha.getPassword());
        String confirma = new String(campoConfirma.getPassword());

        String problema = validar(nome, email, senha, confirma);
        if (problema != null) {
            mostrarErro(problema);
            return;
        }

        btnCadastrar.setEnabled(false);
        lblMensagem.setForeground(Estilo.TEXTO_SECUNDARIO);
        lblMensagem.setText("Cadastrando...");

        Tarefa.rodar(
                () -> UsuarioApiClient.cadastrar(nome, email, senha),
                status -> {
                    btnCadastrar.setEnabled(true);
                    if (status == 201) {
                        lblMensagem.setText(" ");
                        aoCadastrar.run();
                    } else if (status == 409) {
                        mostrarErro("Este e-mail já está cadastrado.");
                    } else if (status == 400) {
                        mostrarErro("Preencha todos os campos.");
                    } else {
                        mostrarErro("Não foi possível cadastrar (código " + status + ").");
                    }
                },
                erro -> {
                    btnCadastrar.setEnabled(true);
                    mostrarErro(Tarefa.mensagem(erro));
                });
    }

    static String validar(String nome, String email, String senha, String confirma) {
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirma.isEmpty()) {
            return "Preencha todos os campos.";
        }
        if (nome.length() > 50) {
            return "O nome pode ter no máximo 50 caracteres.";
        }
        if (email.length() > 100 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return "Informe um e-mail válido.";
        }
        if (senha.length() < 6) {
            return "A senha precisa ter pelo menos 6 caracteres.";
        }
        if (!senha.equals(confirma)) {
            return "As senhas não coincidem.";
        }
        return null;
    }

    private void mostrarErro(String texto) {
        lblMensagem.setForeground(Estilo.ERRO);
        lblMensagem.setText(texto);
    }
}
