package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.dto.CriarClienteRequest;
import br.com.fiap.bank.core.application.dto.CriarContaRequest;
import br.com.fiap.bank.core.domain.model.TipoConta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados de desenvolvimento. Os CPFs sao os mesmos usados pela Plataforma de Credito.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargaInicialService {

    private final ClienteService clienteService;
    private final ContaService contaService;

    public void carregar() {
        if (!clienteService.listar().isEmpty()) {
            return;
        }

        cadastrar("12345678900", "Ana Silva", "ana.silva@fiap.com.br", LocalDate.of(1995, 5, 10),
                "7500.00", "12345-6", "12500.00");
        cadastrar("98765432100", "Carlos Eduardo Oliveira", "carlos.oliveira@fiap.com.br", LocalDate.of(1988, 11, 2),
                "12000.00", "78901-2", "45800.50");

        log.info("Core Bancario: dados iniciais carregados");
    }

    private void cadastrar(String cpf, String nome, String email, LocalDate nascimento,
                           String renda, String numeroConta, String saldoInicial) {
        clienteService.criar(new CriarClienteRequest(cpf, nome, email, nascimento, new BigDecimal(renda)));
        contaService.abrirComNumero(
                new CriarContaRequest(cpf, "0001", TipoConta.CORRENTE, new BigDecimal(saldoInicial)),
                numeroConta);
    }
}
