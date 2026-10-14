package br.com.fiap.bank.core.domain.vo;

import br.com.fiap.bank.core.domain.exception.DominioException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public final class Email {

    private static final Pattern FORMATO = Pattern.compile("^[\\w.%+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final String valor;

    private Email(String valor) {
        this.valor = valor;
    }

    public static Email of(String entrada) {
        if (entrada == null || !FORMATO.matcher(entrada.trim()).matches()) {
            throw new DominioException("E-mail invalido: " + entrada);
        }
        return new Email(entrada.trim().toLowerCase());
    }

    @Override
    public String toString() {
        return valor;
    }
}
