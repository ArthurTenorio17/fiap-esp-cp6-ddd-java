package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import br.com.fiap.bank.plataforma.application.dto.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PlataformaCreditoServiceTest {

    @Mock
    private SolicitacaoCreditoRepository repository;

    @Mock
    private ScrGateway scrGateway;

    @Mock
    private BureauCreditoGateway bureauGateway;

    @Mock
    private NotificacaoGateway notificacaoGateway;

    @Mock
    private CoreBancarioGateway coreBancarioGateway;

    private PlataformaCreditoService service;

    @BeforeEach
    void configurar() {
        service = new PlataformaCreditoService(
                repository,
                scrGateway,
                bureauGateway,
                notificacaoGateway,
                coreBancarioGateway
        );
    }

    private SolicitarCreditoRequest criarRequest() {
        return new SolicitarCreditoRequest(
                "12345678901",
                "Cliente Teste",
                "PESSOAL",
                new BigDecimal("12000.00"),
                12,
                new BigDecimal("5000.00")
        );
    }

    private void configurarPersistencia() {
        when(repository.salvar(any(SolicitacaoCredito.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0, SolicitacaoCredito.class)
                );
    }

    private SolicitacaoCredito criarSolicitacao(
            StatusSolicitacao status) {

        return new SolicitacaoCredito(
                1L,
                "12345678901",
                "Cliente Teste",
                "PESSOAL",
                new BigDecimal("12000.00"),
                12,
                new BigDecimal("5000.00"),
                800,
                status,
                "Aguardando decisao",
                false,
                800,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void deveReprovarQuandoClientePossuiRestricaoScr() {
        when(scrGateway.consultarRestricoesScr(anyString()))
                .thenReturn(true);

        when(bureauGateway.consultarScoreBureau(anyString()))
                .thenReturn(800);

        when(coreBancarioGateway.consultarContaCliente(anyString()))
                .thenReturn(Map.of("statusConta", "ATIVA"));

        configurarPersistencia();

        var resposta = service.solicitarCredito(criarRequest());

        assertEquals(StatusSolicitacao.REPROVADO, resposta.status());
        assertEquals(Boolean.TRUE, resposta.scrRestricoes());

        verify(repository).salvar(any(SolicitacaoCredito.class));
    }

    @Test
    void deveReprovarQuandoScoreEhInsuficiente() {
        when(scrGateway.consultarRestricoesScr(anyString()))
                .thenReturn(false);

        when(bureauGateway.consultarScoreBureau(anyString()))
                .thenReturn(450);

        when(coreBancarioGateway.consultarContaCliente(anyString()))
                .thenReturn(Map.of("statusConta", "ATIVA"));

        configurarPersistencia();

        var resposta = service.solicitarCredito(criarRequest());

        assertEquals(StatusSolicitacao.REPROVADO, resposta.status());
        assertEquals(450, resposta.scoreBureau());
    }

    @Test
    void deveEncaminharParaAnaliseQuandoParcelaSuperaLimite() {
        when(scrGateway.consultarRestricoesScr(anyString()))
                .thenReturn(false);

        when(bureauGateway.consultarScoreBureau(anyString()))
                .thenReturn(750);

        when(coreBancarioGateway.consultarContaCliente(anyString()))
                .thenReturn(Map.of("statusConta", "ATIVA"));

        configurarPersistencia();

        SolicitarCreditoRequest request = new SolicitarCreditoRequest(
                "12345678901",
                "Cliente Teste",
                "PESSOAL",
                new BigDecimal("24000.00"),
                12,
                new BigDecimal("5000.00")
        );

        var resposta = service.solicitarCredito(request);

        assertEquals(StatusSolicitacao.EM_ANALISE, resposta.status());
    }

    @Test
    void deveAprovarQuandoClienteAtendeAosCriterios() {
        when(scrGateway.consultarRestricoesScr(anyString()))
                .thenReturn(false);

        when(bureauGateway.consultarScoreBureau(anyString()))
                .thenReturn(800);

        when(coreBancarioGateway.consultarContaCliente(anyString()))
                .thenReturn(Map.of("statusConta", "ATIVA"));

        configurarPersistencia();

        var resposta = service.solicitarCredito(criarRequest());

        assertEquals(StatusSolicitacao.APROVADO, resposta.status());

        verify(notificacaoGateway).enviarNotificacao(
                eq("12345678901"),
                contains("APROVADO")
        );
    }

    @Test
    void deveConsultarSolicitacoesPorCpf() {
        when(repository.buscarPorCpfCliente("12345678901"))
                .thenReturn(List.of(
                        criarSolicitacao(StatusSolicitacao.EM_ANALISE)
                ));

        var resposta = service.consultarStatusPorCpf(
                new ConsultarStatusRequest("123.456.789-01")
        );

        assertEquals(1, resposta.size());
        assertEquals("12345678901", resposta.get(0).cpfCliente());
        assertEquals(StatusSolicitacao.EM_ANALISE,
                resposta.get(0).status());
    }

    @Test
    void deveBuscarSolicitacaoPorId() {
        when(repository.buscarPorId(1L))
                .thenReturn(Optional.of(
                        criarSolicitacao(StatusSolicitacao.EM_ANALISE)
                ));

        var resposta = service.buscarPorId(
                new BuscarSolicitacaoRequest(1L)
        );

        assertEquals(1L, resposta.id());
        assertEquals(StatusSolicitacao.EM_ANALISE, resposta.status());
    }

    @Test
    void deveLancarExcecaoQuandoSolicitacaoNaoExiste() {
        when(repository.buscarPorId(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorId(
                        new BuscarSolicitacaoRequest(99L)
                )
        );
    }

    @Test
    void deveListarTodasSolicitacoes() {
        when(repository.listarTodas())
                .thenReturn(List.of(
                        criarSolicitacao(StatusSolicitacao.EM_ANALISE),
                        criarSolicitacao(StatusSolicitacao.APROVADO)
                ));

        var resposta = service.listarTodasSolicitacoes();

        assertEquals(2, resposta.size());
    }

    @Test
    void deveAprovarSolicitacaoPelaMesa() {
        when(repository.buscarPorId(1L))
                .thenReturn(Optional.of(
                        criarSolicitacao(StatusSolicitacao.EM_ANALISE)
                ));

        configurarPersistencia();

        var resposta = service.aprovarSolicitacao(
                new AprovarSolicitacaoRequest(
                        1L,
                        "Documentacao validada"
                )
        );

        assertEquals(StatusSolicitacao.APROVADO, resposta.status());
        assertTrue(resposta.parecerOperador()
                .contains("Documentacao validada"));

        verify(notificacaoGateway).enviarNotificacao(
                eq("12345678901"),
                contains("APROVADA")
        );
    }

    @Test
    void deveReprovarSolicitacaoPelaMesa() {
        when(repository.buscarPorId(1L))
                .thenReturn(Optional.of(
                        criarSolicitacao(StatusSolicitacao.EM_ANALISE)
                ));

        configurarPersistencia();

        var resposta = service.reprovarSolicitacao(
                new ReprovarSolicitacaoRequest(
                        1L,
                        "Risco elevado"
                )
        );

        assertEquals(StatusSolicitacao.REPROVADO, resposta.status());
        assertTrue(resposta.parecerOperador()
                .contains("Risco elevado"));

        verify(notificacaoGateway).enviarNotificacao(
                eq("12345678901"),
                contains("REPROVADA")
        );
    }

    @Test
    void deveImpedirAlteracaoDeSolicitacaoJaAprovada() {
        when(repository.buscarPorId(1L))
                .thenReturn(Optional.of(
                        criarSolicitacao(StatusSolicitacao.APROVADO)
                ));

        assertThrows(
                IllegalStateException.class,
                () -> service.reprovarSolicitacao(
                        new ReprovarSolicitacaoRequest(
                                1L,
                                "Tentativa de alteracao"
                        )
                )
        );

        verify(repository, never())
                .salvar(any(SolicitacaoCredito.class));

        verifyNoInteractions(notificacaoGateway);
    }

}
