package br.com.fiap.bank.core.domain.exception;

/**
 * Violacao de uma regra de negocio do Core Bancario.
 * A camada de apresentacao decide qual status HTTP representa cada caso.
 */
public class DominioException extends RuntimeException {

    public DominioException(String mensagem) {
        super(mensagem);
    }
}
