package br.com.fiap.bank.plataforma.infrastructure.persistence.mapper;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.infrastructure.persistence.entity.SolicitacaoCreditoJpaEntity;

public final class SolicitacaoCreditoMapper {

    private SolicitacaoCreditoMapper() {
    }

    public static SolicitacaoCredito paraDominio(
            SolicitacaoCreditoJpaEntity entity) {

        if (entity == null) {
            return null;
        }

        return new SolicitacaoCredito(
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

    public static SolicitacaoCreditoJpaEntity paraJpa(
            SolicitacaoCredito dominio) {

        if (dominio == null) {
            return null;
        }

        SolicitacaoCreditoJpaEntity entity =
                new SolicitacaoCreditoJpaEntity();

        entity.setId(dominio.getId());
        entity.setCpfCliente(dominio.getCpfCliente());
        entity.setNomeCliente(dominio.getNomeCliente());
        entity.setTipoCredito(dominio.getTipoCredito());
        entity.setValorSolicitado(dominio.getValorSolicitado());
        entity.setQuantidadeParcelas(dominio.getQuantidadeParcelas());
        entity.setRendaMensal(dominio.getRendaMensal());
        entity.setScoreCredito(dominio.getScoreCredito());
        entity.setStatus(dominio.getStatus());
        entity.setParecerOperador(dominio.getParecerOperador());
        entity.setScrRestricoes(dominio.getScrRestricoes());
        entity.setScoreBureau(dominio.getScoreBureau());
        entity.setDataSolicitacao(dominio.getDataSolicitacao());
        entity.setDataAtualizacao(dominio.getDataAtualizacao());

        return entity;
    }
}
