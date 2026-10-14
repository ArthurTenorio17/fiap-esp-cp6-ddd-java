package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.ContaInativaException;
import br.com.fiap.bank.core.domain.exception.DominioException;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Saldo;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Conta bancaria. O titular e referenciado pelo CPF (identidade de negocio) para
 * nao criar dependencia circular com o agregado Cliente.
 */
@Getter
public class Conta {

    public static final String AGENCIA_PADRAO = "0001";

    private final UUID id;
    private final String numero;
    private final String agencia;
    private final TipoConta tipo;
    private final Cpf cpfTitular;
    private final LocalDate dataAbertura;
    private Saldo saldo;
    private StatusConta status;

    private Conta(UUID id, String numero, String agencia, TipoConta tipo, Cpf cpfTitular,
                  Saldo saldo, StatusConta status, LocalDate dataAbertura) {
        this.id = id;
        this.numero = numero;
        this.agencia = agencia;
        this.tipo = tipo;
        this.cpfTitular = cpfTitular;
        this.saldo = saldo;
        this.status = status;
        this.dataAbertura = dataAbertura;
    }

    /** Abertura de uma conta nova (nasce ATIVA). */
    public static Conta abrir(String numero, String agencia, TipoConta tipo, Cpf cpfTitular, BigDecimal saldoInicial) {
        if (numero == null || numero.isBlank()) {
            throw new DominioException("Numero da conta e obrigatorio");
        }
        if (tipo == null) {
            throw new DominioException("Tipo da conta e obrigatorio");
        }
        Objects.requireNonNull(cpfTitular, "CPF do titular e obrigatorio");
        String ag = (agencia == null || agencia.isBlank()) ? AGENCIA_PADRAO : agencia;
        if (!ag.matches("\\d{4}")) {
            throw new DominioException("Agencia deve ter exatamente 4 digitos");
        }
        return new Conta(UUID.randomUUID(), numero, ag, tipo, cpfTitular,
                Saldo.of(saldoInicial), StatusConta.ATIVA, LocalDate.now());
    }

    /** Reconstroi uma conta ja persistida (usado pelos adapters de infraestrutura). */
    public static Conta reconstituir(UUID id, String numero, String agencia, TipoConta tipo, Cpf cpfTitular,
                                     Saldo saldo, StatusConta status, LocalDate dataAbertura) {
        return new Conta(id, numero, agencia, tipo, cpfTitular, saldo, status, dataAbertura);
    }

    public void depositar(BigDecimal valor) {
        exigirAtiva();
        this.saldo = saldo.adicionar(valor);
    }

    public void sacar(BigDecimal valor) {
        exigirAtiva();
        this.saldo = saldo.subtrair(valor);
    }

    public void bloquear() {
        exigirAtiva();
        this.status = StatusConta.BLOQUEADA;
    }

    /** Inativacao logica. Chamar duas vezes nao muda nada (idempotente). */
    public void encerrar() {
        this.status = StatusConta.ENCERRADA;
    }

    public boolean estaAtiva() {
        return status == StatusConta.ATIVA;
    }

    private void exigirAtiva() {
        if (!estaAtiva()) {
            throw new ContaInativaException(numero);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Conta outra && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
