
package br.com.fiap.bank.core.presentation;

import br.com.fiap.bank.core.application.dto.ClienteResponse;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.service.ClienteService;
import br.com.fiap.bank.core.presentation.controller.ClienteController;
import br.com.fiap.bank.core.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private static final String JSON_VALIDO = """
            {"cpf":"12345678900","nome":"Ana Silva","email":"ana@fiap.com.br",
             "dataNascimento":"1995-05-10","rendaMensal":7500.00}""";

    @Mock
    ClienteService service;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ClienteController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ClienteResponse resposta(UUID id) {
        return new ClienteResponse(
                id,
                "12345678900",
                "Ana Silva",
                "ana@fiap.com.br",
                LocalDate.of(1995, 5, 10),
                new BigDecimal("7500.00"),
                null
        );
    }

    @Test
    void getListaDevolve200() throws Exception {
        when(service.listar())
                .thenReturn(List.of(resposta(UUID.randomUUID())));

        mvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpf").value("12345678900"));
    }

    @Test
    void getPorIdInexistenteDevolve404ComCorpoPadrao() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.buscarPorId(id))
                .thenThrow(new RecursoNaoEncontradoException(
                        "Cliente nao encontrado: " + id
                ));

        mvc.perform(get("/api/v1/clientes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/clientes/" + id));
    }

    @Test
    void getComIdMalFormadoDevolve400() throws Exception {
        mvc.perform(get("/api/v1/clientes/nao-e-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPorCpfDevolve200() throws Exception {
        when(service.buscarPorCpf("12345678900"))
                .thenReturn(resposta(UUID.randomUUID()));

        mvc.perform(get("/api/v1/clientes/cpf/{cpf}", "12345678900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Silva"));
    }

    @Test
    void postValidoDevolve201ComLocation() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.criar(any()))
                .thenReturn(resposta(id));

        mvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        org.hamcrest.Matchers.endsWith(
                                "/api/v1/clientes/" + id
                        )
                ));
    }

    @Test
    void postInvalidoDevolve400ListandoOsCampos() throws Exception {
        String invalido = """
                {"cpf":"123","nome":"Al","email":"x",
                 "dataNascimento":"2999-01-01","rendaMensal":-1}""";

        mvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(5));
    }

    @Test
    void postComCpfDuplicadoDevolve409() throws Exception {
        when(service.criar(any()))
                .thenThrow(new ConflitoException("CPF ja cadastrado"));

        mvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void postSemCorpoDevolve400() throws Exception {
        mvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putDevolve200() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.atualizar(any(), any()))
                .thenReturn(resposta(id));

        mvc.perform(put("/api/v1/clientes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDevolve204() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/v1/clientes/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).remover(id);
    }

    @Test
    void deleteInexistenteDevolve404() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new RecursoNaoEncontradoException("nao existe"))
                .when(service)
                .remover(id);

        mvc.perform(delete("/api/v1/clientes/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void erroInesperadoDevolve500SemExporDetalhesInternos() throws Exception {
        when(service.listar())
                .thenThrow(new RuntimeException("senha-interna-secreta"));

        mvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem")
                        .value("Erro interno do servidor"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/clientes"))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString(
                                        "senha-interna-secreta"
                                )
                        )
                ));
    }
}
