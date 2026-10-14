package br.com.fiap.bank.core.infrastructure;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.model.TipoConta;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaRepository;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Email;
import br.com.fiap.bank.core.infrastructure.persistence.adapter.ClienteRepositoryAdapter;
import br.com.fiap.bank.core.infrastructure.persistence.adapter.ContaRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ClienteRepositoryAdapter.class, ContaRepositoryAdapter.class})
class RepositoryAdaptersTest {

    @Autowired
    ClienteRepository clientes;

    @Autowired
    ContaRepository contas;

    private Cliente novoCliente(String cpf) {
        return clientes.salvar(Cliente.cadastrar(Cpf.of(cpf), "Ana Silva", Email.of("ana@fiap.com.br"),
                LocalDate.of(1995, 5, 10), new BigDecimal("7500.00")));
    }

    @Test
    void salvaEBuscaClientePorCpfEId() {
        Cliente salvo = novoCliente("12345678900");

        assertTrue(clientes.existePorCpf(Cpf.of("123.456.789-00")));
        assertEquals(salvo.getId(), clientes.buscarPorCpf(Cpf.of("12345678900")).orElseThrow().getId());
        assertEquals("Ana Silva", clientes.buscarPorId(salvo.getId()).orElseThrow().getNome());
    }

    @Test
    void contaFicaVinculadaAoTitularEPersisteSaldo() {
        Cliente cliente = novoCliente("98765432100");
        Conta conta = Conta.abrir("78901-2", null, TipoConta.CORRENTE, cliente.getCpf(), new BigDecimal("100"));
        contas.salvar(conta);

        conta.depositar(new BigDecimal("25.50"));
        contas.salvar(conta);

        Conta lida = contas.buscarPorCpfTitular(cliente.getCpf()).orElseThrow();
        assertEquals(new BigDecimal("125.50"), lida.getSaldo().getValor());
        assertEquals("0001", lida.getAgencia());
        assertNotNull(clientes.buscarPorCpf(cliente.getCpf()).orElseThrow().getConta());
    }

    @Test
    void removerClienteApagaAsContasDele() {
        Cliente cliente = novoCliente("11122233344");
        contas.salvar(Conta.abrir("1-1", "0001", TipoConta.POUPANCA, cliente.getCpf(), BigDecimal.ZERO));

        clientes.remover(cliente.getId());

        assertTrue(clientes.buscarPorId(cliente.getId()).isEmpty());
        assertFalse(contas.existePorNumero("1-1"));
    }
}
