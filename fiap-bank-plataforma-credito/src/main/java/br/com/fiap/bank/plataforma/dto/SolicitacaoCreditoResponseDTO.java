package br.com.fiap.bank.plataforma.dto;

import br.com.fiap.bank.plataforma.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.model.StatusSolicitacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitacaoCreditoResponseDTO(
    Long id,
    String cpfCliente,
    String nomeCliente,
    String tipoCredito,
    BigDecimal valorSolicitado,
    Integer quantidadeParcelas,
    BigDecimal rendaMensal,
    Integer scoreCredito,
    StatusSolicitacao status,
    String parecerOperador,
    Boolean scrRestricoes,
    Integer scoreBureau,
    LocalDateTime dataSolicitacao,
    LocalDateTime dataAtualizacao
) {
    public static SolicitacaoCreditoResponseDTO fromEntity(SolicitacaoCredito entity) {
        return new SolicitacaoCreditoResponseDTO(
            entity.getId(),
            entity.getCpfCliente(),
            entity.getNomeCliente(),
            entity.getTipoCredito(),
            entity.getValorSolicitado(),
            entity.getQuantidadeParcelas(),
            entity.getRendaMensal(),
            entity.getScoreCredito(),
            entity.getStatus(),
            entity.getParecerOperador(),
            entity.getScrRestricoes(),
            entity.getScoreBureau(),
            entity.getDataSolicitacao(),
            entity.getDataAtualizacao()
        );
    }
}
