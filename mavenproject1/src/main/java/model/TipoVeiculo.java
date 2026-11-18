package model;

/**
 *
 * @author henri
 */
public enum TipoVeiculo {
    CARRO("Carro"),
    MOTO("Moto");

    private final String descricao;

    TipoVeiculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
