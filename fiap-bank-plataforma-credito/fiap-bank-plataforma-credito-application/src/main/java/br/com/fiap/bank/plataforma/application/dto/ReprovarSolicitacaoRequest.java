
package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.Size;

public record ReprovarSolicitacaoRequest(

        Long solicitacaoId,

        @Size(
                max = 460,
                message = "O motivo deve conter no máximo 460 caracteres"
        )
        String motivoReprovacao

) {}
