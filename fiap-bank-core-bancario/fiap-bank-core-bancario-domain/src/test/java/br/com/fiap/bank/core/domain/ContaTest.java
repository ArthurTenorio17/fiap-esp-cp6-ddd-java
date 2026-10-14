package br.com.fiap.bank.core.domain;

import br.com.fiap.bank.core.domain.exception.ContaInativaException;
import br.com.fiap.bank.core.domain.exception.DominioException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.model.TipoConta;
import br.com.fiap.bank.core.domain.vo.Cpf;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ContaTest {

    private Conta novaConta(String saldoInicial) {
        return Conta.abrir("12345-6", null, TipoConta.CORRENTE, Cpf.of("12345678900"), new BigDecimal(saldoInicial));
    }

    @Test
    void agenciaAusenteAssumePadrao() {
        assertEquals("0001", novaConta("0").getAgencia());
    }

    @Test
    void agenciaInformadaPrecisaTerQuatroDigitos() {
        assertThrows(DominioException.class,
                () -> Conta.abrir("1-1", "12", TipoConta.POUPANCA, Cpf.of("12345678900"), BigDecimal.TEN));
    }

    @Test
    void depositoESaqueAtualizamSaldo() {
        Conta conta = novaConta("100.00");
        conta.depositar(new BigDecimal("50"));
        conta.sacar(new BigDecimal("30.50"));
        assertEquals(new BigDecimal("119.50"), conta.getSaldo().getValor());
    }

    @Test
    void saqueMaiorQueSaldoEhRejeitado() {
        Conta conta = novaConta("10.00");
        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(new BigDecimal("10.01")));
        assertEquals(new BigDecimal("10.00"), conta.getSaldo().getValor());
    }

    @Test
    void valorZeroOuNegativoEhRejeitado() {
        Conta conta = novaConta("10.00");
        assertThrows(DominioException.class, () -> conta.depositar(BigDecimal.ZERO));
        assertThrows(DominioException.class, () -> conta.sacar(new BigDecimal("-1")));
    }

    @Test
    void contaEncerradaNaoMovimentaEEncerrarEhIdempotente() {
        Conta conta = novaConta("10.00");
        conta.encerrar();
        conta.encerrar();
        assertFalse(conta.estaAtiva());
        assertThrows(ContaInativaException.class, () -> conta.depositar(BigDecimal.ONE));
    }
}
