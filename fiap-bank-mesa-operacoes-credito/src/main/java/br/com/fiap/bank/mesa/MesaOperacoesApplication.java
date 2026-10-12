package br.com.fiap.bank.mesa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MesaOperacoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MesaOperacoesApplication.class, args);
    }
}
