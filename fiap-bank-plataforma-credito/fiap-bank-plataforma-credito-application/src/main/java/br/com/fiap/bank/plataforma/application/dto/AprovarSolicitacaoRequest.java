package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AprovarSolicitacaoRequest(

        @NotNull(message = "O ID da solicitação é obrigatório")
        @Positive(message = "O ID da solicitação deve ser positivo")
        Long solicitacaoId,

        @Size(
                max = 470,
                message = "O parecer deve conter no máximo 470 caracteres"
        )
        String parecerOperador

) {}
