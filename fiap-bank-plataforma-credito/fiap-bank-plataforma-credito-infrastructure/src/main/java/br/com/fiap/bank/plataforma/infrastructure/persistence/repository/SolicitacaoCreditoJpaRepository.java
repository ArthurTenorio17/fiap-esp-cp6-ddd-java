package br.com.fiap.bank.plataforma.infrastructure.persistence.repository;

import br.com.fiap.bank.plataforma.infrastructure.persistence.entity.SolicitacaoCreditoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoCreditoJpaRepository
        extends JpaRepository<SolicitacaoCreditoJpaEntity, Long> {

    List<SolicitacaoCreditoJpaEntity>
    findByCpfClienteOrderByDataSolicitacaoDesc(String cpfCliente);

    List<SolicitacaoCreditoJpaEntity>
    findAllByOrderByDataSolicitacaoDesc();
}
