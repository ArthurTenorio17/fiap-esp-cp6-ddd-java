package br.com.fiap.bank.mesa.dto;

public record ReprovarSolicitacaoRequestDTO(
    Long solicitacaoId,
    String motivoReprovacao
) {}
