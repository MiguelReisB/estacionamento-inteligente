package com.mycompany.mavenproject1;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.StatusTicket;
import model.Ticket;
import service.CalculadoraTarifa;
import service.SaidaVeiculoService;

/**
 * Saida de veiculo: consulta pela placa, mostra o recibo, paga e libera a vaga.
 *
 * @author henri
 */
public class PainelSaida extends JPanel implements Tela_Principal.Atualizavel {

    private static final String FORMULARIO = "formulario";
    private static final String RESULTADO = "resultado";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Tela_Principal principal;
    private final SaidaVeiculoService saidaService = new SaidaVeiculoService();
    private final CardLayout cartoes = new CardLayout();

    // formulario
    private final JTextField campoPlaca = Estilo.campoPlaca(40);
    private final JButton btnConsultar = Estilo.botaoPrimario("Consultar", 22);
    private final JLabel lblEntrada = valor();
    private final JLabel lblVaga = valor();
    private final JLabel lblTempo = valor();
    private final JLabel lblHoras = valor();
    private final JLabel lblTotal = Estilo.rotulo("-", Font.BOLD, 44);
    private final JLabel lblAviso = Estilo.rotulo(" ", Font.PLAIN, 16);
    private final JButton btnPagar = Estilo.botaoPerigo("Pagar", 24);

    // resultado
    private final JPanel painelResultado = Estilo.painel(new GridBagLayout());
    private final JLabel lblResumo = centralizado(20, Font.PLAIN);
    private final JLabel lblVagaLiberada = centralizado(18, Font.PLAIN);
    private final JLabel lblContagem = centralizado(13, Font.PLAIN);
    private final Timer relogio;
    private int segundos;

    // ticket consultado (null enquanto nao consultar)
    private Ticket ticketAtual;
    private boolean preenchendo;

    public PainelSaida(Tela_Principal principal) {
        this.principal = principal;
        setLayout(cartoes);
        setBackground(Estilo.FUNDO);

        add(montarFormulario(), FORMULARIO);
        add(montarResultado(), RESULTADO);

        relogio = new Timer(1000, e -> contar());
    }

    private JPanel montarFormulario() {
        JPanel tela = Estilo.painel(new GridBagLayout());
        tela.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel coluna = Estilo.painel(new GridBagLayout());

        JLabel titulo = Estilo.titulo("Saída de Veículo", 32);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel tarifa = Estilo.rotulo("R$ 5,00 por hora ou fração", Font.PLAIN, 18);
        tarifa.setForeground(Estilo.TEXTO_SECUNDARIO);
        tarifa.setHorizontalAlignment(SwingConstants.CENTER);

        // consulta
        JPanel consulta = Estilo.painel(new GridBagLayout());
        Estilo.bordaTitulada(consulta, "Consulta");
        Estilo.tamanho(campoPlaca, 460, 70);
        Estilo.tamanho(btnConsultar, 200, 70);
        JPanel linhaPlaca = Estilo.painel(new BorderLayout(14, 0));
        linhaPlaca.add(campoPlaca, BorderLayout.CENTER);
        linhaPlaca.add(btnConsultar, BorderLayout.EAST);
        consulta.add(Estilo.rotulo("Digite a Placa do Veículo", Font.PLAIN, 22), Estilo.linha(0, 4));
        consulta.add(linhaPlaca, Estilo.linha(1, 10));

        // recibo
        JPanel recibo = Estilo.painel(new GridBagLayout());
        Estilo.bordaTitulada(recibo, "Recibo");
        JPanel campos = Estilo.painel(new GridLayout(2, 2, 40, 8));
        campos.add(item("Entrada: ", lblEntrada));
        campos.add(item("Vaga: ", lblVaga));
        campos.add(item("Tempo: ", lblTempo));
        campos.add(item("Horas cobradas: ", lblHoras));

        JPanel total = Estilo.painel(new BorderLayout());
        JPanel textoTotal = Estilo.painel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        textoTotal.add(Estilo.rotulo("Total: ", Font.PLAIN, 24));
        textoTotal.add(lblTotal);
        Estilo.tamanho(btnPagar, 220, 64);
        total.add(textoTotal, BorderLayout.WEST);
        total.add(btnPagar, BorderLayout.EAST);

        lblAviso.setForeground(Estilo.ERRO);
        lblAviso.setHorizontalAlignment(SwingConstants.CENTER);

        recibo.add(campos, Estilo.linha(0, 4));
        recibo.add(new JSeparator(), Estilo.linha(1, 12));
        recibo.add(total, Estilo.linha(2, 12));

        int y = 0;
        coluna.add(titulo, Estilo.linha(y++, 0));
        coluna.add(tarifa, Estilo.linha(y++, 4));
        coluna.add(consulta, Estilo.linha(y++, 16));
        coluna.add(recibo, Estilo.linha(y++, 16));
        coluna.add(lblAviso, Estilo.linha(y++, 10));
        Estilo.tamanho(coluna, 720, 520);
        tela.add(coluna, new GridBagConstraints());

        btnConsultar.addActionListener(e -> consultar());
        campoPlaca.addActionListener(e -> consultar());
        btnPagar.addActionListener(e -> pagar());

        // se mudar a placa depois de consultar, o recibo antigo nao vale mais
        campoPlaca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                placaMudou();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                placaMudou();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                placaMudou();
            }
        });
        return tela;
    }

    private JPanel montarResultado() {
        JPanel tela = Estilo.painel(new GridBagLayout());
        tela.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel coluna = Estilo.painel(new GridBagLayout());

        JLabel titulo = Estilo.titulo("Saída de Veículo", 32);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel destaque = centralizado(56, Font.BOLD);
        destaque.setText("Saída liberada");
        destaque.setForeground(Estilo.SUCESSO);
        JLabel boaViagem = centralizado(22, Font.PLAIN);
        boaViagem.setText("Boa viagem!");

        JButton btnNova = Estilo.botaoPadrao("Nova saída", 18);
        Estilo.tamanho(btnNova, 260, 52);
        JPanel botao = Estilo.painel(new FlowLayout(FlowLayout.CENTER));
        botao.add(btnNova);
        lblVagaLiberada.setForeground(Estilo.TEXTO_SECUNDARIO);
        lblContagem.setForeground(Estilo.TEXTO_SECUNDARIO);

        Estilo.bordaResultado(painelResultado, "Pagamento confirmado", Estilo.SUCESSO);
        Estilo.tamanho(painelResultado, 720, 400);
        int y = 0;
        painelResultado.add(destaque, Estilo.linha(y++, 0));
        painelResultado.add(boaViagem, Estilo.linha(y++, 6));
        painelResultado.add(lblResumo, Estilo.linha(y++, 16));
        painelResultado.add(lblVagaLiberada, Estilo.linha(y++, 10));
        painelResultado.add(botao, Estilo.linha(y++, 14));
        painelResultado.add(lblContagem, Estilo.linha(y++, 6));

        coluna.add(titulo, Estilo.linha(0, 0));
        coluna.add(painelResultado, Estilo.linha(1, 16));
        tela.add(coluna, new GridBagConstraints());

        btnNova.addActionListener(e -> voltarAoFormulario());
        return tela;
    }

    @Override
    public void aoMostrar() {
        voltarAoFormulario();
    }

    @Override
    public void aoSair() {
        relogio.stop();
    }

    private void voltarAoFormulario() {
        relogio.stop();
        preenchendo = true;
        campoPlaca.setText("");
        preenchendo = false;
        limparRecibo();
        lblAviso.setText(" ");
        btnConsultar.setEnabled(true);
        cartoes.show(this, FORMULARIO);
        campoPlaca.requestFocusInWindow();
    }

    private void placaMudou() {
        if (!preenchendo && ticketAtual != null) {
            limparRecibo();
        }
    }

    private void limparRecibo() {
        ticketAtual = null;
        lblEntrada.setText("-");
        lblVaga.setText("-");
        lblTempo.setText("-");
        lblHoras.setText("-");
        lblTotal.setText("-");
        btnPagar.setText("Pagar");
        btnPagar.setEnabled(false);
    }

    private void consultar() {
        String placa = campoPlaca.getText();
        if (placa.isBlank()) {
            lblAviso.setForeground(Estilo.ERRO);
            lblAviso.setText("Digite a placa do veículo.");
            return;
        }

        btnConsultar.setEnabled(false);
        lblAviso.setText(" ");
        principal.status("Consultando ticket...");

        Tarefa.rodar(
                () -> saidaService.consultar(placa),
                ticket -> {
                    btnConsultar.setEnabled(true);
                    mostrarRecibo(ticket);
                    principal.status("Ticket " + ticket.getId() + " encontrado.");
                },
                erro -> {
                    btnConsultar.setEnabled(true);
                    limparRecibo();
                    lblAviso.setForeground(Estilo.ERRO);
                    lblAviso.setText(Tarefa.mensagem(erro));
                    principal.status(" ");
                });
    }

    private void mostrarRecibo(Ticket ticket) {
        ticketAtual = ticket;
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime entrada = ticket.getDataEntrada();

        lblEntrada.setText(entrada.toLocalDate().equals(agora.toLocalDate())
                ? entrada.format(HORA) : entrada.format(DATA_HORA));
        lblVaga.setText(ticket.getVagaIdentificacao());
        lblTempo.setText(tempo(entrada, agora));

        LocalDateTime fimCobranca = ticket.getDataPagamento() != null ? ticket.getDataPagamento() : agora;
        lblHoras.setText(String.valueOf(CalculadoraTarifa.horasCobradas(entrada, fimCobranca)));
        lblTotal.setText(dinheiro(ticket.getValor()));

        btnPagar.setEnabled(true);
        if (ticket.getStatus() == StatusTicket.PAGO) {
            btnPagar.setText("Liberar saída");
            lblAviso.setForeground(Estilo.TEXTO_SECUNDARIO);
            lblAviso.setText("Pagamento já registrado. Falta só liberar a saída.");
        } else {
            btnPagar.setText("Pagar");
            lblAviso.setText(" ");
        }
    }

    private void pagar() {
        if (ticketAtual == null) {
            return;
        }
        Ticket ticket = ticketAtual;
        boolean jaPago = ticket.getStatus() == StatusTicket.PAGO;

        if (!jaPago) {
            int opcao = JOptionPane.showConfirmDialog(this,
                    "Confirmar o pagamento de " + dinheiro(ticket.getValor()) + " da placa " + ticket.getPlaca() + "?",
                    "Pagamento", JOptionPane.YES_NO_OPTION);
            if (opcao != JOptionPane.YES_OPTION) {
                return;
            }
        }

        btnPagar.setEnabled(false);
        btnConsultar.setEnabled(false);
        principal.status("Registrando pagamento e saída...");

        Tarefa.rodar(
                () -> {
                    if (!jaPago) {
                        saidaService.pagar(ticket.getPlaca());
                    }
                    return saidaService.registrarSaida(ticket.getPlaca());
                },
                encerrado -> mostrarResultado(ticket, encerrado),
                erro -> {
                    // a situacao do ticket pode ter mudado (ex.: pagou mas a saida falhou),
                    // entao limpa o recibo e pede para consultar de novo
                    limparRecibo();
                    btnConsultar.setEnabled(true);
                    lblAviso.setForeground(Estilo.ERRO);
                    lblAviso.setText(Tarefa.mensagem(erro) + " Consulte a placa de novo.");
                    principal.status(" ");
                });
    }

    private void mostrarResultado(Ticket consultado, Ticket encerrado) {
        BigDecimal valorPago = encerrado.getValor() != null ? encerrado.getValor() : consultado.getValor();
        LocalDateTime saida = encerrado.getDataSaida() != null ? encerrado.getDataSaida() : LocalDateTime.now();

        lblResumo.setText("Placa: " + consultado.getPlaca() + "      Pago: " + dinheiro(valorPago)
                + "      Saída: " + saida.format(HORA));
        lblVagaLiberada.setText("Vaga " + consultado.getVagaIdentificacao() + " liberada.");
        principal.status("Saída registrada: " + consultado.getPlaca());

        segundos = 8;
        lblContagem.setText("Volta sozinho para a saída em " + segundos + " s");
        cartoes.show(this, RESULTADO);
        relogio.restart();
    }

    private void contar() {
        segundos--;
        if (segundos <= 0) {
            voltarAoFormulario();
        } else {
            lblContagem.setText("Volta sozinho para a saída em " + segundos + " s");
        }
    }

    static String tempo(LocalDateTime inicio, LocalDateTime fim) {
        long minutos = Math.max(0, Duration.between(inicio, fim).toMinutes());
        return (minutos / 60) + "h " + String.format("%02d", minutos % 60) + "min";
    }

    static String dinheiro(BigDecimal valor) {
        if (valor == null) {
            return "-";
        }
        return "R$ " + String.format("%.2f", valor).replace('.', ',');
    }

    private static JLabel valor() {
        JLabel rotulo = Estilo.rotulo("-", Font.BOLD, 20);
        // folga para o texto em negrito nao cortar a ultima letra
        rotulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        return rotulo;
    }

    private static JPanel item(String nome, JLabel valor) {
        JPanel linha = Estilo.painel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.add(Estilo.rotulo(nome, Font.PLAIN, 20));
        linha.add(valor);
        return linha;
    }

    private static JLabel centralizado(int tamanho, int estilo) {
        JLabel rotulo = Estilo.rotulo(" ", estilo, tamanho);
        rotulo.setHorizontalAlignment(SwingConstants.CENTER);
        return rotulo;
    }
}
