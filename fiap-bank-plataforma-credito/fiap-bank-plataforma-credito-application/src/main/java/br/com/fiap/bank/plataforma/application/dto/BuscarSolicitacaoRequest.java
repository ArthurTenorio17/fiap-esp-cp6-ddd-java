package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BuscarSolicitacaoRequest(

        @NotNull(message = "O ID da solicitação é obrigatório")
        @Positive(message = "O ID da solicitação deve ser positivo")
        Long solicitacaoId

) {}
