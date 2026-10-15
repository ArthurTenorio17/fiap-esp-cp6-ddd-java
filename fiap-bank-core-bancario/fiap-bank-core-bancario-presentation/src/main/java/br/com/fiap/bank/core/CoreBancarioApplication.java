package br.com.fiap.bank.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Fica no pacote raiz br.com.fiap.bank.core para o scan enxergar as camadas
 * domain, infrastructure, application e presentation.
 */
@SpringBootApplication
public class CoreBancarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBancarioApplication.class, args);
    }
}
