package br.com.fiap.bank.plataforma.service;

import org.springframework.stereotype.Service;

@Service
public class ScrService {

    /**
     * Consulta o Sistema do Banco Central (SCR) para verificar se o cliente possui restricoes ou dividas vencidas.
     */
    public boolean consultarRestricoesScr(String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", "");
        // Simulação: CPFs terminados em 9 possuem restrição no SCR
        return cpfLimpo.endsWith("9");
    }
}
