package br.com.fiap.bank.plataforma.infrastructure.client;

import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.exception.CoreBancarioIndisponivelException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class CoreBancarioRestClientAdapter implements CoreBancarioGateway {

    private final RestClient restClient;

    public CoreBancarioRestClientAdapter(
            RestClient.Builder builder,
            @Value("${core.bancario.url:http://localhost:8080/api/v1}")
            String coreBancarioUrl) {

        this.restClient = builder
                .baseUrl(coreBancarioUrl)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> consultarContaCliente(String cpf) {

        try {
            Map<String, Object> resposta = restClient.get()
                    .uri("/contas/cliente/{cpf}", cpf)
                    .retrieve()
                    .body(Map.class);

            if (resposta == null) {
                throw new CoreBancarioIndisponivelException(
                        "O Core Bancario retornou uma resposta vazia."
                );
            }

            return resposta;

        } catch (RestClientException e) {
            throw new CoreBancarioIndisponivelException(
                    "Falha ao consultar a conta do cliente no Core Bancario.",
                    e
            );
        }
    }
}
