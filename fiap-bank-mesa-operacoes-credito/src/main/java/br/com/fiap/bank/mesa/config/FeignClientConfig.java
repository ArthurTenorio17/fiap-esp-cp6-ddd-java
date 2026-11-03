package br.com.fiap.bank.mesa.config;

import br.com.fiap.bank.mesa.auth.TokenCookieService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propaga o Bearer Token da requisicao atual para a Plataforma de Credito.
 */
@Configuration
public class FeignClientConfig {

    private static final String PREFIXO_BEARER = "Bearer ";
    private static final String ROTA_PUBLICA_AUTH = "/api/auth/";

    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor(
            TokenCookieService tokenCookieService) {

        return (RequestTemplate template) -> {

            // O Resource Server rejeita um Bearer invalido ate em rotas publicas:
            // um cookie expirado nao pode impedir um novo login.
            if (template.path().startsWith(ROTA_PUBLICA_AUTH)) {
                return;
            }

            if (!(RequestContextHolder.getRequestAttributes()
                    instanceof ServletRequestAttributes attributes)) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // 1. Cliente HTTP que ja envia o header (Postman, curl).
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (header != null && header.startsWith(PREFIXO_BEARER)) {
                template.header(HttpHeaders.AUTHORIZATION, header);
                return;
            }

            // 2. Navegador: o token do login esta no cookie HttpOnly.
            tokenCookieService.ler(request).ifPresent(token ->
                    template.header(
                            HttpHeaders.AUTHORIZATION,
                            PREFIXO_BEARER + token
                    )
            );
        };
    }
}
