package br.com.fiap.bank.plataforma.config;

import br.com.fiap.bank.plataforma.infrastructure.persistence.entity.UsuarioJpaEntity;
import br.com.fiap.bank.plataforma.infrastructure.persistence.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
public class UsuarioDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String email = "teste@fiapbank.com";

        if (usuarioRepository.existsByEmail(email)) {
            return;
        }

        UsuarioJpaEntity usuario = new UsuarioJpaEntity();

        usuario.setId(UUID.randomUUID());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("Teste@123"));
        usuario.setNome("Operador FIAP Bank");
        usuario.setPerfil("OPERADOR");
        usuario.setCpf("12345678900");
        usuario.setConta("12345-6");

        usuarioRepository.save(usuario);

        System.out.println(
                ">>> Usuario de teste cadastrado com sucesso!"
        );
    }
}
