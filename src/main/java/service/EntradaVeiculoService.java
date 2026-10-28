/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import util.FabricaConexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



/**
 *
 * @author cnvtte
 */
public class EntradaVeiculoService {

    public boolean processarEntrada(String placa, String cancelaOrigem) {
        String sqlLock = "SELECT id FROM vagas WHERE status = 'LIVRE' LIMIT 1 FOR UPDATE";
        String sqlInsertTicket = "INSERT INTO tickets (placa, vaga_id, data_entrada, cancela_entrada, status) VALUES (?, ?, NOW(), ?, 'ATIVO')";
        String sqlUpdateVaga = "UPDATE vagas SET status = 'OCUPADA' WHERE id = ?";

        try (Connection conexao = FabricaConexao.getConexaoTransacional()) {
            
            int vagaId = -1;

            try (PreparedStatement stmtLockVaga = conexao.prepareStatement(sqlLock);
                 ResultSet rs = stmtLockVaga.executeQuery()) {
                if (rs.next()) {
                    vagaId = rs.getInt("id");
                }
            }

            if (vagaId != -1) {
                try (PreparedStatement stmtInsertTicket = conexao.prepareStatement(sqlInsertTicket)) {
                    stmtInsertTicket.setString(1, placa);
                    stmtInsertTicket.setInt(2, vagaId);
                    stmtInsertTicket.setString(3, cancelaOrigem);
                    stmtInsertTicket.executeUpdate();
                }

                try (PreparedStatement stmtUpdateVaga = conexao.prepareStatement(sqlUpdateVaga)) {
                    stmtUpdateVaga.setInt(1, vagaId);
                    stmtUpdateVaga.executeUpdate();
                }

                conexao.commit();
                System.out.println("[" + cancelaOrigem + "] autorizada" + placa + "Vaga ID: " + vagaId);
                return true;

            } else {

                conexao.rollback();
                System.out.println("[" + cancelaOrigem + "] recusada" + placa);
                return false;
            }

        } catch (SQLException e) {

            System.err.println("[" + cancelaOrigem + "] ROLLBACK" + e.getMessage());
            return false;
        }
    }
}
