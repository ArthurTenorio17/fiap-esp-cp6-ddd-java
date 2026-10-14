package br.com.fiap.bank.core.domain.repository;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.vo.Cpf;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saida: a implementacao fica na camada de infraestrutura. */
public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    Optional<Cliente> buscarPorCpf(Cpf cpf);

    List<Cliente> listarTodos();

    boolean existePorCpf(Cpf cpf);

    void remover(UUID id);
}
