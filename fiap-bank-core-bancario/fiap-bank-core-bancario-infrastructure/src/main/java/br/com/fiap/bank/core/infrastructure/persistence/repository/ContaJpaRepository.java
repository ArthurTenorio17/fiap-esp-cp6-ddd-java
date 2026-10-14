package br.com.fiap.bank.core.infrastructure.persistence.repository;

import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaJpaRepository extends JpaRepository<ContaJpaEntity, UUID> {

    Optional<ContaJpaEntity> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<ContaJpaEntity> findByClienteId(UUID clienteId);

    List<ContaJpaEntity> findByClienteCpf(String cpf);

    void deleteByClienteId(UUID clienteId);
}
