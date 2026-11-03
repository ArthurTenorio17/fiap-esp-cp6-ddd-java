package br.com.fiap.bank.mesa.dto;

public record LoginResponseDTO(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
