package br.com.fiap.bank.plataforma.dto;

public record ReprovarSolicitacaoRequest(
    Long solicitacaoId,
    String motivoReprovacao
) {}
