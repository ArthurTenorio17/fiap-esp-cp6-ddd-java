package br.com.fiap.bank.plataforma.domain.port;

public interface SenhaVerificacaoPort {

    boolean verificar(String senhaInformada, String senhaHash);

}
