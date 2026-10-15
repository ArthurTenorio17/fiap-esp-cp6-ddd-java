package br.com.fiap.bank.core.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record AtualizarSaldoRequest(

        @NotBlank(message = "operacao e obrigatoria")
        @Pattern(regexp = "(?i)DEPOSITO|SAQUE", message = "operacao deve ser DEPOSITO ou SAQUE")
        String operacao,

        @NotNull(message = "valor e obrigatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "valor deve ser maior que zero")
        BigDecimal valor
) {
}
