package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.Size;

/**
 * Corpo do PUT /solicitacoes/{id}/reprovacao.
 * O id vem na URL, por isso aqui so existe o motivo.
 */
public record MotivoReprovacaoRequest(

        @Size(
                max = 460,
                message = "O motivo deve conter no máximo 460 caracteres"
        )
        String motivoReprovacao

) {}
