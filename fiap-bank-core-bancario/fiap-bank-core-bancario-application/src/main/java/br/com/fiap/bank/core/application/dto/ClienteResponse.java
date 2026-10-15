package br.com.fiap.bank.core.application.dto;

import br.com.fiap.bank.core.domain.model.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String cpf,
        String nome,
        String email,
        LocalDate dataNascimento,
        BigDecimal rendaMensal,
        ContaResponse conta
) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getCpf().getValor(),
                cliente.getNome(),
                cliente.getEmail().getValor(),
                cliente.getDataNascimento(),
                cliente.getRendaMensal(),
                cliente.getConta() == null ? null : ContaResponse.de(cliente.getConta()));
    }
}
