package br.com.fiap.bank.core.presentation.exception;

import java.time.Instant;
import java.util.List;

/** Corpo padrao devolvido em qualquer erro da API. */
public record ErroResponse(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoInvalido> campos
) {

    public record CampoInvalido(String campo, String mensagem) {
    }
}
