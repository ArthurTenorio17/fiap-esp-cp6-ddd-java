package br.com.fiap.bank.core.presentation;

import br.com.fiap.bank.core.application.dto.ContaResponse;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.service.ContaService;
import br.com.fiap.bank.core.presentation.controller.ContaController;
import br.com.fiap.bank.core.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContaControllerTest {

    @Mock
    ContaService service;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ContaController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ContaResponse resposta(String saldo) {
        return new ContaResponse(UUID.randomUUID(), "12345-6", "0001", "CORRENTE",
                "ATIVA", new BigDecimal(saldo), LocalDate.now(), "12345678900");
    }

    @Test
    void getListaDevolve200() throws Exception {
        when(service.listar()).thenReturn(List.of(resposta("100.00")));

        mvc.perform(get("/api/v1/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroConta").value("12345-6"));
    }

    @Test
    void getPorNumeroExpoeOsCamposQueOMobileConsome() throws Exception {
        when(service.buscarPorNumero("12345-6")).thenReturn(resposta("12500.00"));

        mvc.perform(get("/api/v1/contas/{numero}", "12345-6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroConta").value("12345-6"))
                .andExpect(jsonPath("$.agencia").value("0001"))
                .andExpect(jsonPath("$.statusConta").value("ATIVA"))
                .andExpect(jsonPath("$.saldo").value(12500.00));
    }

    @Test
    void getPorCpfDoTitularDevolve200() throws Exception {
        when(service.buscarPorCpfTitular("12345678900")).thenReturn(resposta("10.00"));

        mvc.perform(get("/api/v1/contas/cliente/{cpf}", "12345678900"))
                .andExpect(status().isOk());
    }

    @Test
    void getInexistenteDevolve404() throws Exception {
        when(service.buscarPorNumero("0-0")).thenThrow(new RecursoNaoEncontradoException("Conta nao encontrada: 0-0"));

        mvc.perform(get("/api/v1/contas/{numero}", "0-0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postValidoDevolve201() throws Exception {
        when(service.abrir(any())).thenReturn(resposta("100.00"));

        mvc.perform(post("/api/v1/contas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpfCliente":"12345678900","tipoConta":"CORRENTE","saldoInicial":100.00}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/contas/12345-6")));
    }

    @Test
    void postComTipoDeContaDesconhecidoDevolve400() throws Exception {
        mvc.perform(post("/api/v1/contas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpfCliente":"12345678900","tipoConta":"INVESTIMENTO","saldoInicial":100.00}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postParaClienteQueJaTemContaDevolve409() throws Exception {
        when(service.abrir(any())).thenThrow(new ConflitoException("ja possui conta ativa"));

        mvc.perform(post("/api/v1/contas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpfCliente":"12345678900","tipoConta":"POUPANCA","saldoInicial":0}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void putSaldoDevolve200() throws Exception {
        when(service.atualizarSaldo(any(), any())).thenReturn(resposta("150.00"));

        mvc.perform(put("/api/v1/contas/{numero}/saldo", "12345-6").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operacao":"DEPOSITO","valor":50.00}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(150.00));
    }

    @Test
    void putSaldoComOperacaoInvalidaDevolve400() throws Exception {
        mvc.perform(put("/api/v1/contas/{numero}/saldo", "12345-6").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operacao":"PIX","valor":50.00}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saqueSemSaldoDevolve409() throws Exception {
        when(service.atualizarSaldo(any(), any())).thenThrow(new ConflitoException("Saldo insuficiente"));

        mvc.perform(put("/api/v1/contas/{numero}/saldo", "12345-6").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"operacao":"SAQUE","valor":999999}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteDevolve204() throws Exception {
        mvc.perform(delete("/api/v1/contas/{numero}", "12345-6"))
                .andExpect(status().isNoContent());
        verify(service).encerrar("12345-6");
    }
}
