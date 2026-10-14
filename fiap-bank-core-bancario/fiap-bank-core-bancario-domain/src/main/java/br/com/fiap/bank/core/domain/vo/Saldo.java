package br.com.fiap.bank.core.domain.vo;

import br.com.fiap.bank.core.domain.exception.DominioException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valor monetario imutavel. Toda operacao devolve um novo Saldo.
 */
@Getter
@EqualsAndHashCode
public final class Saldo {

    public static final Saldo ZERO = new Saldo(BigDecimal.ZERO.setScale(2));

    private final BigDecimal valor;

    private Saldo(BigDecimal valor) {
        this.valor = valor;
    }

    public static Saldo of(BigDecimal valor) {
        if (valor == null) {
            throw new DominioException("Saldo e obrigatorio");
        }
        if (valor.signum() < 0) {
            throw new DominioException("Saldo nao pode ser negativo");
        }
        return new Saldo(valor.setScale(2, RoundingMode.HALF_UP));
    }

    public Saldo adicionar(BigDecimal quantia) {
        exigirPositiva(quantia);
        return new Saldo(valor.add(quantia).setScale(2, RoundingMode.HALF_UP));
    }

    public Saldo subtrair(BigDecimal quantia) {
        exigirPositiva(quantia);
        if (valor.compareTo(quantia) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente: disponivel " + valor + ", solicitado " + quantia);
        }
        return new Saldo(valor.subtract(quantia).setScale(2, RoundingMode.HALF_UP));
    }

    private static void exigirPositiva(BigDecimal quantia) {
        if (quantia == null || quantia.signum() <= 0) {
            throw new DominioException("O valor da operacao deve ser maior que zero");
        }
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
