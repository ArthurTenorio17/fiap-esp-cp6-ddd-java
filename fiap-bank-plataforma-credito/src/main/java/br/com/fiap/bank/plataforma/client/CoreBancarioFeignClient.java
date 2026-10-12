package br.com.fiap.bank.plataforma.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "coreBancarioClient", url = "${core.bancario.url:http://localhost:8080/api/v1}")
public interface CoreBancarioFeignClient {

    @GetMapping("/contas/cliente/{cpf}")
    Map<String, Object> consultarContaCliente(@PathVariable("cpf") String cpf);
}
