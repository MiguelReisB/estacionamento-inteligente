package service;

/**
 *
 * @author henri
 */
// erro de regra do estacionamento (placa invalida, ticket nao pago, etc)
// a mensagem ja pode ir direto pra tela
public class OperacaoInvalidaException extends Exception {

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
