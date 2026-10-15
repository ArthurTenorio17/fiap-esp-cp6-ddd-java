
package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.ConsultarStatusRequest;
import br.com.fiap.bank.plataforma.application.service.PlataformaCreditoService;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CreditoRestControllerWebTest {

    private PlataformaCreditoService plataformaService;
    private CoreBancarioGateway coreBancarioGateway;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        plataformaService = mock(PlataformaCreditoService.class);
        coreBancarioGateway = mock(CoreBancarioGateway.class);

        CreditoRestController controller =
                new CreditoRestController(
                        plataformaService,
                        coreBancarioGateway
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void deveListarSolicitacoesViaHttp() throws Exception {
        when(plataformaService.listarTodasSolicitacoes())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/solicitacoes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(plataformaService).listarTodasSolicitacoes();
    }

    @Test
    void deveConsultarSolicitacoesPorCpfViaHttp()
            throws Exception {

        String cpf = "12345678909";

        when(plataformaService.consultarStatusPorCpf(
                any(ConsultarStatusRequest.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .param("cpf", cpf))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(plataformaService).consultarStatusPorCpf(
                new ConsultarStatusRequest(cpf)
        );
    }

    @Test
    void deveConsultarClienteCoreViaHttp()
            throws Exception {

        String cpf = "12345678909";

        when(coreBancarioGateway.consultarContaCliente(cpf))
                .thenReturn(Map.of(
                        "cpf", cpf,
                        "saldo", 1500.00
                ));

        mockMvc.perform(get("/api/v1/contas/cliente/{cpf}", cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.saldo").value(1500.00));

        verify(coreBancarioGateway)
                .consultarContaCliente(cpf);
    }
}
