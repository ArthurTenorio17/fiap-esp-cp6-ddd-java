package br.com.fiap.bank.core.domain.exception;

public class SaldoInsuficienteException extends DominioException {

    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
