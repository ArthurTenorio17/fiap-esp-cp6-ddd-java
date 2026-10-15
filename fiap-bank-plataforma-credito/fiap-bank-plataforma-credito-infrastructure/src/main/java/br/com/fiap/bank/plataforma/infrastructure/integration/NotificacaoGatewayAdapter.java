package br.com.fiap.bank.plataforma.infrastructure.integration;

import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoGatewayAdapter
        implements NotificacaoGateway {

    @Override
    public void enviarNotificacao(String cpf, String mensagem) {
        System.out.println(
                "[SISTEMA DE NOTIFICACOES] Notificacao enviada para CPF "
                        + cpf + ": " + mensagem
        );
    }
}
