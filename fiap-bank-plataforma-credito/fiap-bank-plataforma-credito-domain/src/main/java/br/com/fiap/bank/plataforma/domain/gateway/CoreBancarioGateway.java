package br.com.fiap.bank.plataforma.domain.gateway;

import java.util.Map;

public interface CoreBancarioGateway {

    Map<String, Object> consultarContaCliente(String cpf);
}
