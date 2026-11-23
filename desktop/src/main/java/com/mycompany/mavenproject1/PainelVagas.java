package com.mycompany.mavenproject1;

import dao.TicketDAO;
import dao.VagaDAO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.Ticket;
import model.TipoVeiculo;
import model.Vaga;

/**
 * Vagas cadastradas e cadastro de vaga nova. So aparece para administrador.
 *
 * @author henri
 */
public class PainelVagas extends JPanel implements Tela_Principal.Atualizavel {

    private final Tela_Principal principal;
    private final VagaDAO vagaDAO = new VagaDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    private final JComboBox<String> filtroStatus = combo("Todas", "Livres", "Ocupadas");
    private final JComboBox<String> filtroTipo = combo("Todos", "Carro", "Moto");
    private final DefaultTableModel modelo = Estilo.modeloTabela("Vaga", "Tipo", "Status", "Placa");

    private final JTextField campoIdentificacao = Estilo.campo(14);
    private final JComboBox<String> campoTipo = combo("Carro", "Moto");
    private final JButton btnCadastrar = Estilo.botaoPrimario("Cadastrar", 14);
    private final JLabel lblMensagem = Estilo.rotulo(" ", Font.PLAIN, 13);

    private List<Vaga> vagas = new ArrayList<>();
    private Map<Integer, String> placaPorVaga = new HashMap<>();

    public PainelVagas(Tela_Principal principal) {
        super(new BorderLayout(0, 14));
        this.principal = principal;
        setBackground(Estilo.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // lista
        JPanel lista = Estilo.painel(new BorderLayout(0, 10));
        Estilo.bordaTitulada(lista, "Vagas cadastradas");
        JPanel filtros = Estilo.painel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(Estilo.rotulo("Mostrar:", Font.PLAIN, 14));
        filtros.add(filtroStatus);
        filtros.add(Estilo.rotulo("    Tipo:", Font.PLAIN, 14));
        filtros.add(filtroTipo);
        lista.add(filtros, BorderLayout.NORTH);
        lista.add(new JScrollPane(Estilo.tabela(modelo)), BorderLayout.CENTER);

        // nova vaga
        JPanel nova = Estilo.painel(new GridBagLayout());
        Estilo.bordaTitulada(nova, "Nova vaga");
        Estilo.tamanho(nova, 320, 300);
        Estilo.tamanho(campoIdentificacao, 280, 30);
        Estilo.tamanho(campoTipo, 280, 30);
        Estilo.tamanho(btnCadastrar, 280, 34);
        lblMensagem.setForeground(Estilo.ERRO);
        int y = 0;
        nova.add(Estilo.rotulo("Identificação", Font.PLAIN, 14), Estilo.linha(y++, 4));
        nova.add(campoIdentificacao, Estilo.linha(y++, 4));
        nova.add(Estilo.rotulo("Tipo", Font.PLAIN, 14), Estilo.linha(y++, 10));
        nova.add(campoTipo, Estilo.linha(y++, 4));
        nova.add(btnCadastrar, Estilo.linha(y++, 14));
        nova.add(lblMensagem, Estilo.linha(y++, 8));
        GridBagConstraints resto = Estilo.linha(y, 0);
        resto.weighty = 1;
        nova.add(Estilo.painel(null), resto);

        JPanel direita = Estilo.painel(new BorderLayout());
        direita.add(nova, BorderLayout.NORTH);

        JPanel centro = Estilo.painel(new BorderLayout(14, 0));
        centro.add(lista, BorderLayout.CENTER);
        centro.add(direita, BorderLayout.EAST);

        add(Estilo.titulo("Vagas", 20), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        filtroStatus.addActionListener(e -> preencherTabela());
        filtroTipo.addActionListener(e -> preencherTabela());
        btnCadastrar.addActionListener(e -> cadastrar());
        campoIdentificacao.addActionListener(e -> cadastrar());
    }

    @Override
    public void aoMostrar() {
        lblMensagem.setText(" ");
        carregar();
    }

    private void carregar() {
        principal.status("Carregando vagas...");
        Tarefa.rodar(
                () -> {
                    List<Vaga> lista = vagaDAO.listar();
                    Map<Integer, String> placas = new HashMap<>();
                    for (Ticket t : ticketDAO.listarAbertos()) {
                        placas.put(t.getVagaId(), t.getPlaca());
                    }
                    return new Dados(lista, placas);
                },
                dados -> {
                    vagas = dados.vagas();
                    placaPorVaga = dados.placas();
                    preencherTabela();
                    principal.status(vagas.size() + " vagas cadastradas.");
                },
                erro -> principal.status(Tarefa.mensagem(erro)));
    }

    private record Dados(List<Vaga> vagas, Map<Integer, String> placas) {
    }

    private void preencherTabela() {
        String status = (String) filtroStatus.getSelectedItem();
        String tipo = (String) filtroTipo.getSelectedItem();

        modelo.setRowCount(0);
        for (Vaga vaga : vagas) {
            if ("Livres".equals(status) && !vaga.isLivre()) {
                continue;
            }
            if ("Ocupadas".equals(status) && vaga.isLivre()) {
                continue;
            }
            if (!"Todos".equals(tipo) && !vaga.getTipo().getDescricao().equals(tipo)) {
                continue;
            }
            String placa = placaPorVaga.getOrDefault(vaga.getId(), "-");
            modelo.addRow(new Object[]{vaga.getIdentificacao(), vaga.getTipo().getDescricao(), vaga.getStatus(), placa});
        }
    }

    private void cadastrar() {
        String identificacao = campoIdentificacao.getText().trim().toUpperCase();
        TipoVeiculo tipo = "Moto".equals(campoTipo.getSelectedItem()) ? TipoVeiculo.MOTO : TipoVeiculo.CARRO;

        if (identificacao.isEmpty()) {
            erro("Informe a identificação da vaga.");
            return;
        }
        if (identificacao.length() > 10 || !identificacao.matches("[A-Z0-9-]+")) {
            erro("Use até 10 letras, números ou traço (ex.: C11).");
            return;
        }

        btnCadastrar.setEnabled(false);
        Tarefa.rodar(
                () -> {
                    vagaDAO.cadastrar(identificacao, tipo);
                    return identificacao;
                },
                ok -> {
                    btnCadastrar.setEnabled(true);
                    campoIdentificacao.setText("");
                    lblMensagem.setForeground(Estilo.SUCESSO);
                    lblMensagem.setText("Vaga " + ok + " cadastrada.");
                    carregar();
                },
                e -> {
                    btnCadastrar.setEnabled(true);
                    if (e instanceof SQLException sql && sql.getErrorCode() == 1062) {
                        erro("Já existe uma vaga " + identificacao + ".");
                    } else {
                        erro(Tarefa.mensagem(e));
                    }
                });
    }

    private void erro(String texto) {
        lblMensagem.setForeground(Estilo.ERRO);
        lblMensagem.setText("<html>" + texto + "</html>");
    }

    private static JComboBox<String> combo(String... opcoes) {
        JComboBox<String> combo = new JComboBox<>(opcoes);
        combo.setFont(Estilo.fonte(Font.PLAIN, 13));
        return combo;
    }
}
