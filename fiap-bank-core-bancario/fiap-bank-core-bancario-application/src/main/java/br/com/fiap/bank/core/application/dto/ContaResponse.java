package br.com.fiap.bank.core.application.dto;

import br.com.fiap.bank.core.domain.model.Conta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Os nomes numeroConta, agencia, saldo e statusConta sao os mesmos que o app mobile
 * (ClienteCoreInfo) ja consome, para nao quebrar o front-end legado.
 * Tipo e status saem como texto para a apresentacao nao depender de enums do dominio.
 */
public record ContaResponse(
        UUID id,
        String numeroConta,
        String agencia,
        String tipoConta,
        String statusConta,
        BigDecimal saldo,
        LocalDate dataAbertura,
        String cpfTitular
) {

    public static ContaResponse de(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNumero(),
                conta.getAgencia(),
                conta.getTipo().name(),
                conta.getStatus().name(),
                conta.getSaldo().getValor(),
                conta.getDataAbertura(),
                conta.getCpfTitular().getValor());
    }
}
