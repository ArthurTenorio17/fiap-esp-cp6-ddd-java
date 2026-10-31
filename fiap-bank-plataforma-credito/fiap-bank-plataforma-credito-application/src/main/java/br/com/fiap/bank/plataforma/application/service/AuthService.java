
package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.LoginRequest;
import br.com.fiap.bank.plataforma.application.dto.LoginResponse;
import br.com.fiap.bank.plataforma.domain.port.UsuarioConsultaPort;
import br.com.fiap.bank.plataforma.domain.port.SenhaVerificacaoPort;
import br.com.fiap.bank.plataforma.domain.port.TokenGeracaoPort;

public class AuthService {

    private final UsuarioConsultaPort usuarioConsultaPort;
    private final SenhaVerificacaoPort senhaVerificacaoPort;
    private final TokenGeracaoPort tokenGeracaoPort;

    public AuthService(
            UsuarioConsultaPort usuarioConsultaPort,
            SenhaVerificacaoPort senhaVerificacaoPort,
            TokenGeracaoPort tokenGeracaoPort
    ) {
        this.usuarioConsultaPort = usuarioConsultaPort;
        this.senhaVerificacaoPort = senhaVerificacaoPort;
        this.tokenGeracaoPort = tokenGeracaoPort;
    }

    public LoginResponse autenticar(LoginRequest request) {

        var usuario = usuarioConsultaPort
                .buscarPorEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Credenciais inválidas"
                        )
                );

        boolean senhaValida = senhaVerificacaoPort.verificar(
                request.senha(),
                usuario.senhaHash()
        );

        if (!senhaValida) {
            throw new IllegalArgumentException(
                    "Credenciais inválidas"
            );
        }

        String token = tokenGeracaoPort.gerarToken(usuario);

        return new LoginResponse(
                token,
                "Bearer",
                3600
        );
    }
}
