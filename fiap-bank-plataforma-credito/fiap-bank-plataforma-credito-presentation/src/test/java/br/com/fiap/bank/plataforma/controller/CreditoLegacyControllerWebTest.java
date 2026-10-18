package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.AprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.ConsultarStatusRequest;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Garante que as rotas antigas (POST /api/credito/*) continuam respondendo
 * como o Angular e a Mesa de Operacoes esperam.
 */
class CreditoLegacyControllerWebTest {

    private static final String CPF = "12345678900";

    private PlataformaCreditoService plataformaService;
    private CoreBancarioGateway coreBancarioGateway;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        plataformaService = mock(PlataformaCreditoService.class);
        coreBancarioGateway = mock(CoreBancarioGateway.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CreditoLegacyController(
                        plataformaService, coreBancarioGateway))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private SolicitacaoCreditoResponseDTO respostaPadrao(
            StatusSolicitacao status) {
        return new SolicitacaoCreditoResponseDTO(
                1L, CPF, "Ana Silva", "PESSOAL",
                new BigDecimal("5000.00"), 12,
                new BigDecimal("4500.00"), 700, status,
                null, false, 650, null, null);
    }

    @Test
    void deveManterRotaListarTodasSolicitacoes() throws Exception {
        when(plataformaService.listarTodasSolicitacoes())
                .thenReturn(List.of());

        mockMvc.perform(post("/api/credito/listarTodasSolicitacoes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deveManterRotaConsultarClienteCore() throws Exception {
        when(coreBancarioGateway.consultarContaCliente(CPF))
                .thenReturn(Map.of("cpf", CPF, "saldo", 12500.00));

        mockMvc.perform(post("/api/credito/consultarClienteCore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCliente\":\"" + CPF + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(CPF))
                .andExpect(jsonPath("$.saldo").value(12500.00));
    }

    @Test
    void deveManterRotaSolicitarCredito() throws Exception {
        when(plataformaService.solicitarCredito(any()))
                .thenReturn(respostaPadrao(StatusSolicitacao.EM_ANALISE));

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

        mockMvc.perform(post("/api/credito/solicitarCredito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));
    }

    @Test
    void deveManterRotaConsultarStatusCredito() throws Exception {
        when(plataformaService.consultarStatusPorCpf(
                new ConsultarStatusRequest(CPF)))
                .thenReturn(List.of(
                        respostaPadrao(StatusSolicitacao.EM_ANALISE)));

        mockMvc.perform(post("/api/credito/consultarStatusCredito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCliente\":\"" + CPF + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpfCliente").value(CPF));
    }

    @Test
    void deveManterRotaAprovarSolicitacao() throws Exception {
        when(plataformaService.aprovarSolicitacao(
                new AprovarSolicitacaoRequest(1L, "ok")))
                .thenReturn(respostaPadrao(StatusSolicitacao.APROVADO));

        mockMvc.perform(post("/api/credito/aprovarSolicitacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitacaoId\":1,\"parecerOperador\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void deveRetornar400QuandoCpfForInvalido() throws Exception {
        mockMvc.perform(post("/api/credito/consultarStatusCredito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCliente\":\"abc\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(plataformaService);
    }
}
