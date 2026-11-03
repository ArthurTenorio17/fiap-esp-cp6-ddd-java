package br.com.fiap.bank.mesa.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Guarda o JWT do operador no navegador, sem HttpSession.
 * HttpOnly impede a leitura por JavaScript; SameSite=Strict impede que outro
 * site dispare aprovacoes em nome do operador.
 */
@Component
public class TokenCookieService {

    static final String NOME_COOKIE = "MESA_ACCESS_TOKEN";

    private final boolean secure;

    public TokenCookieService(
            @Value("${mesa.auth.cookie-secure:false}") boolean secure) {
        this.secure = secure;
    }

    public void salvar(
            HttpServletResponse response,
            String token,
            long expiracaoSegundos) {

        escrever(response, token, Duration.ofSeconds(expiracaoSegundos));
    }

    public void limpar(HttpServletResponse response) {
        escrever(response, "", Duration.ZERO);
    }

    public Optional<String> ler(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> NOME_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(valor -> !valor.isBlank())
                .findFirst();
    }

    private void escrever(
            HttpServletResponse response,
            String valor,
            Duration duracao) {

        ResponseCookie cookie = ResponseCookie.from(NOME_COOKIE, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(duracao)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
