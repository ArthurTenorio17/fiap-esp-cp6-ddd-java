package br.com.fiap.bank.core.infrastructure.persistence.adapter;

import br.com.fiap.bank.core.domain.exception.DominioException;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.repository.ContaRepository;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaJpaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.mapper.ContaMapper;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ClienteJpaRepository;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ContaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContaRepositoryAdapter implements ContaRepository {

    private final ContaJpaRepository contaJpa;
    private final ClienteJpaRepository clienteJpa;

    @Override
    @Transactional
    public Conta salvar(Conta conta) {
        ClienteJpaEntity titular = clienteJpa.findByCpf(conta.getCpfTitular().getValor())
                .orElseThrow(() -> new DominioException("Titular nao encontrado para o CPF " + conta.getCpfTitular()));
        ContaJpaEntity salva = contaJpa.save(ContaMapper.toEntity(conta, titular));
        return ContaMapper.toDomain(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conta> buscarPorNumero(String numero) {
        return contaJpa.findByNumero(numero).map(ContaMapper::toDomain);
    }

    /** Devolve a conta ativa do titular; se nao houver, a mais recente. */
    @Override
    @Transactional(readOnly = true)
    public Optional<Conta> buscarPorCpfTitular(Cpf cpf) {
        List<ContaJpaEntity> contas = contaJpa.findByClienteCpf(cpf.getValor());
        return contas.stream()
                .filter(c -> c.getStatus() == StatusConta.ATIVA)
                .findFirst()
                .or(() -> contas.stream().max(Comparator.comparing(ContaJpaEntity::getDataAbertura)))
                .map(ContaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conta> listarTodas() {
        return contaJpa.findAll().stream().map(ContaMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNumero(String numero) {
        return contaJpa.existsByNumero(numero);
    }
}
