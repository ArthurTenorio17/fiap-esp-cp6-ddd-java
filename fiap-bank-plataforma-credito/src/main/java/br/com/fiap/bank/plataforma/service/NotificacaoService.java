package br.com.fiap.bank.plataforma.service;

import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    /**
     * Envia notificacao externa via SMS/Email para o cliente sobre a atualizacao do status do credito.
     */
    public void enviarNotificacao(String cpf, String mensagem) {
        System.out.println("[SISTEMA DE NOTIFICACOES] Notificacao enviada para CPF " + cpf + ": " + mensagem);
    }
}
