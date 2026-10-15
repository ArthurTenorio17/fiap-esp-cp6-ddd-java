package br.com.fiap.bank.plataforma.domain.exception;

public class SolicitacaoNaoEncontradaException
        extends RuntimeException {

    public SolicitacaoNaoEncontradaException(Long id) {
        super("Solicitacao de credito nao encontrada com ID: " + id);
    }
}
