package br.com.fiap.bank.core.application.dto;

import br.com.fiap.bank.core.domain.model.TipoConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CriarContaRequest(

        @NotBlank(message = "cpfCliente e obrigatorio")
        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "cpfCliente deve ter 11 digitos ou o formato XXX.XXX.XXX-XX")
        String cpfCliente,

        // opcional: quando ausente o dominio assume "0001"
        @Pattern(regexp = "^\\d{4}$", message = "agencia deve ter exatamente 4 digitos")
        String agencia,

        @NotNull(message = "tipoConta e obrigatorio (CORRENTE, POUPANCA ou SALARIO)")
        TipoConta tipoConta,

        @NotNull(message = "saldoInicial e obrigatorio")
        @DecimalMin(value = "0.00", message = "saldoInicial nao pode ser negativo")
        BigDecimal saldoInicial
) {
}
