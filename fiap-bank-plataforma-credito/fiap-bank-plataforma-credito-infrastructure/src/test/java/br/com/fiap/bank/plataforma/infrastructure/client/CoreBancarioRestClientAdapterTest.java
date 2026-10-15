package br.com.fiap.bank.plataforma.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class CoreBancarioRestClientAdapterTest {

    private MockRestServiceServer server;
    private CoreBancarioRestClientAdapter adapter;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer
                .bindTo(builder)
                .build();

        adapter = new CoreBancarioRestClientAdapter(
                builder,
                "http://localhost:8080/api/v1"
        );
    }

    @Test
    void deveConsultarContaComSucesso() {
        server.expect(requestTo(
                        "http://localhost:8080/api/v1/contas/cliente/12345678901"
                ))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        """
                        {
                          "statusConta": "ATIVA",
                          "saldo": 2500.00,
                          "agencia": "0001",
                          "numeroConta": "12345-6"
                        }
                        """,
                        APPLICATION_JSON
                ));

        Map<String, Object> resposta =
                adapter.consultarContaCliente("12345678901");

        assertEquals("ATIVA", resposta.get("statusConta"));
        assertEquals(2500.0,
                ((Number) resposta.get("saldo")).doubleValue(),
                0.001);
        assertEquals("0001", resposta.get("agencia"));

        server.verify();
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoExiste() {
        server.expect(requestTo(
                        "http://localhost:8080/api/v1/contas/cliente/00000000000"
                ))
                .andRespond(withResourceNotFound());

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> adapter.consultarContaCliente("00000000000")
        );

        assertEquals(
                "Falha ao consultar a conta do cliente no Core Bancario.",
                excecao.getMessage()
        );

        server.verify();
    }

    @Test
    void deveLancarExcecaoQuandoCoreEstaIndisponivel() {
        server.expect(requestTo(
                        "http://localhost:8080/api/v1/contas/cliente/12345678901"
                ))
                .andRespond(withServerError());

        assertThrows(
                IllegalStateException.class,
                () -> adapter.consultarContaCliente("12345678901")
        );

        server.verify();
    }

    @Test
    void deveLancarExcecaoQuandoRespostaEstaVazia() {
        server.expect(requestTo(
                        "http://localhost:8080/api/v1/contas/cliente/12345678901"
                ))
                .andRespond(withSuccess(
                        "",
                        APPLICATION_JSON
                ));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> adapter.consultarContaCliente("12345678901")
        );

        assertEquals(
                "O Core Bancario retornou uma resposta vazia.",
                excecao.getMessage()
        );

        server.verify();
    }
}
