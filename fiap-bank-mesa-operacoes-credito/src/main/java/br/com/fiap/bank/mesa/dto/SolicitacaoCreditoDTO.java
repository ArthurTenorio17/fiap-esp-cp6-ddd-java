package br.com.fiap.bank.mesa.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitacaoCreditoDTO(
    Long id,
    String cpfCliente,
    String nomeCliente,
    String tipoCredito,
    BigDecimal valorSolicitado,
    Integer quantidadeParcelas,
    BigDecimal rendaMensal,
    Integer scoreCredito,
    String status,
    String parecerOperador,
    Boolean scrRestricoes,
    Integer scoreBureau,
    LocalDateTime dataSolicitacao,
    LocalDateTime dataAtualizacao
) {}
