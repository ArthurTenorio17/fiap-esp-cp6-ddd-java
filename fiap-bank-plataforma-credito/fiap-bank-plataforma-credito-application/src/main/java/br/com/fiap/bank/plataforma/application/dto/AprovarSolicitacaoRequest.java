
package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.Size;

public record AprovarSolicitacaoRequest(

        Long solicitacaoId,

        @Size(
                max = 470,
                message = "O parecer deve conter no máximo 470 caracteres"
        )
        String parecerOperador

) {}
