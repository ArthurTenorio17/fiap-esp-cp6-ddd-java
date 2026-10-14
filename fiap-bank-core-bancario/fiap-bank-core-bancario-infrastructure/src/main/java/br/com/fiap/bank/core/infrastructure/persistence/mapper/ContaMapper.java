package br.com.fiap.bank.core.infrastructure.persistence.mapper;

import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Saldo;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaJpaEntity;

public final class ContaMapper {

    private ContaMapper() {
    }

    public static Conta toDomain(ContaJpaEntity entity) {
        return Conta.reconstituir(
                entity.getId(),
                entity.getNumero(),
                entity.getAgencia(),
                entity.getTipo(),
                Cpf.of(entity.getCliente().getCpf()),
                Saldo.of(entity.getSaldo()),
                entity.getStatus(),
                entity.getDataAbertura());
    }

    public static ContaJpaEntity toEntity(Conta conta, ClienteJpaEntity titular) {
        ContaJpaEntity entity = new ContaJpaEntity();
        entity.setId(conta.getId());
        entity.setNumero(conta.getNumero());
        entity.setAgencia(conta.getAgencia());
        entity.setTipo(conta.getTipo());
        entity.setStatus(conta.getStatus());
        entity.setSaldo(conta.getSaldo().getValor());
        entity.setDataAbertura(conta.getDataAbertura());
        entity.setCliente(titular);
        return entity;
    }
}
