package br.com.fiap.bank.plataforma.infrastructure.persistence.adapter;

import br.com.fiap.bank.plataforma.domain.port.UsuarioAutenticacao;
import br.com.fiap.bank.plataforma.domain.port.UsuarioConsultaPort;
import br.com.fiap.bank.plataforma.infrastructure.persistence.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioConsultaAdapter implements UsuarioConsultaPort {

    private final UsuarioRepository usuarioRepository;

    public UsuarioConsultaAdapter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<UsuarioAutenticacao> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> new UsuarioAutenticacao(
                        usuario.getId(),
                        usuario.getEmail(),
                        usuario.getSenha(),
                        usuario.getNome(),
                        usuario.getPerfil()
                ));
    }
}
