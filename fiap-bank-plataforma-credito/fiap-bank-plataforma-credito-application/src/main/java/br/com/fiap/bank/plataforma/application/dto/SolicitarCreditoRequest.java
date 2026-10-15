package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record SolicitarCreditoRequest(

        @NotBlank(message = "O CPF do cliente é obrigatório")
        @Pattern(
                regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}",
                message = "O CPF deve conter 11 dígitos ou estar formatado"
        )
        String cpfCliente,

        @NotBlank(message = "O nome do cliente é obrigatório")
        @Size(min = 3, max = 150)
        String nomeCliente,

        @NotBlank(message = "O tipo de crédito é obrigatório")
        String tipoCredito,

        @NotNull(message = "O valor solicitado é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valorSolicitado,

        @NotNull(message = "A quantidade de parcelas é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        Integer quantidadeParcelas,

        @NotNull(message = "A renda mensal é obrigatória")
        @DecimalMin(value = "0.0", message = "A renda não pode ser negativa")
        BigDecimal rendaMensal

) {}
