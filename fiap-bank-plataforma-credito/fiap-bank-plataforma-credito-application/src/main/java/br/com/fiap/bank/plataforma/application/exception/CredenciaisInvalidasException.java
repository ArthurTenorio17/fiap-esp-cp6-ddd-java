package br.com.fiap.bank.plataforma.application.exception;

/** E-mail ou senha incorretos. A mensagem nao revela qual dos dois falhou. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
