package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConsultarStatusRequest(

        @NotBlank(message = "O CPF do cliente é obrigatório")
        @Pattern(
                regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}",
                message = "O CPF deve conter 11 dígitos ou estar formatado"
        )
        String cpfCliente

) {}
