package br.com.fiap.bank.plataforma.config;

import br.com.fiap.bank.plataforma.domain.port.SenhaVerificacaoPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SenhaVerificacaoAdapter implements SenhaVerificacaoPort {

    private final PasswordEncoder passwordEncoder;

    public SenhaVerificacaoAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean verificar(String senhaInformada, String senhaHash) {
        return passwordEncoder.matches(senhaInformada, senhaHash);
    }
}
