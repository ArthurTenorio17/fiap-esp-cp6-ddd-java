package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.AprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.ReprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponseDTO;
import br.com.fiap.bank.plataforma.application.service.PlataformaCreditoService;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Cobre POST e PUT da API REST (/api/v1), incluindo a validacao do corpo. */
class CreditoRestControllerEscritaWebTest {

    private PlataformaCreditoService plataformaService;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        plataformaService = mock(PlataformaCreditoService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CreditoRestController(
                        plataformaService, mock(CoreBancarioGateway.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private SolicitacaoCreditoResponseDTO resposta(StatusSolicitacao status) {
        return new SolicitacaoCreditoResponseDTO(
                5L, "12345678900", "Ana Silva", "PESSOAL",
                new BigDecimal("5000.00"), 12,
                new BigDecimal("4500.00"), 700, status,
                null, false, 650, null, null);
    }

    @Test
    void deveCriarSolicitacaoComStatus201() throws Exception {
        when(plataformaService.solicitarCredito(any()))
                .thenReturn(resposta(StatusSolicitacao.EM_ANALISE));

        String corpo = """
                {
                  "cpfCliente": "12345678900",
                  "nomeCliente": "Ana Silva",
                  "tipoCredito": "PESSOAL",
                  "valorSolicitado": 5000.00,
                  "quantidadeParcelas": 12,
                  "rendaMensal": 4500.00
                }
                """;

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/solicitacoes/5"));
    }

    @Test
    void deveRetornar400AoCriarSolicitacaoInvalida() throws Exception {
        String corpo = """
                {
                  "cpfCliente": "123",
                  "nomeCliente": "",
                  "valorSolicitado": -10
                }
                """;

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(plataformaService);
    }

    @Test
    void deveAprovarComPutUsandoIdDaUrl() throws Exception {
        when(plataformaService.aprovarSolicitacao(
                new AprovarSolicitacaoRequest(5L, "dentro da politica")))
                .thenReturn(resposta(StatusSolicitacao.APROVADO));

        mockMvc.perform(put("/api/v1/solicitacoes/5/aprovacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parecerOperador\":\"dentro da politica\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void deveReprovarComPutUsandoIdDaUrl() throws Exception {
        when(plataformaService.reprovarSolicitacao(
                new ReprovarSolicitacaoRequest(5L, "renda insuficiente")))
                .thenReturn(resposta(StatusSolicitacao.REPROVADO));

        mockMvc.perform(put("/api/v1/solicitacoes/5/reprovacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivoReprovacao\":\"renda insuficiente\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADO"));
    }

    @Test
    void deveRetornar400QuandoParecerExcederLimite() throws Exception {
        String parecerGrande = "x".repeat(471);

        mockMvc.perform(put("/api/v1/solicitacoes/5/aprovacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parecerOperador\":\"" + parecerGrande + "\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(plataformaService);
    }
}
