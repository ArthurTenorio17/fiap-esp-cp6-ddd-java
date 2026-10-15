package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RequisicaoInvalidaException;
import br.com.fiap.bank.core.domain.exception.ContaInativaException;
import br.com.fiap.bank.core.domain.exception.DominioException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;

import java.util.function.Supplier;

/**
 * Converte as excecoes do dominio em excecoes da camada de aplicacao, para que a
 * apresentacao nunca precise importar classes do dominio.
 */
final class TradutorDeErros {

    private TradutorDeErros() {
    }

    static <T> T executar(Supplier<T> acao) {
        try {
            return acao.get();
        } catch (SaldoInsuficienteException | ContaInativaException e) {
            throw new ConflitoException(e.getMessage());
        } catch (DominioException e) {
            throw new RequisicaoInvalidaException(e.getMessage());
        }
    }

    static void executar(Runnable acao) {
        executar(() -> {
            acao.run();
            return null;
        });
    }
}
