package br.com.fiap.bank.plataforma.service;

import br.com.fiap.bank.plataforma.client.CoreBancarioFeignClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CoreBancarioClientService {

    private final CoreBancarioFeignClient coreBancarioFeignClient;

    public CoreBancarioClientService(CoreBancarioFeignClient coreBancarioFeignClient) {
        this.coreBancarioFeignClient = coreBancarioFeignClient;
    }

    /**
     * Consulta informacoes da conta do cliente no Core Bancario via Spring Cloud OpenFeign.
     */
    public Map<String, Object> consultarContaCliente(String cpf) {
        try {
            return coreBancarioFeignClient.consultarContaCliente(cpf);
        } catch (Exception e) {
            System.err.println("[CORE BANCARIO CLIENT] Core Bancario offline ou conta nao encontrada para CPF: " + cpf + ". Usando dados padrao.");
            return Map.of(
                    "statusConta", "ATIVA",
                    "saldo", 12500.00,
                    "agencia", "0001",
                    "numeroConta", "12345-6"
            );
        }
    }
}
