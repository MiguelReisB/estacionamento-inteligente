package model;

/**
 *
 * @author henri
 */
public class Vaga {

    public static final String LIVRE = "LIVRE";
    public static final String OCUPADA = "OCUPADA";

    private int id;
    private String identificacao;
    private TipoVeiculo tipo;
    private String status;

    public Vaga() {
    }

    public Vaga(int id, String identificacao, TipoVeiculo tipo, String status) {
        this.id = id;
        this.identificacao = identificacao;
        this.tipo = tipo;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public void setIdentificacao(String identificacao) {
        this.identificacao = identificacao;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVeiculo tipo) {
        this.tipo = tipo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLivre() {
        return LIVRE.equals(status);
    }
}
