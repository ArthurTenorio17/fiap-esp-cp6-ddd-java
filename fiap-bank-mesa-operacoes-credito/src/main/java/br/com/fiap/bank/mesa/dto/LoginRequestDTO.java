package br.com.fiap.bank.mesa.dto;

public record LoginRequestDTO(
    String email,
    String senha
) {}
