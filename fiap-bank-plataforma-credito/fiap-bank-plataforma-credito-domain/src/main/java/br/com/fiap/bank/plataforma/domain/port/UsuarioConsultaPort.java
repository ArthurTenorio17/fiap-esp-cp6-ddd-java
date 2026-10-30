package br.com.fiap.bank.plataforma.application.port;

import java.util.Optional;

public interface UsuarioConsultaPort {

    Optional<UsuarioAutenticacao> buscarPorEmail(String email);

}