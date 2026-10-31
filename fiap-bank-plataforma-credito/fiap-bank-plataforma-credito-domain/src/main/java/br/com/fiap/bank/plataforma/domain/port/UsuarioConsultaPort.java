package br.com.fiap.bank.plataforma.domain.port;

import java.util.Optional;

public interface UsuarioConsultaPort {

    Optional<UsuarioAutenticacao> buscarPorEmail(String email);

}