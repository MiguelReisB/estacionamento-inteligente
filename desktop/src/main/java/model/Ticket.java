package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author henri
 */
public class Ticket {

    private int id;
    private String placa;
    private TipoVeiculo tipoVeiculo;
    private int vagaId;
    private String vagaIdentificacao;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataPagamento;
    private LocalDateTime dataSaida;
    private String cancelaEntrada;
    private BigDecimal valor;
    private StatusTicket status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public TipoVeiculo getTipoVeiculo() {
        return tipoVeiculo;
    }

    public void setTipoVeiculo(TipoVeiculo tipoVeiculo) {
        this.tipoVeiculo = tipoVeiculo;
    }

    public int getVagaId() {
        return vagaId;
    }

    public void setVagaId(int vagaId) {
        this.vagaId = vagaId;
    }

    public String getVagaIdentificacao() {
        return vagaIdentificacao;
    }

    public void setVagaIdentificacao(String vagaIdentificacao) {
        this.vagaIdentificacao = vagaIdentificacao;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
    }

    public String getCancelaEntrada() {
        return cancelaEntrada;
    }

    public void setCancelaEntrada(String cancelaEntrada) {
        this.cancelaEntrada = cancelaEntrada;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusTicket getStatus() {
        return status;
    }

    public void setStatus(StatusTicket status) {
        this.status = status;
    }
}
