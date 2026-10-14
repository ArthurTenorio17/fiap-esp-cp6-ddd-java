package br.com.fiap.bank.core.domain;

import br.com.fiap.bank.core.domain.exception.DominioException;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.model.TipoConta;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Email;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Cliente cliente() {
        return Cliente.cadastrar(Cpf.of("123.456.789-00"), "Ana Silva", Email.of("Ana@Exemplo.com"),
                LocalDate.of(1995, 5, 10), new BigDecimal("7500.00"));
    }

    @Test
    void cpfComMascaraEhNormalizado() {
        assertEquals("12345678900", cliente().getCpf().getValor());
        assertEquals("123.456.789-00", cliente().getCpf().formatado());
    }

    @Test
    void cpfInvalidoEhRejeitado() {
        assertThrows(DominioException.class, () -> Cpf.of("123"));
        assertThrows(DominioException.class, () -> Cpf.of("abcdefghijk"));
        assertThrows(DominioException.class, () -> Cpf.of(null));
    }

    @Test
    void emailEhNormalizadoEValidado() {
        assertEquals("ana@exemplo.com", cliente().getEmail().getValor());
        assertThrows(DominioException.class, () -> Email.of("sem-arroba"));
    }

    @Test
    void nascimentoNoFuturoEhRejeitado() {
        assertThrows(DominioException.class, () -> Cliente.cadastrar(Cpf.of("12345678900"), "Ana Silva",
                Email.of("a@b.com"), LocalDate.now().plusDays(1), BigDecimal.ONE));
    }

    @Test
    void vincularContaDeOutroTitularEhRejeitado() {
        Conta alheia = Conta.abrir("9-9", "0001", TipoConta.CORRENTE, Cpf.of("98765432100"), BigDecimal.ZERO);
        assertThrows(DominioException.class, () -> cliente().vincularConta(alheia));
    }

    @Test
    void naoPermiteSegundaContaAtiva() {
        Cliente cliente = cliente();
        cliente.vincularConta(Conta.abrir("1-1", "0001", TipoConta.CORRENTE, cliente.getCpf(), BigDecimal.ZERO));
        Conta segunda = Conta.abrir("2-2", "0001", TipoConta.POUPANCA, cliente.getCpf(), BigDecimal.ZERO);
        assertThrows(DominioException.class, () -> cliente.vincularConta(segunda));
    }

    @Test
    void permiteNovaContaDepoisDeEncerrarAAnterior() {
        Cliente cliente = cliente();
        Conta primeira = Conta.abrir("1-1", "0001", TipoConta.CORRENTE, cliente.getCpf(), BigDecimal.ZERO);
        cliente.vincularConta(primeira);
        primeira.encerrar();
        Conta segunda = Conta.abrir("2-2", "0001", TipoConta.POUPANCA, cliente.getCpf(), BigDecimal.ZERO);
        cliente.vincularConta(segunda);
        assertEquals("2-2", cliente.getConta().getNumero());
    }
}
