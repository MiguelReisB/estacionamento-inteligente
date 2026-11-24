package com.mycompany.mavenproject1;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Janela principal depois do login. A barra de ferramentas mostra
 * Painel, Entrada e Saída para todos; Vagas e Usuários so para administrador.
 *
 * @author henri
 */
public class Tela_Principal extends JFrame {

    public static final String PAINEL = "Painel";
    public static final String ENTRADA = "Entrada";
    public static final String SAIDA = "Saída";
    public static final String VAGAS = "Vagas";
    public static final String USUARIOS = "Usuários";

    // cada tela interna sabe se atualizar quando aparece
    public interface Atualizavel {
        void aoMostrar();

        default void aoSair() {
        }
    }

    private final CardLayout cartoes = new CardLayout();
    private final JPanel conteudo = new JPanel(cartoes);
    private final Map<String, JToggleButton> botoes = new LinkedHashMap<>();
    private final Map<String, Atualizavel> telas = new LinkedHashMap<>();
    private final JLabel barraStatus = Estilo.rotulo(" ", Font.PLAIN, 12);
    private String telaAtual;

    public Tela_Principal() {
        UsuarioInfo usuario = Sessao.getUsuario();

        setTitle("Estacionamento Inteligente");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(Estilo.LARGURA_JANELA, Estilo.ALTURA_JANELA);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Estilo.FUNDO);
        getContentPane().setLayout(new BorderLayout());

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setBackground(Estilo.FUNDO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        conteudo.setBackground(Estilo.FUNDO);
        ButtonGroup grupo = new ButtonGroup();

        adicionarTela(barra, grupo, PAINEL, new PainelInicio(this));
        adicionarTela(barra, grupo, ENTRADA, new PainelEntrada(this));
        adicionarTela(barra, grupo, SAIDA, new PainelSaida(this));

        // as telas de administrador nem sao criadas para o operador
        if (Sessao.isAdmin()) {
            JSeparator separador = new JSeparator(SwingConstants.VERTICAL);
            separador.setMaximumSize(new Dimension(12, 24));
            barra.add(Box.createHorizontalStrut(4));
            barra.add(separador);
            adicionarTela(barra, grupo, VAGAS, new PainelVagas(this));
            adicionarTela(barra, grupo, USUARIOS, new PainelUsuarios(this));
        }

        barra.add(Box.createHorizontalGlue());
        JLabel quem = Estilo.rotulo(usuario == null ? "" : usuario.nome() + " (" + usuario.perfilDescricao() + ")",
                Font.PLAIN, 13);
        quem.setForeground(Estilo.TEXTO_SECUNDARIO);
        quem.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        barra.add(quem);

        JButton btnSair = Estilo.botaoPadrao("Sair", 13);
        btnSair.addActionListener(e -> sair());
        barra.add(btnSair);

        barraStatus.setForeground(Estilo.TEXTO_SECUNDARIO);
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Estilo.FUNDO);
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        rodape.add(barraStatus, BorderLayout.WEST);

        getContentPane().add(barra, BorderLayout.NORTH);
        getContentPane().add(conteudo, BorderLayout.CENTER);
        getContentPane().add(rodape, BorderLayout.SOUTH);

        mostrar(PAINEL);
    }

    private void adicionarTela(JToolBar barra, ButtonGroup grupo, String nome, JPanel tela) {
        JToggleButton botao = Estilo.botaoBarra(nome);
        botao.addActionListener(e -> mostrar(nome));
        grupo.add(botao);
        barra.add(botao);
        barra.add(Box.createHorizontalStrut(2));
        botoes.put(nome, botao);
        telas.put(nome, (Atualizavel) tela);
        conteudo.add(tela, nome);
    }

    public void mostrar(String nome) {
        Atualizavel tela = telas.get(nome);
        if (tela == null) {
            return; // tela de admin pedida por quem nao e admin
        }
        if (telaAtual != null && !telaAtual.equals(nome)) {
            telas.get(telaAtual).aoSair();
        }
        telaAtual = nome;
        botoes.get(nome).setSelected(true);
        cartoes.show(conteudo, nome);
        tela.aoMostrar();
    }

    public void status(String texto) {
        barraStatus.setText(texto);
    }

    private void sair() {
        if (telaAtual != null) {
            telas.get(telaAtual).aoSair();
        }
        Sessao.encerrar();
        new Tela_Login().setVisible(true);
        dispose();
    }
}
