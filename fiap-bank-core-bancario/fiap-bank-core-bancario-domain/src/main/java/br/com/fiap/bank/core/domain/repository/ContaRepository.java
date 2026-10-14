package br.com.fiap.bank.core.domain.repository;

import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.vo.Cpf;

import java.util.List;
import java.util.Optional;

/** Porta de saida: a implementacao fica na camada de infraestrutura. */
public interface ContaRepository {

    Conta salvar(Conta conta);

    Optional<Conta> buscarPorNumero(String numero);

    Optional<Conta> buscarPorCpfTitular(Cpf cpf);

    List<Conta> listarTodas();

    boolean existePorNumero(String numero);
}
