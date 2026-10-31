
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

        cadastrarUsuario(
                "teste@fiapbank.com",
                "Teste@123",
                "Operador FIAP Bank",
                "OPERADOR",
                "12345678900",
                "12345-6"
        );

        cadastrarUsuario(
                "cliente@fiapbank.com",
                "Cliente@123",
                "Cliente Teste FIAP",
                "CLIENTE",
                "98765432100",
                "98765-4"
        );
    }

    private void cadastrarUsuario(
            String email,
            String senha,
            String nome,
            String perfil,
            String cpf,
            String conta
    ) {

        if (usuarioRepository.existsByEmail(email)) {
            System.out.println(
                    ">>> Usuario ja cadastrado: " + email
            );
            return;
        }

        UsuarioJpaEntity usuario = new UsuarioJpaEntity();

        usuario.setId(UUID.randomUUID());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setNome(nome);
        usuario.setPerfil(perfil);
        usuario.setCpf(cpf);
        usuario.setConta(conta);

        usuarioRepository.save(usuario);

        System.out.println(
                ">>> Usuario cadastrado com sucesso: "
                        + email + " | Perfil: " + perfil
        );
    }
}
