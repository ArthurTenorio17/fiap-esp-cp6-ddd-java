package br.com.fiap.bank.plataforma.infrastructure.integration;

import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import org.springframework.stereotype.Component;

@Component
public class BureauCreditoGatewayAdapter
        implements BureauCreditoGateway {

    @Override
    public int consultarScoreBureau(String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", "");

        if (cpfLimpo.isEmpty()) {
            return 500;
        }

        int sum = 0;

        for (char c : cpfLimpo.toCharArray()) {
            sum += Character.getNumericValue(c);
        }

        // Score deterministico preservado do projeto original.
        return 400 + (sum * 9) % 551;
    }
}
