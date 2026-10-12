package br.com.fiap.bank.mesa.dto;

public record AprovarSolicitacaoRequestDTO(
    Long solicitacaoId,
    String parecerOperador
) {}
