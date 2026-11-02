package br.com.fiap.bank.plataforma.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CpfAuthorizationServiceTest {

    private CpfAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new CpfAuthorizationService();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String perfil, String cpf) {
        Instant agora = Instant.now();

        Jwt jwt = Jwt.withTokenValue("token-teste")
                .header("alg", "HS256")
                .subject("usuario-teste")
                .claim("cpf", cpf)
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(3600))
                .build();

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        jwt,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + perfil
                                )
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @Test
    void clientePodeConsultarProprioCpf() {
        autenticar("CLIENTE", "12345678900");

        assertTrue(
                service.podeAcessarCpf("12345678900")
        );
    }

    @Test
    void clienteNaoPodeConsultarOutroCpf() {
        autenticar("CLIENTE", "12345678900");

        assertFalse(
                service.podeAcessarCpf("98765432100")
        );
    }

    @Test
    void operadorPodeConsultarOutroCpf() {
        autenticar("OPERADOR", "12345678900");

        assertTrue(
                service.podeAcessarCpf("98765432100")
        );
    }

    @Test
    void adminPodeConsultarOutroCpf() {
        autenticar("ADMIN", "12345678900");

        assertTrue(
                service.podeAcessarCpf("98765432100")
        );
    }

    @Test
    void usuarioSemAutenticacaoNaoPodeConsultar() {
        assertFalse(
                service.podeAcessarCpf("12345678900")
        );
    }

    @Test
    void clienteComCpfDiferenteNaFormatacaoPodeConsultar() {
        autenticar("CLIENTE", "12345678900");

        assertTrue(
                service.podeAcessarCpf("123.456.789-00")
        );
    }

    @Test
    void usuarioSemPerfilAutorizadoNaoPodeConsultar() {
        autenticar("VISITANTE", "12345678900");

        assertFalse(
                service.podeAcessarCpf("12345678900")
        );
    }
}
