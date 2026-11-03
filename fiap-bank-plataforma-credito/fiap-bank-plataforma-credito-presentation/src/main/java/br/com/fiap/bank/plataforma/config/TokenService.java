package br.com.fiap.bank.plataforma.config;

import br.com.fiap.bank.plataforma.domain.port.TokenGeracaoPort;
import br.com.fiap.bank.plataforma.domain.port.UsuarioAutenticacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Emite os JWT assinados com HS256. Fica na presentation porque depende do
 * Spring Security; a application conhece apenas a porta TokenGeracaoPort.
 */
@Service
public class TokenService implements TokenGeracaoPort {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;
    private final String issuer;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value("${jwt.expiration-seconds}") long expirationSeconds,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
        this.issuer = issuer;
    }

    @Override
    public String gerarToken(UsuarioAutenticacao usuario) {

        Instant agora = Instant.now();
        boolean possuiCpf = usuario.cpf() != null && !usuario.cpf().isBlank();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                // Registered Claims (RFC 7519)
                .issuer(issuer)
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(expirationSeconds))
                .subject(possuiCpf ? usuario.cpf() : usuario.id().toString())
                // Private Claims do dominio bancario
                .claim("email", usuario.email())
                .claim("perfil", usuario.perfil())
                .claim("roles", List.of(usuario.perfil()))
                // Vira a autoridade SCOPE_ROLE_<PERFIL>, ex.: SCOPE_ROLE_CLIENTE
                .claim("scope", "ROLE_" + usuario.perfil());

        if (possuiCpf) {
            claims.claim("cpf", usuario.cpf());
        }

        if (usuario.conta() != null && !usuario.conta().isBlank()) {
            claims.claim("conta", usuario.conta());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims.build())
        ).getTokenValue();
    }

    @Override
    public long expiracaoEmSegundos() {
        return expirationSeconds;
    }
}
