package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DominioException;
import br.com.fiap.bank.core.domain.vo.Cpf;
import br.com.fiap.bank.core.domain.vo.Email;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Getter
public class Cliente {

    private final UUID id;
    private final Cpf cpf;
    private String nome;
    private Email email;
    private LocalDate dataNascimento;
    private BigDecimal rendaMensal;
    private Conta conta;

    private Cliente(UUID id, Cpf cpf, String nome, Email email, LocalDate dataNascimento,
                    BigDecimal rendaMensal, Conta conta) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.rendaMensal = rendaMensal;
        this.conta = conta;
    }

    public static Cliente cadastrar(Cpf cpf, String nome, Email email, LocalDate dataNascimento, BigDecimal rendaMensal) {
        validar(nome, dataNascimento, rendaMensal);
        return new Cliente(UUID.randomUUID(), cpf, nome.trim(), email, dataNascimento, rendaMensal, null);
    }

    public static Cliente reconstituir(UUID id, Cpf cpf, String nome, Email email, LocalDate dataNascimento,
                                       BigDecimal rendaMensal, Conta conta) {
        return new Cliente(id, cpf, nome, email, dataNascimento, rendaMensal, conta);
    }

    /** Substituicao total dos dados cadastrais (PUT). O CPF e o id nao mudam. */
    public void atualizarDados(String nome, Email email, LocalDate dataNascimento, BigDecimal rendaMensal) {
        validar(nome, dataNascimento, rendaMensal);
        this.nome = nome.trim();
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.rendaMensal = rendaMensal;
    }

    public void vincularConta(Conta novaConta) {
        if (this.conta != null && this.conta.estaAtiva()) {
            throw new DominioException("Cliente ja possui uma conta ativa");
        }
        if (!novaConta.getCpfTitular().equals(this.cpf)) {
            throw new DominioException("A conta pertence a outro titular");
        }
        this.conta = novaConta;
    }

    public int idade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    private static void validar(String nome, LocalDate dataNascimento, BigDecimal rendaMensal) {
        if (nome == null || nome.trim().length() < 3 || nome.trim().length() > 150) {
            throw new DominioException("Nome deve ter entre 3 e 150 caracteres");
        }
        if (dataNascimento == null || !dataNascimento.isBefore(LocalDate.now())) {
            throw new DominioException("Data de nascimento deve estar no passado");
        }
        if (rendaMensal == null || rendaMensal.signum() < 0) {
            throw new DominioException("Renda mensal nao pode ser negativa");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cliente outro && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
