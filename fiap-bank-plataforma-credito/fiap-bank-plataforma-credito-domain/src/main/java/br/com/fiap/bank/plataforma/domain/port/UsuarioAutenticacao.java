package br.com.fiap.bank.plataforma.application.port;

import java.util.UUID;

public record UsuarioAutenticacao(
        UUID id,
        String email,
        String senhaHash,
        String nome,
        String perfil
) {}