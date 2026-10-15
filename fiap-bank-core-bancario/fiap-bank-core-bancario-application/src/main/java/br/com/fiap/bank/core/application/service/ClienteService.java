package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.dto.ClienteResponse;
import br.com.fiap.bank.core.application.dto.CriarClienteRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.exception.RequisicaoInvalidaException;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse criar(CriarClienteRequest request) {
        return TradutorDeErros.executar(() -> {
            Cpf cpf = Cpf.of(request.cpf());
            if (clienteRepository.existePorCpf(cpf)) {
                throw new ConflitoException("Ja existe cliente cadastrado com o CPF " + cpf);
            }
            Cliente cliente = Cliente.cadastrar(cpf, request.nome(), Email.of(request.email()),
                    request.dataNascimento(), request.rendaMensal());
            return ClienteResponse.de(clienteRepository.salvar(cliente));
        });
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.listarTodos().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(UUID id) {
        return ClienteResponse.de(obterPorId(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorCpf(String cpf) {
        return TradutorDeErros.executar(() -> {
            Cpf documento = Cpf.of(cpf);
            return clienteRepository.buscarPorCpf(documento)
                    .map(ClienteResponse::de)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado para o CPF " + documento));
        });
    }

    /** PUT: substitui todos os dados cadastrais. O CPF identifica o cliente e nao pode mudar. */
    @Transactional
    public ClienteResponse atualizar(UUID id, CriarClienteRequest request) {
        Cliente cliente = obterPorId(id);
        return TradutorDeErros.executar(() -> {
            if (!cliente.getCpf().equals(Cpf.of(request.cpf()))) {
                throw new RequisicaoInvalidaException("O CPF de um cliente existente nao pode ser alterado");
            }
            cliente.atualizarDados(request.nome(), Email.of(request.email()),
                    request.dataNascimento(), request.rendaMensal());
            return ClienteResponse.de(clienteRepository.salvar(cliente));
        });
    }

    @Transactional
    public void remover(UUID id) {
        obterPorId(id);
        clienteRepository.remover(id);
    }

    private Cliente obterPorId(UUID id) {
        return clienteRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + id));
    }
}
