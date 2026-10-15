package br.com.fiap.bank.plataforma.domain.gateway;

public interface NotificacaoGateway {

    void enviarNotificacao(String cpf, String mensagem);
}
