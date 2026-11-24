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
 *
 * @author henri
 */
public class TicketDAO {

    // ticket "aberto" = ATIVO ou PAGO, ou seja o carro ainda ta no estacionamento
    private static final String SELECT_TICKET =
            "SELECT t.id, t.placa, t.tipo_veiculo, t.vaga_id, v.identificacao AS vaga_identificacao, "
            + "t.data_entrada, t.data_pagamento, t.data_saida, t.cancela_entrada, t.valor, t.status "
            + "FROM tickets t JOIN vagas v ON v.id = t.vaga_id ";

    public Ticket buscarAbertoPorPlaca(String placa) throws SQLException {
        try (Connection conexao = FabricaConexao.getConexao()) {
            return buscarAberto(conexao, placa);
        }
    }

    public List<Ticket> listarAtivos() throws SQLException {
        String sql = SELECT_TICKET + "WHERE t.status = 'ATIVO' ORDER BY t.data_entrada";
        List<Ticket> tickets = new ArrayList<>();

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {

            while (rs.next()) {
                tickets.add(montarTicket(rs));
            }
        }
        return tickets;
    }

    // ATIVO e PAGO: todos os carros que ainda estao no patio
    public List<Ticket> listarAbertos() throws SQLException {
        String sql = SELECT_TICKET + "WHERE t.status IN ('ATIVO','PAGO') ORDER BY t.data_entrada";
        List<Ticket> tickets = new ArrayList<>();

        try (Connection conexao = FabricaConexao.getConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {

            while (rs.next()) {
                tickets.add(montarTicket(rs));
            }
        }
        return tickets;
    }

    // daqui pra baixo sao usados dentro das transacoes

    public Ticket buscarAberto(Connection conexao, String placa) throws SQLException {
        return buscar(conexao, placa, false);
    }

    // mesma busca mas trava a linha do ticket (FOR UPDATE)
    public Ticket buscarAbertoComLock(Connection conexao, String placa) throws SQLException {
        return buscar(conexao, placa, true);
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

            try (ResultSet rs = comando.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1); // id do ticket criado
            }
        }
    }

    // o "AND status = 'ATIVO'" e pra garantir que so paga ticket ativo
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

    public boolean encerrar(Connection conexao, int ticketId, LocalDateTime dataSaida) throws SQLException {
        String sql = "UPDATE tickets SET status = 'ENCERRADO', data_saida = ? "
                + "WHERE id = ? AND status = 'PAGO'";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setTimestamp(1, Timestamp.valueOf(dataSaida));
            comando.setInt(2, ticketId);
            return comando.executeUpdate() == 1;
        }
    }

    private Ticket buscar(Connection conexao, String placa, boolean travar) throws SQLException {
        String sql = SELECT_TICKET + "WHERE t.placa = ? AND t.status IN ('ATIVO','PAGO')";
        if (travar) {
            sql += " FOR UPDATE";
        }

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, placa);
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return montarTicket(rs);
                }
                return null;
            }
        }
    }

    private Ticket montarTicket(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setId(rs.getInt("id"));
        t.setPlaca(rs.getString("placa"));
        t.setTipoVeiculo(TipoVeiculo.valueOf(rs.getString("tipo_veiculo")));
        t.setVagaId(rs.getInt("vaga_id"));
        t.setVagaIdentificacao(rs.getString("vaga_identificacao"));
        t.setDataEntrada(converterData(rs.getTimestamp("data_entrada")));
        t.setDataPagamento(converterData(rs.getTimestamp("data_pagamento")));
        t.setDataSaida(converterData(rs.getTimestamp("data_saida")));
        t.setCancelaEntrada(rs.getString("cancela_entrada"));
        t.setValor(rs.getBigDecimal("valor"));
        t.setStatus(StatusTicket.valueOf(rs.getString("status")));
        return t;
    }

    private LocalDateTime converterData(Timestamp data) {
        if (data == null) {
            return null;
        }
        return data.toLocalDateTime();
    }
}
