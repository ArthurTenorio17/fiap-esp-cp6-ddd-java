package br.com.fiap.bank.core.application;

import br.com.fiap.bank.core.application.dto.AtualizarSaldoRequest;
import br.com.fiap.bank.core.application.dto.CriarClienteRequest;
import br.com.fiap.bank.core.application.dto.CriarContaRequest;
import br.com.fiap.bank.core.domain.model.TipoConta;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void clienteValidoNaoTemViolacoes() {
        var dto = new CriarClienteRequest("123.456.789-00", "Ana Silva", "ana@fiap.com.br",
                LocalDate.of(1995, 5, 10), BigDecimal.ZERO);
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void clienteInvalidoViolaTodasAsRegras() {
        var dto = new CriarClienteRequest("123", "Al", "isso-nao-e-email",
                LocalDate.now().plusDays(1), new BigDecimal("-1"));
        assertEquals(5, validator.validate(dto).size());
    }

    @Test
    void agenciaOpcionalMasComQuatroDigitosQuandoInformada() {
        var semAgencia = new CriarContaRequest("12345678900", null, TipoConta.POUPANCA, BigDecimal.TEN);
        var agenciaCurta = new CriarContaRequest("12345678900", "12", TipoConta.POUPANCA, BigDecimal.TEN);
        assertTrue(validator.validate(semAgencia).isEmpty());
        assertEquals(1, validator.validate(agenciaCurta).size());
    }

    @Test
    void contaExigeTipoESaldoNaoNegativo() {
        var dto = new CriarContaRequest("12345678900", "0001", null, new BigDecimal("-0.01"));
        assertEquals(2, validator.validate(dto).size());
    }

    @Test
    void operacaoAceitaSomenteDepositoOuSaqueEValorPositivo() {
        assertTrue(validator.validate(new AtualizarSaldoRequest("saque", BigDecimal.ONE)).isEmpty());
        assertEquals(1, validator.validate(new AtualizarSaldoRequest("TRANSFERENCIA", BigDecimal.ONE)).size());
        assertEquals(1, validator.validate(new AtualizarSaldoRequest("DEPOSITO", BigDecimal.ZERO)).size());
    }
}
