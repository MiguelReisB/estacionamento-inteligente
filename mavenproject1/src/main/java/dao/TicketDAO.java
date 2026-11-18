package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.StatusTicket;
import model.Ticket;
import model.TipoVeiculo;
import util.FabricaConexao;

/**
 * Acesso à tabela tickets.
 *
 * "Ticket aberto" = status ATIVO ou PAGO (o veículo ainda está no pátio).
 * Os métodos que recebem uma Connection são usados dentro de transações.
 */
public class TicketDAO {

    private static final String SELECT_BASE =
            "SELECT t.id, t.placa, t.tipo_veiculo, t.vaga_id, v.identificacao AS vaga_identificacao, "
            + "t.data_entrada, t.data_pagamento, t.data_saida, t.cancela_entrada, t.valor, t.status "
            + "FROM tickets t JOIN vagas v ON v.id = t.vaga_id ";

    // ---------- consultas simples ----------

    /** Ticket ATIVO ou PAGO da placa, ou null se o veículo não está no estacionamento. */
    public Ticket buscarAbertoPorPlaca(String placa) throws SQLException {
        try (Connection conexao = FabricaConexao.getConexao()) {
            return buscarAberto(conexao, placa, false);
        }
    }

    /** Tickets ATIVOS (veículos estacionados que ainda não pagaram), do mais antigo ao mais novo. */
    public List<Ticket> listarAtivos() throws SQLException {
        String sql = SELECT_BASE + "WHERE t.status = 'ATIVO' ORDER BY t.data_entrada";
        List<Ticket> tickets = new ArrayList<>();

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {

            while (rs.next()) {
                tickets.add(montar(rs));
            }
        }
        return tickets;
    }

    // ---------- operações transacionais ----------

    /** Busca o ticket aberto da placa dentro da transação, sem travar. */
    public Ticket buscarAberto(Connection conexao, String placa) throws SQLException {
        return buscarAberto(conexao, placa, false);
    }

    /** Busca o ticket aberto da placa TRAVANDO a linha (SELECT ... FOR UPDATE). */
    public Ticket buscarAbertoComLock(Connection conexao, String placa) throws SQLException {
        return buscarAberto(conexao, placa, true);
    }

    public int inserir(Connection conexao, String placa, TipoVeiculo tipo, int vagaId,
                       LocalDateTime dataEntrada, String cancela) throws SQLException {
        String sql = "INSERT INTO tickets (placa, tipo_veiculo, vaga_id, data_entrada, cancela_entrada, status) "
                + "VALUES (?, ?, ?, ?, ?, 'ATIVO')";

        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, placa);
            comando.setString(2, tipo.name());
            comando.setInt(3, vagaId);
            comando.setTimestamp(4, Timestamp.valueOf(dataEntrada));
            comando.setString(5, cancela);
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                chaves.next();
                return chaves.getInt(1);
            }
        }
    }

    /** ATIVO -> PAGO. O "AND status = 'ATIVO'" impede pular etapas mesmo se a regra falhar no Java. */
    public boolean registrarPagamento(Connection conexao, int ticketId, BigDecimal valor,
                                      LocalDateTime dataPagamento) throws SQLException {
        String sql = "UPDATE tickets SET status = 'PAGO', valor = ?, data_pagamento = ? "
                + "WHERE id = ? AND status = 'ATIVO'";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setBigDecimal(1, valor);
            comando.setTimestamp(2, Timestamp.valueOf(dataPagamento));
            comando.setInt(3, ticketId);
            return comando.executeUpdate() == 1;
        }
    }

    /** PAGO -> ENCERRADO. */
    public boolean encerrar(Connection conexao, int ticketId, LocalDateTime dataSaida) throws SQLException {
        String sql = "UPDATE tickets SET status = 'ENCERRADO', data_saida = ? "
                + "WHERE id = ? AND status = 'PAGO'";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setTimestamp(1, Timestamp.valueOf(dataSaida));
            comando.setInt(2, ticketId);
            return comando.executeUpdate() == 1;
        }
    }

    // ---------- auxiliares ----------

    private Ticket buscarAberto(Connection conexao, String placa, boolean comLock) throws SQLException {
        String sql = SELECT_BASE + "WHERE t.placa = ? AND t.status IN ('ATIVO','PAGO')"
                + (comLock ? " FOR UPDATE" : "");

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, placa);
            try (ResultSet rs = comando.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    private Ticket montar(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setId(rs.getInt("id"));
        t.setPlaca(rs.getString("placa"));
        t.setTipoVeiculo(TipoVeiculo.valueOf(rs.getString("tipo_veiculo")));
        t.setVagaId(rs.getInt("vaga_id"));
        t.setVagaIdentificacao(rs.getString("vaga_identificacao"));
        t.setDataEntrada(paraLocal(rs.getTimestamp("data_entrada")));
        t.setDataPagamento(paraLocal(rs.getTimestamp("data_pagamento")));
        t.setDataSaida(paraLocal(rs.getTimestamp("data_saida")));
        t.setCancelaEntrada(rs.getString("cancela_entrada"));
        t.setValor(rs.getBigDecimal("valor"));
        t.setStatus(StatusTicket.valueOf(rs.getString("status")));
        return t;
    }

    private static LocalDateTime paraLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
