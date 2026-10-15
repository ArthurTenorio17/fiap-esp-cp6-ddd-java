package br.com.fiap.bank.plataforma.domain.exception;

public class CoreBancarioIndisponivelException extends RuntimeException {

    public CoreBancarioIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public CoreBancarioIndisponivelException(
            String mensagem,
            Throwable causa) {
        super(mensagem, causa);
    }
}
