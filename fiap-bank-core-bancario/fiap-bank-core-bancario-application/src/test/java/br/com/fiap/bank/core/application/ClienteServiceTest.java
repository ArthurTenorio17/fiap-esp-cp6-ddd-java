package br.com.fiap.bank.core.application;

import br.com.fiap.bank.core.application.dto.ClienteResponse;
import br.com.fiap.bank.core.application.dto.CriarClienteRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.exception.RequisicaoInvalidaException;
import br.com.fiap.bank.core.application.service.ClienteService;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    ClienteRepository repository;

    @InjectMocks
    ClienteService service;

    private CriarClienteRequest request(String cpf) {
        return new CriarClienteRequest(cpf, "Ana Silva", "ana@fiap.com.br",
                LocalDate.of(1995, 5, 10), new BigDecimal("7500.00"));
    }

    private Cliente clienteExistente() {
        return Cliente.cadastrar(Cpf.of("12345678900"), "Ana Silva", Email.of("ana@fiap.com.br"),
                LocalDate.of(1995, 5, 10), new BigDecimal("7500.00"));
    }

    @Test
    void criaClienteNormalizandoCpfComMascara() {
        when(repository.existePorCpf(any())).thenReturn(false);
        when(repository.salvar(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse resposta = service.criar(request("123.456.789-00"));

        assertEquals("12345678900", resposta.cpf());
        assertNull(resposta.conta());
    }

    @Test
    void cpfDuplicadoGeraConflito() {
        when(repository.existePorCpf(any())).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.criar(request("12345678900")));
        verify(repository, never()).salvar(any());
    }

    @Test
    void buscarPorIdInexistenteLancaNaoEncontrado() {
        when(repository.buscarPorId(any())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(UUID.randomUUID()));
    }

    @Test
    void buscarPorCpfInvalidoLancaErroDeDominio() {
        assertThrows(RequisicaoInvalidaException.class, () -> service.buscarPorCpf("123"));
    }

    @Test
    void atualizarSubstituiDadosMantendoOCpf() {
        Cliente existente = clienteExistente();
        when(repository.buscarPorId(existente.getId())).thenReturn(Optional.of(existente));
        when(repository.salvar(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        CriarClienteRequest novo = new CriarClienteRequest("12345678900", "Ana Maria Silva", "ana.maria@fiap.com.br",
                LocalDate.of(1995, 5, 10), new BigDecimal("9000.00"));
        ClienteResponse resposta = service.atualizar(existente.getId(), novo);

        assertEquals("Ana Maria Silva", resposta.nome());
        assertEquals("ana.maria@fiap.com.br", resposta.email());
        assertEquals(new BigDecimal("9000.00"), resposta.rendaMensal());
    }

    @Test
    void atualizarComCpfDiferenteEhRejeitado() {
        Cliente existente = clienteExistente();
        when(repository.buscarPorId(existente.getId())).thenReturn(Optional.of(existente));

        assertThrows(RequisicaoInvalidaException.class, () -> service.atualizar(existente.getId(), request("98765432100")));
        verify(repository, never()).salvar(any());
    }

    @Test
    void removerInexistenteLancaNaoEncontrado() {
        when(repository.buscarPorId(any())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.remover(UUID.randomUUID()));
        verify(repository, never()).remover(any());
    }

    @Test
    void removerExistenteChamaORepositorio() {
        Cliente existente = clienteExistente();
        when(repository.buscarPorId(existente.getId())).thenReturn(Optional.of(existente));

        service.remover(existente.getId());

        verify(repository).remover(existente.getId());
    }
}
