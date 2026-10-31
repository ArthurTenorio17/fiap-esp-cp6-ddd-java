
package br.com.fiap.bank.plataforma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // API REST com JWT: sem autenticacao por cookies
                .csrf(csrf -> csrf.disable())

                // Permite os frames do H2 Console
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // API sem sessao HTTP
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Regras de autorizacao
                .authorizeHttpRequests(auth -> auth

                        // H2 Console - somente desenvolvimento
                        .requestMatchers(
                                "/h2-console",
                                "/h2-console/**"
                        ).permitAll()

                        // Login publico
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        // Demais endpoints exigem autenticacao
                        .anyRequest().authenticated()
                )

                // Validacao de tokens JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
