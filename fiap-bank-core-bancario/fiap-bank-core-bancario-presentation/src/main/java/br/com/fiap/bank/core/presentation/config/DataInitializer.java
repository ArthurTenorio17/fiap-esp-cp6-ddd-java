package br.com.fiap.bank.core.presentation.config;

import br.com.fiap.bank.core.application.service.CargaInicialService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Dispara a carga inicial ao subir a aplicacao. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CargaInicialService cargaInicialService;

    @Override
    public void run(String... args) {
        cargaInicialService.carregar();
    }
}
