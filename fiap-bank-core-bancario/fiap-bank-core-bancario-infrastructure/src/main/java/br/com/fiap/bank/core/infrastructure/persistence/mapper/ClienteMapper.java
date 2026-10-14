package br.com.fiap.bank.core.infrastructure.persistence.mapper;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Email;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static Cliente toDomain(ClienteJpaEntity entity, Conta conta) {
        return Cliente.reconstituir(
                entity.getId(),
                Cpf.of(entity.getCpf()),
                entity.getNome(),
                Email.of(entity.getEmail()),
                entity.getDataNascimento(),
                entity.getRendaMensal(),
                conta);
    }

    public static ClienteJpaEntity toEntity(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setId(cliente.getId());
        entity.setCpf(cliente.getCpf().getValor());
        entity.setNome(cliente.getNome());
        entity.setEmail(cliente.getEmail().getValor());
        entity.setDataNascimento(cliente.getDataNascimento());
        entity.setRendaMensal(cliente.getRendaMensal());
        return entity;
    }
}
