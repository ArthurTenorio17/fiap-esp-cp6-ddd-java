package br.com.fiap.bank.plataforma.infrastructure.persistence.adapter;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;
import br.com.fiap.bank.plataforma.infrastructure.persistence.mapper.SolicitacaoCreditoMapper;
import br.com.fiap.bank.plataforma.infrastructure.persistence.repository.SolicitacaoCreditoJpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitacaoCreditoRepositoryAdapter
        implements SolicitacaoCreditoRepository {

    private final SolicitacaoCreditoJpaRepository jpaRepository;

    public SolicitacaoCreditoRepositoryAdapter(
            SolicitacaoCreditoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SolicitacaoCredito salvar(SolicitacaoCredito solicitacao) {
        var entity = SolicitacaoCreditoMapper.paraJpa(solicitacao);
        var salvo = jpaRepository.save(entity);
        return SolicitacaoCreditoMapper.paraDominio(salvo);
    }

    @Override
    public Optional<SolicitacaoCredito> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(SolicitacaoCreditoMapper::paraDominio);
    }

    @Override
    public List<SolicitacaoCredito> buscarPorCpfCliente(String cpfCliente) {
        return jpaRepository
                .findByCpfClienteOrderByDataSolicitacaoDesc(cpfCliente)
                .stream()
                .map(SolicitacaoCreditoMapper::paraDominio)
                .toList();
    }

    @Override
    public List<SolicitacaoCredito> listarTodas() {
        return jpaRepository
                .findAllByOrderByDataSolicitacaoDesc()
                .stream()
                .map(SolicitacaoCreditoMapper::paraDominio)
                .toList();
    }
}
