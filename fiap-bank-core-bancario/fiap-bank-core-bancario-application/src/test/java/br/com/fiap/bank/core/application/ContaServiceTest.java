
package br.com.fiap.bank.core.application;

import br.com.fiap.bank.core.application.dto.AtualizarSaldoRequest;
import br.com.fiap.bank.core.application.dto.ContaResponse;
import br.com.fiap.bank.core.application.dto.CriarContaRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.exception.RequisicaoInvalidaException;
import br.com.fiap.bank.core.application.service.ContaService;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.model.TipoConta;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    ContaRepository contaRepository;

    @Mock
    ClienteRepository clienteRepository;

    @InjectMocks
    ContaService service;

    private Cliente cliente() {
        return Cliente.cadastrar(
                Cpf.of("12345678900"),
                "Ana Silva",
                Email.of("ana@fiap.com.br"),
                LocalDate.of(1995, 5, 10),
                new BigDecimal("7500.00")
        );
    }

    private CriarContaRequest pedidoDeConta() {
        return new CriarContaRequest(
                "12345678900",
                null,
                TipoConta.CORRENTE,
                new BigDecimal("100.00")
        );
    }

    private Conta contaAtiva(String saldo) {
        return Conta.abrir(
                "12345-6",
                "0001",
                TipoConta.CORRENTE,
                Cpf.of("12345678900"),
                new BigDecimal(saldo)
        );
    }

    @Test
    void abreContaComAgenciaPadraoEVinculaAoCliente() {
        when(clienteRepository.buscarPorCpf(any()))
                .thenReturn(Optional.of(cliente()));

        when(contaRepository.salvar(any(Conta.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ContaResponse resposta = service.abrir(pedidoDeConta());

        assertEquals("0001", resposta.agencia());
        assertEquals("ATIVA", resposta.statusConta());
        assertEquals(new BigDecimal("100.00"), resposta.saldo());
        assertTrue(resposta.numeroConta().matches("\\d{5}-\\d"));
    }

    @Test
    void abrirParaClienteInexistenteEhRejeitado() {
        when(clienteRepository.buscarPorCpf(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                RequisicaoInvalidaException.class,
                () -> service.abrir(pedidoDeConta())
        );

        verify(contaRepository, never()).salvar(any());
    }

    @Test
    void clienteComContaAtivaGeraConflito() {
        Cliente comConta = cliente();
        comConta.vincularConta(contaAtiva("10"));

        when(clienteRepository.buscarPorCpf(any()))
                .thenReturn(Optional.of(comConta));

        assertThrows(
                ConflitoException.class,
                () -> service.abrir(pedidoDeConta())
        );
    }

    @Test
    void numeroDesejadoJaExistenteGeraConflito() {
        when(clienteRepository.buscarPorCpf(any()))
                .thenReturn(Optional.of(cliente()));

        when(contaRepository.existePorNumero("12345-6"))
                .thenReturn(true);

        assertThrows(
                ConflitoException.class,
                () -> service.abrirComNumero(
                        pedidoDeConta(),
                        "12345-6"
                )
        );
    }

    @Test
    void depositoESaqueSaoCaseInsensitive() {
        Conta conta = contaAtiva("100.00");

        when(contaRepository.buscarPorNumero("12345-6"))
                .thenReturn(Optional.of(conta));

        when(contaRepository.salvar(any(Conta.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.atualizarSaldo(
                "12345-6",
                new AtualizarSaldoRequest(
                        "deposito",
                        new BigDecimal("50")
                )
        );

        ContaResponse resposta = service.atualizarSaldo(
                "12345-6",
                new AtualizarSaldoRequest(
                        "SAQUE",
                        new BigDecimal("30")
                )
        );

        assertEquals(new BigDecimal("120.00"), resposta.saldo());
    }

    @Test
    void saqueAcimaDoSaldoLancaSaldoInsuficiente() {
        when(contaRepository.buscarPorNumero("12345-6"))
                .thenReturn(Optional.of(contaAtiva("10.00")));

        assertThrows(
                ConflitoException.class,
                () -> service.atualizarSaldo(
                        "12345-6",
                        new AtualizarSaldoRequest(
                                "SAQUE",
                                new BigDecimal("10.01")
                        )
                )
        );

        verify(contaRepository, never()).salvar(any());
    }

    @Test
    void operacaoInvalidaNaoPodeSerInterpretadaComoSaque() {
        Conta conta = contaAtiva("100.00");

        when(contaRepository.buscarPorNumero("12345-6"))
                .thenReturn(Optional.of(conta));

        assertThrows(
                RequisicaoInvalidaException.class,
                () -> service.atualizarSaldo(
                        "12345-6",
                        new AtualizarSaldoRequest(
                                "TRANSFERENCIA",
                                new BigDecimal("50.00")
                        )
                )
        );

        assertEquals(
                new BigDecimal("100.00"),
                ContaResponse.de(conta).saldo()
        );

        verify(contaRepository, never()).salvar(any());
    }

    @Test
    void contaInexistenteLancaNaoEncontrado() {
        when(contaRepository.buscarPorNumero("0-0"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.buscarPorNumero("0-0")
        );

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.encerrar("0-0")
        );
    }

    @Test
    void encerrarMarcaStatusEPersiste() {
        Conta conta = contaAtiva("10.00");

        when(contaRepository.buscarPorNumero("12345-6"))
                .thenReturn(Optional.of(conta));

        service.encerrar("12345-6");

        assertEquals(StatusConta.ENCERRADA, conta.getStatus());
        verify(contaRepository).salvar(conta);
    }
}
