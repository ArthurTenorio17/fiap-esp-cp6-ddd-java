package br.com.fiap.bank.plataforma.domain.port;

public interface TokenGeracaoPort {

    String gerarToken(UsuarioAutenticacao usuario);
}
