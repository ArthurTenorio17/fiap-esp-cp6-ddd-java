package br.com.fiap.bank.mesa.config;

import br.com.fiap.bank.mesa.auth.TokenCookieService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FeignClientConfigTest {

    private final RequestInterceptor interceptor = new FeignClientConfig()
            .bearerTokenRequestInterceptor(new TokenCookieService(false));

    @AfterEach
    void limpar() {
        RequestContextHolder.resetRequestAttributes();
    }

    private RequestTemplate executar(String caminho) {
        RequestTemplate template = new RequestTemplate();
        template.uri(caminho);
        interceptor.apply(template);
        return template;
    }

    private void requisicaoAtual(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }

    @Test
    void devePropagarOTokenDoCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("MESA_ACCESS_TOKEN", "token-do-cookie"));
        requisicaoAtual(request);

        RequestTemplate template = executar("/api/credito/listarTodasSolicitacoes");

        assertEquals(
                List.of("Bearer token-do-cookie"),
                List.copyOf(template.headers().get("Authorization"))
        );
    }

    @Test
    void devePriorizarOHeaderAuthorizationRecebido() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-do-header");
        request.setCookies(new Cookie("MESA_ACCESS_TOKEN", "token-do-cookie"));
        requisicaoAtual(request);

        RequestTemplate template = executar("/api/credito/listarTodasSolicitacoes");

        assertEquals(
                List.of("Bearer token-do-header"),
                List.copyOf(template.headers().get("Authorization"))
        );
    }

    @Test
    void naoDeveEnviarTokenNoLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("MESA_ACCESS_TOKEN", "token-expirado"));
        requisicaoAtual(request);

        RequestTemplate template = executar("/api/auth/login");

        assertFalse(template.headers().containsKey("Authorization"));
    }

    @Test
    void naoDeveEnviarHeaderSemToken() {
        requisicaoAtual(new MockHttpServletRequest());

        RequestTemplate template = executar("/api/credito/listarTodasSolicitacoes");

        assertFalse(template.headers().containsKey("Authorization"));
    }

    @Test
    void naoDeveFalharForaDeUmaRequisicaoHttp() {
        RequestTemplate template = executar("/api/credito/listarTodasSolicitacoes");

        assertFalse(template.headers().containsKey("Authorization"));
    }
}
