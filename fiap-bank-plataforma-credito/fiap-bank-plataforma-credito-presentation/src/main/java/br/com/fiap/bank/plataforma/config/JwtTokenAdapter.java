package br.com.fiap.bank.plataforma.config;

import br.com.fiap.bank.plataforma.domain.port.TokenGeracaoPort;
import br.com.fiap.bank.plataforma.domain.port.UsuarioAutenticacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenAdapter implements TokenGeracaoPort {

    private final JwtEncoder jwtEncoder;
    private final long expiracaoMinutos;
    private final String issuer;

    public JwtTokenAdapter(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-minutes}") long expiracaoMinutos,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expiracaoMinutos = expiracaoMinutos;
        this.issuer = issuer;
    }

    @Override
    public String gerarToken(UsuarioAutenticacao usuario) {

        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(expiracaoMinutos * 60))
                .subject(usuario.id().toString())
                .claim("email", usuario.email())
                .claim("perfil", usuario.perfil())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
