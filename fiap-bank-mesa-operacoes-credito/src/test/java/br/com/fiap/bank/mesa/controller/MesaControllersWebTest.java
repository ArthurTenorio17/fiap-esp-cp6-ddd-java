package br.com.fiap.bank.mesa.controller;

import br.com.fiap.bank.mesa.auth.TokenCookieService;
import br.com.fiap.bank.mesa.client.PlataformaCreditoFeignClient;
import br.com.fiap.bank.mesa.dto.LoginResponseDTO;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class MesaControllersWebTest {

    private PlataformaCreditoFeignClient plataformaClient;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        plataformaClient = mock(PlataformaCreditoFeignClient.class);
        TokenCookieService cookies = new TokenCookieService(false);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new MesaOperacoesController(plataformaClient, cookies),
                        new AuthController(plataformaClient, cookies)
                )
                .setViewResolvers(viewResolver)
                .build();
    }

    private Request requisicaoFeign() {
        return Request.create(
                Request.HttpMethod.POST,
                "http://localhost:8081/api/credito/x",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );
    }

    private FeignException.Unauthorized naoAutorizado() {
        return new FeignException.Unauthorized("401", requisicaoFeign(), null, Map.of());
    }

    private FeignException.Forbidden proibido() {
        return new FeignException.Forbidden("403", requisicaoFeign(), null, Map.of());
    }

    @Test
    void loginValidoDeveGravarCookieProtegidoERedirecionar() throws Exception {
        when(plataformaClient.login(any()))
                .thenReturn(new LoginResponseDTO("jwt-valido", "Bearer", 7200));

        MvcResult resultado = mockMvc.perform(post("/login")
                        .param("email", "teste@fiapbank.com")
                        .param("senha", "Teste@123"))
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();

        String cookie = resultado.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        assertTrue(cookie.contains("MESA_ACCESS_TOKEN=jwt-valido"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Strict"));
        assertTrue(cookie.contains("Max-Age=7200"));
    }

    @Test
    void loginInvalidoDeveVoltarParaATelaComErro() throws Exception {
        when(plataformaClient.login(any())).thenThrow(naoAutorizado());

        mockMvc.perform(post("/login")
                        .param("email", "teste@fiapbank.com")
                        .param("senha", "errada"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "E-mail ou senha incorretos."));
    }

    @Test
    void loginComPlataformaForaDoArDeveInformarIndisponibilidade() throws Exception {
        when(plataformaClient.login(any())).thenThrow(new RuntimeException("connection refused"));

        mockMvc.perform(post("/login")
                        .param("email", "teste@fiapbank.com")
                        .param("senha", "Teste@123"))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Plataforma de Crédito indisponível. Tente novamente."));
    }

    @Test
    void logoutDeveExpirarOCookie() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/logout"))
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        assertTrue(
                resultado.getResponse().getHeader(HttpHeaders.SET_COOKIE)
                        .contains("Max-Age=0")
        );
    }

    @Test
    void dashboardDeveExibirDadosQuandoAutorizado() throws Exception {
        when(plataformaClient.listarTodasSolicitacoes()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("totalCount", 0L));
    }

    @Test
    void dashboardCom401DeveRedirecionarParaLogin() throws Exception {
        when(plataformaClient.listarTodasSolicitacoes()).thenThrow(naoAutorizado());

        mockMvc.perform(get("/dashboard"))
                .andExpect(redirectedUrl("/login?motivo=expirada"));
    }

    @Test
    void dashboardCom403DeveInformarFaltaDePermissao() throws Exception {
        when(plataformaClient.listarTodasSolicitacoes()).thenThrow(proibido());

        mockMvc.perform(get("/dashboard"))
                .andExpect(redirectedUrl("/login?motivo=sem-permissao"));
    }

    @Test
    void dashboardComFalhaDeComunicacaoDeveEntrarEmModoOffline() throws Exception {
        when(plataformaClient.listarTodasSolicitacoes())
                .thenThrow(new RuntimeException("connection refused"));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("warning"));
    }

    @Test
    void aprovarCom401DeveRedirecionarParaLogin() throws Exception {
        when(plataformaClient.aprovarSolicitacao(any())).thenThrow(naoAutorizado());

        mockMvc.perform(post("/aprovar").param("solicitacaoId", "1"))
                .andExpect(redirectedUrl("/login?motivo=expirada"));
    }

    @Test
    void reprovarCom403DeveInformarFaltaDePermissao() throws Exception {
        when(plataformaClient.reprovarSolicitacao(any())).thenThrow(proibido());

        mockMvc.perform(post("/reprovar")
                        .param("solicitacaoId", "1")
                        .param("motivoReprovacao", "risco"))
                .andExpect(redirectedUrl("/login?motivo=sem-permissao"));
    }

    @Test
    void falhaAoAprovarNaoDeveExporDetalhesInternosNaUrl() throws Exception {
        when(plataformaClient.aprovarSolicitacao(any()))
                .thenThrow(new RuntimeException("http://interno:8081 {\"detalhe\":\"segredo\"}"));

        MvcResult resultado = mockMvc.perform(post("/aprovar").param("solicitacaoId", "1"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String destino = resultado.getResponse().getHeader(HttpHeaders.LOCATION);

        assertTrue(destino.startsWith("/dashboard?error="));
        assertTrue(!destino.contains("interno") && !destino.contains("segredo"));
    }
}
