package model;

/**
 * Máquina de estados do ticket:
 *   ATIVO  -> PAGO       (pagamento)
 *   PAGO   -> ENCERRADO  (saída: libera a vaga)
 * Qualquer outra transição é proibida.
 */
public enum StatusTicket {
    ATIVO,
    PAGO,
    ENCERRADO;

    public boolean podeIrPara(StatusTicket proximo) {
        return (this == ATIVO && proximo == PAGO)
                || (this == PAGO && proximo == ENCERRADO);
    }
}
