package br.com.fiap.bank.plataforma.dto;

public record AprovarSolicitacaoRequest(
    Long solicitacaoId,
    String parecerOperador
) {}
