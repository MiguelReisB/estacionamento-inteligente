package model;

/** Tipo do veículo e, ao mesmo tempo, tipo da vaga que ele pode ocupar. */
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
