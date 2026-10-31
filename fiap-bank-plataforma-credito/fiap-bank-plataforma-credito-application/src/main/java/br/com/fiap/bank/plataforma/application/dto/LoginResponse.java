package br.com.fiap.bank.plataforma.application.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}