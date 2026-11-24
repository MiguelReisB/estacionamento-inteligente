package com.mycompany.mavenproject1;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Lista de usuarios e troca de perfil. So aparece para administrador.
 *
 * @author henri
 */
public class PainelUsuarios extends JPanel implements Tela_Principal.Atualizavel {

    private final Tela_Principal principal;
    private final DefaultTableModel modelo = Estilo.modeloTabela("Nome", "E-mail", "Perfil");
    private final JTable tabela = Estilo.tabela(modelo);
    private final JLabel lblMensagem = Estilo.rotulo(" ", Font.PLAIN, 13);
    private List<UsuarioInfo> usuarios = new ArrayList<>();

    public PainelUsuarios(Tela_Principal principal) {
        super(new BorderLayout(0, 14));
        this.principal = principal;
        setBackground(Estilo.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel painel = Estilo.painel(new BorderLayout(0, 12));
        Estilo.bordaTitulada(painel, "Usuários cadastrados");
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton btnNovo = Estilo.botaoPrimario("Novo usuário", 14);
        JButton btnAdmin = Estilo.botaoPadrao("Tornar administrador", 14);
        JButton btnOperador = Estilo.botaoPadrao("Tornar operador", 14);
        JPanel botoes = Estilo.painel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botoes.add(btnNovo);
        botoes.add(btnAdmin);
        botoes.add(btnOperador);

        JLabel dica = Estilo.rotulo("Os botões mudam o perfil do usuário selecionado na tabela. "
                + "O administrador não pode tirar o próprio acesso.", Font.PLAIN, 13);
        dica.setForeground(Estilo.TEXTO_SECUNDARIO);

        JPanel baixo = Estilo.painel(new BorderLayout(0, 8));
        baixo.add(botoes, BorderLayout.NORTH);
        baixo.add(dica, BorderLayout.CENTER);
        baixo.add(lblMensagem, BorderLayout.SOUTH);
        painel.add(baixo, BorderLayout.SOUTH);

        add(Estilo.titulo("Usuários", 20), BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);

        btnNovo.addActionListener(e -> novoUsuario());
        btnAdmin.addActionListener(e -> alterarPerfil(UsuarioInfo.ADMIN));
        btnOperador.addActionListener(e -> alterarPerfil(UsuarioInfo.OPERADOR));
    }

    @Override
    public void aoMostrar() {
        lblMensagem.setText(" ");
        carregar();
    }

    private void carregar() {
        principal.status("Carregando usuários...");
        Tarefa.rodar(
                UsuarioApiClient::listar,
                lista -> {
                    usuarios = lista;
                    modelo.setRowCount(0);
                    for (UsuarioInfo u : lista) {
                        modelo.addRow(new Object[]{u.nome(), u.email(), u.perfilDescricao()});
                    }
                    principal.status(lista.size() + " usuários cadastrados.");
                },
                erro -> principal.status(Tarefa.mensagem(erro)));
    }

    private void alterarPerfil(String perfil) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            mensagem("Selecione um usuário na tabela.", true);
            return;
        }
        UsuarioInfo alvo = usuarios.get(tabela.convertRowIndexToModel(linha));

        if (Sessao.getUsuario() != null && alvo.id() == Sessao.getUsuario().id()) {
            mensagem("Você não pode alterar o seu próprio perfil.", true);
            return;
        }
        if (perfil.equals(alvo.perfil())) {
            mensagem(alvo.nome() + " já é " + alvo.perfilDescricao().toLowerCase() + ".", true);
            return;
        }

        Tarefa.rodar(
                () -> UsuarioApiClient.alterarPerfil(alvo.id(), perfil),
                status -> {
                    if (status == 200) {
                        mensagem("Perfil de " + alvo.nome() + " alterado.", false);
                        carregar();
                    } else if (status == 403) {
                        mensagem("Somente administradores podem alterar perfis.", true);
                    } else if (status == 404) {
                        mensagem("Usuário não encontrado.", true);
                        carregar();
                    } else if (status == 409) {
                        mensagem("Você não pode alterar o seu próprio perfil.", true);
                    } else {
                        mensagem("Não foi possível alterar o perfil (código " + status + ").", true);
                    }
                },
                erro -> mensagem(Tarefa.mensagem(erro), true));
    }

    private void novoUsuario() {
        JDialog janela = new JDialog(principal, "Novo usuário", true);
        janela.getContentPane().setBackground(Estilo.FUNDO);
        janela.getContentPane().setLayout(new GridBagLayout());

        PainelCadastro formulario = new PainelCadastro(
                () -> {
                    janela.dispose();
                    mensagem("Usuário cadastrado como operador.", false);
                    carregar();
                },
                janela::dispose);
        janela.getContentPane().add(formulario);
        formulario.usarComoPadrao(janela.getRootPane());

        janela.setSize(480, 560);
        janela.setLocationRelativeTo(principal);
        janela.setVisible(true);
    }

    private void mensagem(String texto, boolean erro) {
        lblMensagem.setForeground(erro ? Estilo.ERRO : Estilo.SUCESSO);
        lblMensagem.setText(texto);
    }
}
