package br.com.fiap.bank.core.domain.vo;

import br.com.fiap.bank.core.domain.exception.DominioException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * CPF guardado sempre sem mascara (11 digitos). Aceita tambem o formato XXX.XXX.XXX-XX na entrada.
 */
@Getter
@EqualsAndHashCode
public final class Cpf {

    private final String valor;

    private Cpf(String valor) {
        this.valor = valor;
    }

    public static Cpf of(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new DominioException("CPF e obrigatorio");
        }
        String digitos = entrada.trim().replaceAll("[.\\-]", "");
        if (!digitos.matches("\\d{11}")) {
            throw new DominioException("CPF deve conter 11 digitos numericos: " + entrada);
        }
        return new Cpf(digitos);
    }

    /** Versao com mascara, usada apenas para exibicao. */
    public String formatado() {
        return valor.substring(0, 3) + "." + valor.substring(3, 6) + "."
                + valor.substring(6, 9) + "-" + valor.substring(9);
    }

    @Override
    public String toString() {
        return valor;
    }
}
