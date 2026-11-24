package com.mycompany.mavenproject1;

import dao.TicketDAO;
import dao.VagaDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Ticket;
import model.TipoVeiculo;
import model.Vaga;

/**
 * Painel inicial: situacao do estacionamento, mapa de vagas e carros no patio.
 *
 * @author henri
 */
public class PainelInicio extends JPanel implements Tela_Principal.Atualizavel {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final Tela_Principal principal;
    private final VagaDAO vagaDAO = new VagaDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    private final JLabel lblEstacionados = valor();
    private final JLabel lblDisponiveis = valor();
    private final JLabel lblCarros = valor();
    private final JLabel lblMotos = valor();
    private final JPanel mapa = Estilo.painel(new GridLayout(0, 5, 6, 6));
    private final DefaultTableModel modeloPatio = Estilo.modeloTabela("Placa", "Tipo", "Vaga", "Entrada", "Status");

    public PainelInicio(Tela_Principal principal) {
        super(new BorderLayout(0, 14));
        this.principal = principal;
        setBackground(Estilo.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // titulo + botao atualizar
        JPanel topo = Estilo.painel(new BorderLayout());
        topo.add(Estilo.titulo("Estacionamento", 20), BorderLayout.WEST);
        JButton btnAtualizar = Estilo.botaoPadrao("Atualizar", 13);
        btnAtualizar.addActionListener(e -> aoMostrar());
        topo.add(btnAtualizar, BorderLayout.EAST);

        // situacao + botoes ENTRADA / SAIDA
        JPanel situacao = Estilo.painel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        Estilo.bordaTitulada(situacao, "Situação");
        situacao.add(metrica("Veículos Estacionados", lblEstacionados));
        situacao.add(metrica("Vagas Disponíveis", lblDisponiveis));
        situacao.add(metrica("Carros livres", lblCarros));
        situacao.add(metrica("Motos livres", lblMotos));

        JButton btnEntrada = Estilo.botaoPrimario("ENTRADA", 15);
        JButton btnSaida = Estilo.botaoPerigo("SAÍDA", 15);
        Estilo.tamanho(btnEntrada, 180, 44);
        Estilo.tamanho(btnSaida, 180, 44);
        btnEntrada.addActionListener(e -> principal.mostrar(Tela_Principal.ENTRADA));
        btnSaida.addActionListener(e -> principal.mostrar(Tela_Principal.SAIDA));
        JPanel atalhos = Estilo.painel(new GridLayout(2, 1, 0, 10));
        atalhos.add(btnEntrada);
        atalhos.add(btnSaida);

        JPanel linhaSituacao = Estilo.painel(new BorderLayout(14, 0));
        linhaSituacao.add(situacao, BorderLayout.CENTER);
        linhaSituacao.add(atalhos, BorderLayout.EAST);

        JPanel cima = Estilo.painel(new BorderLayout(0, 14));
        cima.add(topo, BorderLayout.NORTH);
        cima.add(linhaSituacao, BorderLayout.CENTER);

        // mapa de vagas
        JPanel painelMapa = Estilo.painel(new BorderLayout(0, 10));
        Estilo.bordaTitulada(painelMapa, "Mapa de vagas");
        JPanel mapaTopo = Estilo.painel(new BorderLayout());
        mapaTopo.add(mapa, BorderLayout.NORTH);
        JScrollPane rolagemMapa = new JScrollPane(mapaTopo);
        rolagemMapa.setBorder(null);
        rolagemMapa.getViewport().setOpaque(false);
        rolagemMapa.setOpaque(false);
        painelMapa.add(rolagemMapa, BorderLayout.CENTER);
        JLabel legenda = Estilo.rotulo("Verde: livre     Vermelho: ocupada", Font.PLAIN, 12);
        legenda.setForeground(Estilo.TEXTO_SECUNDARIO);
        painelMapa.add(legenda, BorderLayout.SOUTH);

        // carros no patio
        JPanel painelPatio = Estilo.painel(new BorderLayout());
        Estilo.bordaTitulada(painelPatio, "Veículos no pátio");
        JTable tabela = Estilo.tabela(modeloPatio);
        painelPatio.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel baixo = Estilo.painel(new GridLayout(1, 2, 14, 0));
        baixo.add(painelMapa);
        baixo.add(painelPatio);

        add(cima, BorderLayout.NORTH);
        add(baixo, BorderLayout.CENTER);
    }

    @Override
    public void aoMostrar() {
        principal.status("Carregando dados...");
        Tarefa.rodar(
                () -> new Dados(vagaDAO.listar(), ticketDAO.listarAbertos()),
                this::preencher,
                erro -> principal.status(Tarefa.mensagem(erro)));
    }

    private record Dados(List<Vaga> vagas, List<Ticket> abertos) {
    }

    private void preencher(Dados dados) {
        int livres = 0;
        int carrosLivres = 0;
        int carrosTotal = 0;
        int motosLivres = 0;
        int motosTotal = 0;

        mapa.removeAll();
        for (Vaga vaga : dados.vagas()) {
            boolean livre = vaga.isLivre();
            if (vaga.getTipo() == TipoVeiculo.CARRO) {
                carrosTotal++;
                if (livre) {
                    carrosLivres++;
                }
            } else {
                motosTotal++;
                if (livre) {
                    motosLivres++;
                }
            }
            if (livre) {
                livres++;
            }
            mapa.add(celula(vaga.getIdentificacao(), livre));
        }

        lblEstacionados.setText(String.valueOf(dados.vagas().size() - livres));
        lblDisponiveis.setText(String.valueOf(livres));
        lblCarros.setText(carrosLivres + " / " + carrosTotal);
        lblMotos.setText(motosLivres + " / " + motosTotal);

        modeloPatio.setRowCount(0);
        LocalDateTime hoje = LocalDateTime.now();
        for (Ticket t : dados.abertos()) {
            LocalDateTime entrada = t.getDataEntrada();
            String quando = entrada.toLocalDate().equals(hoje.toLocalDate())
                    ? entrada.format(HORA) : entrada.format(DATA_HORA);
            modeloPatio.addRow(new Object[]{
                t.getPlaca(), t.getTipoVeiculo().getDescricao(), t.getVagaIdentificacao(), quando, t.getStatus().name()
            });
        }

        mapa.revalidate();
        mapa.repaint();
        principal.status("Dados atualizados às " + LocalDateTime.now().format(HORA));
    }

    private static JLabel celula(String identificacao, boolean livre) {
        JLabel celula = new JLabel(identificacao, SwingConstants.CENTER);
        celula.setFont(Estilo.fonte(Font.BOLD, 13));
        celula.setOpaque(true);
        celula.setBackground(livre ? Estilo.VAGA_LIVRE_FUNDO : Estilo.VAGA_OCUPADA_FUNDO);
        celula.setForeground(livre ? Estilo.VAGA_LIVRE_TEXTO : Estilo.VAGA_OCUPADA_TEXTO);
        celula.setBorder(BorderFactory.createLineBorder(new java.awt.Color(158, 158, 158)));
        celula.setPreferredSize(new Dimension(80, 46));
        celula.setToolTipText(identificacao + (livre ? " - livre" : " - ocupada"));
        return celula;
    }

    private static JLabel valor() {
        JLabel rotulo = Estilo.rotulo("-", Font.BOLD | Font.ITALIC, 28);
        // folga para o italico nao cortar a ultima letra
        rotulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        return rotulo;
    }

    private static JPanel metrica(String nome, JLabel valor) {
        JPanel bloco = Estilo.painel(null);
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.add(Estilo.rotulo(nome, Font.PLAIN, 14));
        bloco.add(valor);
        return bloco;
    }
}
