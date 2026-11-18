package service;

/**
 * Violação de regra de negócio (placa inválida, ticket inexistente,
 * transição de estado proibida etc.). A mensagem já é amigável
 * e pode ser mostrada direto ao usuário.
 */
public class OperacaoInvalidaException extends Exception {

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
