package model;

/**
 *
 * @author henri
 */
public enum StatusTicket {
    ATIVO,
    PAGO,
    ENCERRADO;

    // ativo so pode ir pra pago, e pago so pode ir pra encerrado
    public boolean podeIrPara(StatusTicket proximo) {
        if (this == ATIVO) {
            return proximo == PAGO;
        }
        if (this == PAGO) {
            return proximo == ENCERRADO;
        }
        return false;
    }
}
