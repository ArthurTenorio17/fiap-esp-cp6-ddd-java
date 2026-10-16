
package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.ConsultarStatusRequest;
import br.com.fiap.bank.plataforma.application.service.PlataformaCreditoService;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreditoRestControllerTest {

    private PlataformaCreditoService plataformaService;
    private CoreBancarioGateway coreBancarioGateway;
    private CreditoRestController controller;

    @BeforeEach
    void configurar() {
        plataformaService = mock(PlataformaCreditoService.class);
        coreBancarioGateway = mock(CoreBancarioGateway.class);

        controller = new CreditoRestController(
                plataformaService,
                coreBancarioGateway
        );
    }

    @Test
    void deveListarTodasSolicitacoes() {
        when(plataformaService.listarTodasSolicitacoes())
                .thenReturn(List.of());

        var resposta = controller.listarTodas();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertTrue(resposta.getBody().isEmpty());

        verify(plataformaService).listarTodasSolicitacoes();
    }

    @Test
    void deveConsultarSolicitacoesPorCpf() {
        String cpf = "12345678909";

        when(plataformaService.consultarStatusPorCpf(
                any(ConsultarStatusRequest.class)))
                .thenReturn(List.of());

        var resposta = controller.consultarPorCpf(cpf);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertTrue(resposta.getBody().isEmpty());

        verify(plataformaService).consultarStatusPorCpf(
                new ConsultarStatusRequest(cpf)
        );
    }

    @Test
    void deveConsultarClienteNoCoreBancario() {
        String cpf = "12345678909";

        Map<String, Object> dados = Map.of(
                "cpf", cpf,
                "saldo", 1500.00
        );

        when(coreBancarioGateway.consultarContaCliente(cpf))
                .thenReturn(dados);

        var resposta = controller.consultarClienteCore(cpf);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(dados, resposta.getBody());

        verify(coreBancarioGateway)
                .consultarContaCliente(cpf);
    }
}
