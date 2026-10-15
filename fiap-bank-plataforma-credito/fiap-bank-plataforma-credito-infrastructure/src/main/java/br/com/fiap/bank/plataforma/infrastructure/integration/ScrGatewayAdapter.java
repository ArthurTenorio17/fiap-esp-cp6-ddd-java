package br.com.fiap.bank.plataforma.infrastructure.integration;

import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import org.springframework.stereotype.Component;

@Component
public class ScrGatewayAdapter implements ScrGateway {

    @Override
    public boolean consultarRestricoesScr(String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", "");

        // Simulacao original: CPFs terminados em 9 possuem restricao.
        return cpfLimpo.endsWith("9");
    }
}
