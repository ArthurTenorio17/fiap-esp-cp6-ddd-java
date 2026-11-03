package br.com.fiap.bank.plataforma.config;

import br.com.fiap.bank.plataforma.application.dto.ConsultarStatusRequest;
import br.com.fiap.bank.plataforma.application.service.PlataformaCreditoService;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercita a cadeia de seguranca real: login, emissao do JWT, decoder,
 * conversao de claims e @PreAuthorize. Apenas a regra de negocio e o Core
 * sao simulados.
 */
@SpringBootTest
class SecurityIntegrationTest {

    private static final String CPF_CLIENTE = "98765432100";
    private static final String OUTRO_CPF = "12345678900";

    private static final String CORPO_SOLICITACAO = """
            {"cpfCliente":"%s","nomeCliente":"Carlos Oliveira","tipoCredito":"PESSOAL",
             "valorSolicitado":1000.00,"quantidadeParcelas":12,"rendaMensal":5000.00}
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @MockitoBean
    private PlataformaCreditoService plataformaService;

    @MockitoBean
    private CoreBancarioGateway coreBancarioGateway;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        when(plataformaService.listarTodasSolicitacoes())
                .thenReturn(List.of());
        when(plataformaService.consultarStatusPorCpf(any()))
                .thenReturn(List.of());
        when(coreBancarioGateway.consultarContaCliente(any()))
                .thenReturn(Map.of("saldo", 100));
    }

    // ---------- Login e claims ----------

    @Test
    void loginValidoDeveEmitirTokenComTodasAsClaims() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("cliente@fiapbank.com", "Cliente@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(7200))
                .andReturn();

        String accessToken = JsonPath.read(
                resultado.getResponse().getContentAsString(),
                "$.accessToken"
        );

        Jwt jwt = jwtDecoder.decode(accessToken);

        assertEquals(issuer, jwt.getClaimAsString("iss"));
        assertEquals(CPF_CLIENTE, jwt.getSubject());
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());
        assertEquals(CPF_CLIENTE, jwt.getClaimAsString("cpf"));
        assertEquals("78901-2", jwt.getClaimAsString("conta"));
        assertEquals(List.of("CLIENTE"), jwt.getClaimAsStringList("roles"));
        assertEquals("ROLE_CLIENTE", jwt.getClaimAsString("scope"));
        assertEquals("HS256", jwt.getHeaders().get("alg").toString());
    }

    @Test
    void loginComSenhaErradaDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("cliente@fiapbank.com", "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComUsuarioInexistenteDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("ninguem@fiapbank.com", "Cliente@123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComJsonInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalido"))
                .andExpect(status().isBadRequest());
    }

    // ---------- 401: autenticacao ----------

    @Test
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/solicitacoes"))
                .andExpect(status().isUnauthorized());

        verify(plataformaService, never()).listarTodasSolicitacoes();
    }

    @Test
    void rotaLegadaSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/credito/listarTodasSolicitacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenMalformadoDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/solicitacoes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer nao.e.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAssinadoComOutraChaveDeveRetornar401() throws Exception {
        String token = tokenManual(
                "outra-chave-com-mais-de-32-bytes-de-tamanho!!",
                issuer,
                Instant.now().plusSeconds(600)
        );

        mockMvc.perform(comBearer(token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoDeveRetornar401() throws Exception {
        // Fora da tolerancia de 60 segundos do JwtTimestampValidator.
        String token = tokenManual(
                secret,
                issuer,
                Instant.now().minusSeconds(120)
        );

        mockMvc.perform(comBearer(token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeOutroEmissorDeveRetornar401() throws Exception {
        String token = tokenManual(
                secret,
                "emissor-desconhecido",
                Instant.now().plusSeconds(600)
        );

        mockMvc.perform(comBearer(token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenManualValidoDeveSerAceito() throws Exception {
        // Controle dos testes acima: so a chave, o emissor ou a expiracao mudam.
        String token = tokenManual(
                secret,
                issuer,
                Instant.now().plusSeconds(600)
        );

        mockMvc.perform(comBearer(token))
                .andExpect(status().isOk());
    }

    // ---------- 403: autorizacao ----------

    @Test
    void clienteNaoPodeListarTodasAsSolicitacoes() throws Exception {
        mockMvc.perform(comBearer(tokenDe("cliente@fiapbank.com", "Cliente@123")))
                .andExpect(status().isForbidden());

        verify(plataformaService, never()).listarTodasSolicitacoes();
    }

    @Test
    void clienteNaoPodeAprovarNemReprovar() throws Exception {
        String token = tokenDe("cliente@fiapbank.com", "Cliente@123");

        mockMvc.perform(put("/api/v1/solicitacoes/1/aprovacao")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parecerOperador\":\"ok\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/credito/reprovarSolicitacao")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitacaoId\":1,\"motivoReprovacao\":\"x\"}"))
                .andExpect(status().isForbidden());

        verify(plataformaService, never()).aprovarSolicitacao(any());
        verify(plataformaService, never()).reprovarSolicitacao(any());
    }

    @Test
    void clienteNaoPodeUsarCpfDeOutroCliente() throws Exception {
        String token = tokenDe("cliente@fiapbank.com", "Cliente@123");

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .param("cpf", OUTRO_CPF)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_SOLICITACAO.formatted(OUTRO_CPF)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/contas/cliente/{cpf}", OUTRO_CPF)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(plataformaService, never()).solicitarCredito(any());
        verify(coreBancarioGateway, never()).consultarContaCliente(any());
    }

    @Test
    void tokenSemPerfilAutorizadoRecebe403() throws Exception {
        String token = tokenComAutorizacao(
                secret,
                issuer,
                Instant.now().plusSeconds(600),
                "VISITANTE",
                "ROLE_VISITANTE"
        );

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .param("cpf", CPF_CLIENTE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------- Operacoes permitidas ----------

    @Test
    void clientePodeOperarSobreOProprioCpf() throws Exception {
        String token = tokenDe("cliente@fiapbank.com", "Cliente@123");

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .param("cpf", CPF_CLIENTE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/contas/cliente/{cpf}", CPF_CLIENTE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/credito/consultarStatusCredito")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCliente\":\"" + CPF_CLIENTE + "\"}"))
                .andExpect(status().isOk());

        verify(plataformaService, org.mockito.Mockito.atLeastOnce())
                .consultarStatusPorCpf(new ConsultarStatusRequest(CPF_CLIENTE));
    }

    @Test
    void operadorPodeExecutarOperacoesAdministrativas() throws Exception {
        verificarOperacoesAdministrativas(
                tokenDe("teste@fiapbank.com", "Teste@123")
        );
    }

    @Test
    void adminPodeExecutarOperacoesAdministrativas() throws Exception {
        verificarOperacoesAdministrativas(
                tokenDe("admin@fiapbank.com", "Admin@123")
        );
    }

    // ---------- Configuracao da cadeia de filtros ----------

    @Test
    void respostaNaoDeveCriarSessao() throws Exception {
        MvcResult resultado = mockMvc.perform(
                        comBearer(tokenDe("teste@fiapbank.com", "Teste@123")))
                .andExpect(status().isOk())
                .andReturn();

        assertNull(resultado.getResponse().getHeader(HttpHeaders.SET_COOKIE));
        assertNull(resultado.getRequest().getSession(false));
    }

    @Test
    void h2ConsoleDeveSerPublicoESemFrameOptions() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/h2-console/"))
                .andReturn();

        assertNotEquals(401, resultado.getResponse().getStatus());
        assertNotEquals(403, resultado.getResponse().getStatus());
        assertNull(resultado.getResponse().getHeader("X-Frame-Options"));
    }

    @Test
    void preflightCorsDoAngularDeveSerAceitoSemToken() throws Exception {
        mockMvc.perform(options("/api/v1/solicitacoes")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:4200"
                ));
    }

    @Test
    void rotaInexistenteAutenticadaDeveRetornar404() throws Exception {
        mockMvc.perform(get("/api/v1/nao-existe")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenDe("teste@fiapbank.com", "Teste@123")))
                .andExpect(status().isNotFound());
    }

    // ---------- Apoio ----------

    private void verificarOperacoesAdministrativas(String token) throws Exception {
        mockMvc.perform(comBearer(token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/credito/listarTodasSolicitacoes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/solicitacoes/1/aprovacao")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parecerOperador\":\"ok\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/credito/reprovarSolicitacao")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitacaoId\":1,\"motivoReprovacao\":\"risco\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/contas/cliente/{cpf}", OUTRO_CPF)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder comBearer(
            String token) {

        return get("/api/v1/solicitacoes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private String corpoLogin(String email, String senha) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    private String tokenDe(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin(email, senha)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(
                resultado.getResponse().getContentAsString(),
                "$.accessToken"
        );
    }

    // Token de operador cujos parametros de assinatura sao controlados pelo teste.
    private String tokenManual(String chave, String emissor, Instant expiracao)
            throws Exception {

        return tokenComAutorizacao(
                chave,
                emissor,
                expiracao,
                "OPERADOR",
                "ROLE_OPERADOR"
        );
    }

    private String tokenComAutorizacao(
            String chave,
            String emissor,
            Instant expiracao,
            String role,
            String scope) throws Exception {

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(emissor)
                .subject(CPF_CLIENTE)
                .issueTime(Date.from(expiracao.minusSeconds(3600)))
                .expirationTime(Date.from(expiracao))
                .claim("cpf", CPF_CLIENTE)
                .claim("roles", List.of(role))
                .claim("scope", scope)
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(chave.getBytes(StandardCharsets.UTF_8)));

        return jwt.serialize();
    }
}
