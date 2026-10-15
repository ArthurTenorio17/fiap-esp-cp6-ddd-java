package br.com.fiap.bank.plataforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(
        basePackages = "br.com.fiap.bank.plataforma.infrastructure.persistence.entity"
)
@EnableJpaRepositories(
        basePackages = "br.com.fiap.bank.plataforma.infrastructure.persistence.repository"
)
public class PlataformaCreditoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaCreditoApplication.class, args);
    }
}
