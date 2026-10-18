package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.Size;

/**
 * Corpo do PUT /solicitacoes/{id}/aprovacao.
 * O id vem na URL, por isso aqui so existe o parecer.
 */
public record ParecerAprovacaoRequest(

        @Size(
                max = 470,
                message = "O parecer deve conter no máximo 470 caracteres"
        )
        String parecerOperador

) {}
