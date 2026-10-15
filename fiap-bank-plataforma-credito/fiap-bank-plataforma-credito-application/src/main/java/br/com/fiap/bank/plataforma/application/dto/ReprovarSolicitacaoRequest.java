package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReprovarSolicitacaoRequest(

        @NotNull(message = "O ID da solicitação é obrigatório")
        @Positive(message = "O ID da solicitação deve ser positivo")
        Long solicitacaoId,

        @Size(
                max = 460,
                message = "O motivo deve conter no máximo 460 caracteres"
        )
        String motivoReprovacao

) {}
