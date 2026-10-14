package br.com.fiap.bank.core.domain.exception;

public class ContaInativaException extends DominioException {

    public ContaInativaException(String numeroConta) {
        super("A conta " + numeroConta + " nao esta ativa para movimentacoes");
    }
}
