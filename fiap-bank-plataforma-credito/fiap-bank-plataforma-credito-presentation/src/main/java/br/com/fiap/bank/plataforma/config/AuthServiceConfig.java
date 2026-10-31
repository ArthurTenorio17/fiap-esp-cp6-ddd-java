package br.com.fiap.bank.plataforma.config;

import br.com.fiap.bank.plataforma.application.service.AuthService;
import br.com.fiap.bank.plataforma.domain.port.UsuarioConsultaPort;
import br.com.fiap.bank.plataforma.domain.port.SenhaVerificacaoPort;
import br.com.fiap.bank.plataforma.domain.port.TokenGeracaoPort;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthServiceConfig {

    @Bean
    public AuthService authService(
            UsuarioConsultaPort usuarioConsultaPort,
            SenhaVerificacaoPort senhaVerificacaoPort,
            TokenGeracaoPort tokenGeracaoPort
    ) {
        return new AuthService(
                usuarioConsultaPort,
                senhaVerificacaoPort,
                tokenGeracaoPort
        );
    }
}
