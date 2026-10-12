package br.com.fiap.bank.plataforma.service;

import org.springframework.stereotype.Service;

@Service
public class BureauCreditoService {

    /**
     * Consulta o Bureau de Credito (Serasa/SPC) para obter o Score de Risco do cliente (0-1000).
     */
    public int consultarScoreBureau(String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (cpfLimpo.isEmpty()) return 500;
        int sum = 0;
        for (char c : cpfLimpo.toCharArray()) {
            sum += Character.getNumericValue(c);
        }
        // Gera um score deterministico baseado no CPF entre 400 e 950
        return 400 + (sum * 9) % 551;
    }
}
