package com.mycompany.mavenproject1;

import dao.VagaDAO;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import model.TipoVeiculo;
import model.Vaga;
import service.EntradaVeiculoService;
import service.EntradaVeiculoService.ResultadoEntrada;

/**
 * Entrada de veiculo. Mostra o formulario e, depois de registrar, o resultado
 * (vaga liberada ou lotado). O resultado volta sozinho para o formulario.
 *
 * @author henri
 */
public class PainelEntrada extends JPanel implements Tela_Principal.Atualizavel {

    private static final String FORMULARIO = "formulario";
    private static final String RESULTADO = "resultado";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final Tela_Principal principal;
    private final EntradaVeiculoService entradaService = new EntradaVeiculoService();
    private final VagaDAO vagaDAO = new VagaDAO();

    private final CardLayout cartoes = new CardLayout();

    // formulario
    private final JLabel lblLivres = Estilo.rotulo(" ", Font.PLAIN, 18);
    private final JToggleButton btnCarro = Estilo.botaoEscolha("Carro", Estilo.icone("carro.png"), 24);
    private final JToggleButton btnMoto = Estilo.botaoEscolha("Moto", Estilo.icone("moto.png"), 24);
    private final JTextField campoPlaca = Estilo.campoPlaca(44);
    private final JButton btnAdicionar = Estilo.botaoPrimario("Adicionar Veículo", 24);
    private final JLabel lblErro = Estilo.rotulo(" ", Font.PLAIN, 16);

    // resultado
    private final JPanel painelResultado = Estilo.painel(new GridBagLayout());
    private final JLabel lblLinha1 = centralizado(26, Font.PLAIN);
    private final JLabel lblDestaque = centralizado(140, Font.BOLD);
    private final JLabel lblDetalhes = centralizado(20, Font.PLAIN);
    private final JLabel lblObservacao = centralizado(18, Font.PLAIN);
    private final JButton btnVoltar = Estilo.botaoPadrao("Nova entrada", 18);
    private final JLabel lblContagem = centralizado(13, Font.PLAIN);
    private final Timer relogio;
    private int segundos;

    public PainelEntrada(Tela_Principal principal) {
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

        JLabel titulo = Estilo.titulo("Entrada de Veículo", 32);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLivres.setForeground(Estilo.TEXTO_SECUNDARIO);
        lblLivres.setHorizontalAlignment(SwingConstants.CENTER);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(btnCarro);
        grupo.add(btnMoto);
        JPanel tipos = Estilo.painel(new GridLayout(1, 2, 20, 0));
        Estilo.tamanho(btnCarro, 300, 90);
        Estilo.tamanho(btnMoto, 300, 90);
        tipos.add(btnCarro);
        tipos.add(btnMoto);

        Estilo.tamanho(campoPlaca, 640, 76);
        Estilo.tamanho(btnAdicionar, 640, 64);
        lblErro.setForeground(Estilo.ERRO);
        lblErro.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel dados = Estilo.painel(new GridBagLayout());
        Estilo.bordaTitulada(dados, "Dados do veículo");
        int y = 0;
        dados.add(Estilo.rotulo("Tipo de Veículo:", Font.PLAIN, 22), Estilo.linha(y++, 4));
        dados.add(tipos, Estilo.linha(y++, 10));
        dados.add(Estilo.rotulo("Digite a Placa do Veículo", Font.PLAIN, 22), Estilo.linha(y++, 18));
        dados.add(campoPlaca, Estilo.linha(y++, 10));
        dados.add(lblErro, Estilo.linha(y++, 8));
        dados.add(btnAdicionar, Estilo.linha(y++, 8));

        y = 0;
        coluna.add(titulo, Estilo.linha(y++, 0));
        coluna.add(lblLivres, Estilo.linha(y++, 4));
        coluna.add(dados, Estilo.linha(y++, 16));

        tela.add(coluna, new GridBagConstraints());

        btnAdicionar.addActionListener(e -> registrar());
        campoPlaca.addActionListener(e -> registrar());
        return tela;
    }

    private JPanel montarResultado() {
        JPanel tela = Estilo.painel(new GridBagLayout());
        tela.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel coluna = Estilo.painel(new GridBagLayout());
        JLabel titulo = Estilo.titulo("Entrada de Veículo", 32);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        Estilo.tamanho(painelResultado, 720, 470);
        JPanel botao = Estilo.painel(new FlowLayout(FlowLayout.CENTER));
        Estilo.tamanho(btnVoltar, 260, 52);
        botao.add(btnVoltar);
        lblContagem.setForeground(Estilo.TEXTO_SECUNDARIO);
        lblObservacao.setForeground(Estilo.TEXTO_SECUNDARIO);

        int y = 0;
        painelResultado.add(lblLinha1, Estilo.linha(y++, 0));
        painelResultado.add(lblDestaque, Estilo.linha(y++, 4));
        painelResultado.add(lblDetalhes, Estilo.linha(y++, 8));
        painelResultado.add(lblObservacao, Estilo.linha(y++, 10));
        painelResultado.add(botao, Estilo.linha(y++, 14));
        painelResultado.add(lblContagem, Estilo.linha(y++, 6));

        coluna.add(titulo, Estilo.linha(0, 0));
        coluna.add(painelResultado, Estilo.linha(1, 16));
        tela.add(coluna, new GridBagConstraints());

        btnVoltar.addActionListener(e -> voltarAoFormulario());
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
        campoPlaca.setText("");
        lblErro.setText(" ");
        btnAdicionar.setEnabled(true);
        cartoes.show(this, FORMULARIO);
        atualizarLivres();
        campoPlaca.requestFocusInWindow();
    }

    private void atualizarLivres() {
        Tarefa.rodar(
                vagaDAO::listar,
                vagas -> lblLivres.setText("Carros livres: " + livres(vagas, TipoVeiculo.CARRO)
                        + " · Motos livres: " + livres(vagas, TipoVeiculo.MOTO)),
                erro -> lblLivres.setText(Tarefa.mensagem(erro)));
    }

    private static int livres(List<Vaga> vagas, TipoVeiculo tipo) {
        int total = 0;
        for (Vaga vaga : vagas) {
            if (vaga.getTipo() == tipo && vaga.isLivre()) {
                total++;
            }
        }
        return total;
    }

    private void registrar() {
        if (!btnAdicionar.isEnabled()) {
            return;
        }
        TipoVeiculo tipo = btnCarro.isSelected() ? TipoVeiculo.CARRO
                : btnMoto.isSelected() ? TipoVeiculo.MOTO : null;
        String placa = campoPlaca.getText();

        if (tipo == null) {
            lblErro.setText("Selecione o tipo de veículo (Carro ou Moto).");
            return;
        }
        if (placa.isBlank()) {
            lblErro.setText("Digite a placa do veículo.");
            campoPlaca.requestFocusInWindow();
            return;
        }

        btnAdicionar.setEnabled(false);
        lblErro.setText(" ");
        principal.status("Registrando entrada...");

        Tarefa.rodar(
                () -> {
                    ResultadoEntrada resultado = entradaService.processarEntrada(placa, tipo, "Cancela Principal");
                    int livresOutroTipo = resultado.liberada() ? 0
                            : livres(vagaDAO.listar(), tipo == TipoVeiculo.CARRO ? TipoVeiculo.MOTO : TipoVeiculo.CARRO);
                    return new Retorno(resultado, livresOutroTipo);
                },
                retorno -> {
                    if (retorno.resultado().liberada()) {
                        mostrarLiberada(placa, retorno.resultado());
                    } else {
                        mostrarLotado(tipo, retorno.livresOutroTipo());
                    }
                },
                erro -> {
                    btnAdicionar.setEnabled(true);
                    lblErro.setText(Tarefa.mensagem(erro));
                    principal.status(" ");
                });
    }

    private record Retorno(ResultadoEntrada resultado, int livresOutroTipo) {
    }

    private void mostrarLiberada(String placaDigitada, ResultadoEntrada resultado) {
        String placa = util.ValidadorPlaca.normalizar(placaDigitada);
        Estilo.bordaResultado(painelResultado, "Entrada liberada", Estilo.SUCESSO);
        lblLinha1.setText("Siga para a vaga");
        lblDestaque.setFont(Estilo.fonte(Font.BOLD, 140));
        lblDestaque.setForeground(Estilo.SUCESSO);
        lblDestaque.setText(resultado.vaga());
        lblDetalhes.setText("Placa: " + placa + "      Ticket: " + resultado.ticketId()
                + "      Entrada: " + LocalDateTime.now().format(HORA));
        lblObservacao.setText("Na saída, informe a placa para pagar.");
        btnVoltar.setText("Nova entrada");
        principal.status("Entrada registrada: " + placa + " na vaga " + resultado.vaga());
        mostrarResultado(8);
    }

    private void mostrarLotado(TipoVeiculo tipo, int livresOutroTipo) {
        String nomeTipo = tipo == TipoVeiculo.CARRO ? "carros" : "motos";
        String outroTipo = tipo == TipoVeiculo.CARRO ? "moto" : "carro";
        Estilo.bordaResultado(painelResultado, "Entrada recusada", Estilo.ERRO);
        lblLinha1.setText(" ");
        lblDestaque.setFont(Estilo.fonte(Font.BOLD, 56));
        lblDestaque.setForeground(Estilo.ERRO);
        lblDestaque.setText("Lotado para " + nomeTipo);
        lblDetalhes.setText("Não há vaga de " + tipo.getDescricao().toLowerCase() + " livre no momento.");
        lblObservacao.setText("Vagas de " + outroTipo + " livres: " + livresOutroTipo);
        btnVoltar.setText("Voltar");
        principal.status("Entrada recusada: lotado para " + nomeTipo);
        mostrarResultado(10);
    }

    private void mostrarResultado(int tempo) {
        segundos = tempo;
        lblContagem.setText("Volta sozinho para a entrada em " + segundos + " s");
        cartoes.show(this, RESULTADO);
        btnVoltar.requestFocusInWindow();
        relogio.restart();
    }

    private void contar() {
        segundos--;
        if (segundos <= 0) {
            voltarAoFormulario();
        } else {
            lblContagem.setText("Volta sozinho para a entrada em " + segundos + " s");
        }
    }

    private static JLabel centralizado(int tamanho, int estilo) {
        JLabel rotulo = Estilo.rotulo(" ", estilo, tamanho);
        rotulo.setHorizontalAlignment(SwingConstants.CENTER);
        return rotulo;
    }
}
