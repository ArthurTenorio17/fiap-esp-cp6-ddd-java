
package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.AprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.BuscarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.ConsultarStatusRequest;
import br.com.fiap.bank.plataforma.application.dto.ReprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponseDTO;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;

import br.com.fiap.bank.plataforma.domain.exception.SolicitacaoNaoEncontradaException;
import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlataformaCreditoService {

    private final SolicitacaoCreditoRepository repository;
    private final ScrGateway scrGateway;
    private final BureauCreditoGateway bureauGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final CoreBancarioGateway coreBancarioGateway;

    @Transactional
    public SolicitacaoCreditoResponseDTO solicitarCredito(
            SolicitarCreditoRequest request) {

        String cpfLimpo = request.cpfCliente().replaceAll("\\D", "");

        // 1. Consulta as restricoes no SCR
        boolean temRestricaoScr =
                scrGateway.consultarRestricoesScr(cpfLimpo);

        // 2. Consulta o score no Bureau
        int scoreBureau =
                bureauGateway.consultarScoreBureau(cpfLimpo);

        // 3. Consulta as informacoes no Core Bancario
        coreBancarioGateway.consultarContaCliente(cpfLimpo);

        // 4. Aplica as regras de concessao
        StatusSolicitacao status;
        String parecer;

        if (temRestricaoScr) {
            status = StatusSolicitacao.REPROVADO;
            parecer = "Solicitacao reprovada automaticamente devido a "
                    + "restricoes ativas no SCR (Banco Central).";

        } else if (scoreBureau < 500) {
            status = StatusSolicitacao.REPROVADO;
            parecer = "Solicitacao reprovada automaticamente. "
                    + "Score no Bureau de Credito insuficiente ("
                    + scoreBureau + ").";

        } else {
            BigDecimal parcelaEstimada = request.valorSolicitado()
                    .divide(
                            BigDecimal.valueOf(request.quantidadeParcelas()),
                            2,
                            RoundingMode.HALF_UP
                    );

            BigDecimal limiteRenda = request.rendaMensal()
                    .multiply(new BigDecimal("0.30"));

            if (parcelaEstimada.compareTo(limiteRenda) > 0) {
                status = StatusSolicitacao.EM_ANALISE;
                parecer = "Encaminhado para a Mesa de Operacoes de Credito. "
                        + "Parcela estimada (R$ " + parcelaEstimada
                        + ") compromete mais de 30% da renda informada.";
            } else {
                status = StatusSolicitacao.APROVADO;
                parecer = "Solicitacao aprovada automaticamente "
                        + "pelo Motor de Credito FIAP Bank!";
            }
        }

        // 5. Cria a entidade pura de dominio
        LocalDateTime agora = LocalDateTime.now();

        SolicitacaoCredito solicitacao = new SolicitacaoCredito(
                null,
                cpfLimpo,
                request.nomeCliente(),
                request.tipoCredito(),
                request.valorSolicitado(),
                request.quantidadeParcelas(),
                request.rendaMensal(),
                scoreBureau,
                status,
                parecer,
                temRestricaoScr,
                scoreBureau,
                agora,
                agora
        );

        // 6. Persiste utilizando a interface do Domain
        SolicitacaoCredito salva = repository.salvar(solicitacao);

        // 7. Envia notificacao
        notificacaoGateway.enviarNotificacao(
                cpfLimpo,
                "Sua solicitacao de credito #" + salva.getId()
                        + " esta no status: " + status
        );

        return SolicitacaoCreditoResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCreditoResponseDTO> consultarStatusPorCpf(
            ConsultarStatusRequest request) {

        String cpfLimpo = request.cpfCliente().replaceAll("\\D", "");

        return repository.buscarPorCpfCliente(cpfLimpo)
                .stream()
                .map(SolicitacaoCreditoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoCreditoResponseDTO buscarPorId(
            BuscarSolicitacaoRequest request) {

        SolicitacaoCredito solicitacao = repository
                .buscarPorId(request.solicitacaoId())
                .orElseThrow(() ->
                        new SolicitacaoNaoEncontradaException(
                                request.solicitacaoId()
                        )
                );

        return SolicitacaoCreditoResponseDTO.fromEntity(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCreditoResponseDTO> listarTodasSolicitacoes() {

        return repository.listarTodas()
                .stream()
                .map(SolicitacaoCreditoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public SolicitacaoCreditoResponseDTO aprovarSolicitacao(
            AprovarSolicitacaoRequest request) {

        SolicitacaoCredito solicitacao = repository
                .buscarPorId(request.solicitacaoId())
                .orElseThrow(() ->
                        new SolicitacaoNaoEncontradaException(
                                request.solicitacaoId()
                        )
                );

        String parecer = "Aprovado pela Mesa de Operacoes: "
                + (request.parecerOperador() != null
                ? request.parecerOperador()
                : "OK");

        solicitacao.aprovar(parecer);

        SolicitacaoCredito salva = repository.salvar(solicitacao);

        notificacaoGateway.enviarNotificacao(
                salva.getCpfCliente(),
                "PARABENS! Sua solicitacao de credito #"
                        + salva.getId()
                        + " foi APROVADA pela Mesa de Operacoes."
        );

        return SolicitacaoCreditoResponseDTO.fromEntity(salva);
    }

    @Transactional
    public SolicitacaoCreditoResponseDTO reprovarSolicitacao(
            ReprovarSolicitacaoRequest request) {

        SolicitacaoCredito solicitacao = repository
                .buscarPorId(request.solicitacaoId())
                .orElseThrow(() ->
                        new SolicitacaoNaoEncontradaException(
                                request.solicitacaoId()
                        )
                );

        String parecer = "Reprovado pela Mesa de Operacoes. Motivo: "
                + (request.motivoReprovacao() != null
                ? request.motivoReprovacao()
                : "Inconformidade nos criterios de risco");

        solicitacao.reprovar(parecer);

        SolicitacaoCredito salva = repository.salvar(solicitacao);

        notificacaoGateway.enviarNotificacao(
                salva.getCpfCliente(),
                "Sua solicitacao de credito #"
                        + salva.getId()
                        + " foi REPROVADA pela Mesa de Operacoes."
        );

        return SolicitacaoCreditoResponseDTO.fromEntity(salva);
    }
}
