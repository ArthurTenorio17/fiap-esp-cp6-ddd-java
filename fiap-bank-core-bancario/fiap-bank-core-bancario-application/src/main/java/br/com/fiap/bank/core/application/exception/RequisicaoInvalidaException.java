package br.com.fiap.bank.core.application.exception;

/** Pedido que fere uma regra de negocio (ex.: CPF malformado). Vira 400 na camada de apresentacao. */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
