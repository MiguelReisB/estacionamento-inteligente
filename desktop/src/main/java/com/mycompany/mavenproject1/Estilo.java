package com.mycompany.mavenproject1;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.net.URL;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Cores, fontes e componentes padrao das telas, pra todas ficarem iguais.
 *
 * @author henri
 */
public final class Estilo {

    public static final int LARGURA_JANELA = 1200;
    public static final int ALTURA_JANELA = 760;

    public static final Color FUNDO = new Color(240, 240, 240);
    public static final Color TEXTO = new Color(30, 30, 30);
    public static final Color TEXTO_SECUNDARIO = new Color(68, 68, 68);

    public static final Color AZUL = new Color(36, 105, 224);
    public static final Color AZUL_BORDA = new Color(27, 79, 176);
    public static final Color VERMELHO = new Color(212, 45, 73);
    public static final Color VERMELHO_BORDA = new Color(168, 35, 58);
    public static final Color CINZA_BOTAO = new Color(225, 225, 225);
    public static final Color CINZA_BORDA = new Color(173, 173, 173);
    public static final Color BORDA_PAINEL = new Color(185, 185, 185);
    public static final Color BORDA_CAMPO = new Color(171, 173, 179);

    public static final Color SELECAO = new Color(204, 228, 247);
    public static final Color SELECAO_BORDA = new Color(153, 201, 239);

    public static final Color SUCESSO = new Color(46, 125, 50);
    public static final Color ERRO = new Color(198, 40, 40);

    public static final Color VAGA_LIVRE_FUNDO = new Color(216, 240, 216);
    public static final Color VAGA_LIVRE_TEXTO = new Color(30, 94, 30);
    public static final Color VAGA_OCUPADA_FUNDO = new Color(244, 211, 211);
    public static final Color VAGA_OCUPADA_TEXTO = new Color(122, 28, 28);

    private Estilo() {
    }

    public static Font fonte(int estilo, int tamanho) {
        return new Font("Segoe UI", estilo, tamanho);
    }

    public static JLabel rotulo(String texto, int estilo, int tamanho) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(fonte(estilo, tamanho));
        rotulo.setForeground(TEXTO);
        return rotulo;
    }

    public static JLabel titulo(String texto, int tamanho) {
        return rotulo(texto, Font.BOLD | Font.ITALIC, tamanho);
    }

    // ---------- botoes ----------

    public static JButton botaoPrimario(String texto, int tamanhoFonte) {
        return botao(texto, AZUL, AZUL_BORDA, Color.WHITE, tamanhoFonte);
    }

    public static JButton botaoPerigo(String texto, int tamanhoFonte) {
        return botao(texto, VERMELHO, VERMELHO_BORDA, Color.WHITE, tamanhoFonte);
    }

    public static JButton botaoPadrao(String texto, int tamanhoFonte) {
        return botao(texto, CINZA_BOTAO, CINZA_BORDA, TEXTO, tamanhoFonte);
    }

    // botao com cara de link (ex.: "Cadastrar Usuário" no login)
    public static JButton botaoLink(String texto, int tamanhoFonte) {
        JButton botao = new JButton(texto);
        botao.setFont(fonte(Font.PLAIN, tamanhoFonte));
        botao.setForeground(AZUL);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    private static JButton botao(String texto, Color fundo, Color borda, Color corTexto, int tamanhoFonte) {
        JButton botao = new JButton(texto);
        botao.setFont(fonte(Font.PLAIN, tamanhoFonte));
        botao.setForeground(corTexto);
        pintarBotao(botao, fundo, borda);
        return botao;
    }

    // o look and feel do windows ignora a cor de fundo do botao, por isso pinto na mao
    private static void pintarBotao(AbstractButton botao, Color fundo, Color borda) {
        botao.setContentAreaFilled(false);
        botao.setOpaque(true);
        botao.setFocusPainted(false);
        botao.setBackground(fundo);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borda),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        botao.getModel().addChangeListener(e -> {
            ButtonModel modelo = botao.getModel();
            if (!modelo.isEnabled()) {
                botao.setBackground(misturar(fundo, FUNDO, 0.45f));
            } else if (modelo.isPressed()) {
                botao.setBackground(escurecer(fundo, 0.82f));
            } else if (modelo.isRollover()) {
                botao.setBackground(escurecer(fundo, 0.92f));
            } else {
                botao.setBackground(fundo);
            }
        });
    }

    // botao grande de escolha (Carro / Moto)
    public static JToggleButton botaoEscolha(String texto, ImageIcon icone, int tamanhoFonte) {
        JToggleButton botao = new JToggleButton(texto, icone);
        botao.setFont(fonte(Font.PLAIN, tamanhoFonte));
        botao.setForeground(TEXTO);
        botao.setIconTextGap(14);
        botao.setContentAreaFilled(false);
        botao.setOpaque(true);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        atualizarEscolha(botao);
        botao.addItemListener(e -> atualizarEscolha(botao));
        return botao;
    }

    private static void atualizarEscolha(JToggleButton botao) {
        if (botao.isSelected()) {
            botao.setBackground(SELECAO);
            botao.setBorder(BorderFactory.createLineBorder(AZUL, 2));
        } else {
            botao.setBackground(CINZA_BOTAO);
            botao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CINZA_BORDA),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        }
    }

    // botao da barra de ferramentas
    public static JToggleButton botaoBarra(String texto) {
        JToggleButton botao = new JToggleButton(texto);
        botao.setFont(fonte(Font.PLAIN, 14));
        botao.setForeground(TEXTO);
        botao.setContentAreaFilled(false);
        botao.setOpaque(true);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        atualizarBarra(botao);
        botao.addItemListener(e -> atualizarBarra(botao));
        return botao;
    }

    private static void atualizarBarra(JToggleButton botao) {
        Border vazia = BorderFactory.createEmptyBorder(5, 12, 5, 12);
        if (botao.isSelected()) {
            botao.setBackground(SELECAO);
            botao.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SELECAO_BORDA), vazia));
        } else {
            botao.setBackground(FUNDO);
            botao.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FUNDO), vazia));
        }
    }

    // ---------- campos ----------

    public static JTextField campo(int tamanhoFonte) {
        JTextField campo = new JTextField();
        prepararCampo(campo, tamanhoFonte);
        return campo;
    }

    public static JPasswordField campoSenha(int tamanhoFonte) {
        JPasswordField campo = new JPasswordField();
        prepararCampo(campo, tamanhoFonte);
        return campo;
    }

    private static void prepararCampo(JTextField campo, int tamanhoFonte) {
        campo.setFont(fonte(Font.PLAIN, tamanhoFonte));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA_CAMPO),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
    }

    // campo grande da placa: so letras, numeros e traco, sempre maiusculo, ate 8 caracteres
    public static JTextField campoPlaca(int tamanhoFonte) {
        JTextField campo = campo(tamanhoFonte);
        campo.setFont(fonte(Font.BOLD, tamanhoFonte));
        campo.setHorizontalAlignment(JTextField.CENTER);
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr)
                    throws BadLocationException {
                replace(fb, offset, 0, texto, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int tamanho, String texto, AttributeSet attr)
                    throws BadLocationException {
                if (texto == null) {
                    texto = "";
                }
                String limpo = texto.toUpperCase().replaceAll("[^A-Z0-9-]", "");
                int espaco = 8 - (fb.getDocument().getLength() - tamanho);
                if (limpo.length() > espaco) {
                    limpo = limpo.substring(0, Math.max(0, espaco));
                }
                super.replace(fb, offset, tamanho, limpo, attr);
            }
        });
        return campo;
    }

    // ---------- paineis e tabelas ----------

    // painel com titulo na borda (TitledBorder)
    public static void bordaTitulada(JComponent componente, String titulo) {
        TitledBorder borda = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDA_PAINEL), titulo);
        borda.setTitleFont(fonte(Font.PLAIN, 13));
        borda.setTitleColor(TEXTO);
        componente.setBorder(BorderFactory.createCompoundBorder(borda, BorderFactory.createEmptyBorder(6, 12, 12, 12)));
    }

    // borda titulada colorida, usada no resultado da entrada/saida
    public static void bordaResultado(JComponent componente, String titulo, Color cor) {
        TitledBorder borda = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(cor, 2), titulo);
        borda.setTitleFont(fonte(Font.BOLD, 16));
        borda.setTitleColor(cor);
        componente.setBorder(BorderFactory.createCompoundBorder(borda, BorderFactory.createEmptyBorder(10, 24, 20, 24)));
    }

    public static JPanel painel(java.awt.LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setOpaque(false);
        return painel;
    }

    public static DefaultTableModel modeloTabela(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    public static JTable tabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setFont(fonte(Font.PLAIN, 13));
        tabela.setRowHeight(24);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setFont(fonte(Font.PLAIN, 13));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setGridColor(new Color(226, 226, 226));
        return tabela;
    }

    // ---------- outros ----------

    public static ImageIcon icone(String arquivo) {
        URL url = Estilo.class.getResource("/img/" + arquivo);
        return url == null ? null : new ImageIcon(url);
    }

    public static void tamanho(JComponent componente, int largura, int altura) {
        Dimension d = new Dimension(largura, altura);
        componente.setPreferredSize(d);
        componente.setMinimumSize(d);
    }

    // uma linha de um GridBagLayout em coluna
    public static GridBagConstraints linha(int y, int espacoAcima) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(espacoAcima, 0, 0, 0);
        return c;
    }

    private static Color escurecer(Color cor, float fator) {
        return new Color(
                Math.round(cor.getRed() * fator),
                Math.round(cor.getGreen() * fator),
                Math.round(cor.getBlue() * fator));
    }

    private static Color misturar(Color a, Color b, float pesoA) {
        float pesoB = 1 - pesoA;
        return new Color(
                Math.round(a.getRed() * pesoA + b.getRed() * pesoB),
                Math.round(a.getGreen() * pesoA + b.getGreen() * pesoB),
                Math.round(a.getBlue() * pesoA + b.getBlue() * pesoB));
    }
}
