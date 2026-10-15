package br.com.fiap.bank.core.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarClienteRequest(

        @NotBlank(message = "cpf e obrigatorio")
        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "cpf deve ter 11 digitos ou o formato XXX.XXX.XXX-XX")
        String cpf,

        @NotBlank(message = "nome e obrigatorio")
        @Size(min = 3, max = 150, message = "nome deve ter entre 3 e 150 caracteres")
        String nome,

        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        String email,

        @NotNull(message = "dataNascimento e obrigatoria")
        @Past(message = "dataNascimento deve estar no passado")
        LocalDate dataNascimento,

        @NotNull(message = "rendaMensal e obrigatoria")
        @DecimalMin(value = "0.00", message = "rendaMensal nao pode ser negativa")
        BigDecimal rendaMensal
) {
}
