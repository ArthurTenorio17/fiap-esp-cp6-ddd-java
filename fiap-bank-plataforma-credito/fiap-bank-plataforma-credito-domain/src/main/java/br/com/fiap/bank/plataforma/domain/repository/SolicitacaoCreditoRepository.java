package br.com.fiap.bank.plataforma.domain.repository;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoCreditoRepository {

    SolicitacaoCredito salvar(SolicitacaoCredito solicitacao);

    Optional<SolicitacaoCredito> buscarPorId(Long id);

    List<SolicitacaoCredito> buscarPorCpfCliente(String cpfCliente);

    List<SolicitacaoCredito> listarTodas();
}
