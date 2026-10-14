package br.com.fiap.bank.core.infrastructure.persistence.adapter;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaRepository;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.mapper.ClienteMapper;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ClienteJpaRepository;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ContaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository clienteJpa;
    private final ContaJpaRepository contaJpa;
    private final ContaRepository contaRepository;

    /** Persiste apenas os dados do cliente; a conta e gravada pelo ContaRepository. */
    @Override
    @Transactional
    public Cliente salvar(Cliente cliente) {
        ClienteJpaEntity salvo = clienteJpa.save(ClienteMapper.toEntity(cliente));
        return ClienteMapper.toDomain(salvo, cliente.getConta());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(UUID id) {
        return clienteJpa.findById(id).map(this::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorCpf(Cpf cpf) {
        return clienteJpa.findByCpf(cpf.getValor()).map(this::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteJpa.findAll().stream().map(this::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCpf(Cpf cpf) {
        return clienteJpa.existsByCpf(cpf.getValor());
    }

    /** Remove tambem as contas do cliente para nao violar a chave estrangeira. */
    @Override
    @Transactional
    public void remover(UUID id) {
        contaJpa.deleteByClienteId(id);
        clienteJpa.deleteById(id);
    }

    private Cliente paraDominio(ClienteJpaEntity entity) {
        Conta conta = contaRepository.buscarPorCpfTitular(Cpf.of(entity.getCpf())).orElse(null);
        return ClienteMapper.toDomain(entity, conta);
    }
}
