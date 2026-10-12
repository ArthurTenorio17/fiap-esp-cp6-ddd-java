package br.com.fiap.bank.plataforma.service;

import br.com.fiap.bank.plataforma.dto.*;
import br.com.fiap.bank.plataforma.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.repository.SolicitacaoCreditoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlataformaCreditoService {

    private final SolicitacaoCreditoRepository repository;
    private final ScrService scrService;
    private final BureauCreditoService bureauService;
    private final NotificacaoService notificacaoService;
    private final CoreBancarioClientService coreBancarioClientService;

    public PlataformaCreditoService(SolicitacaoCreditoRepository repository,
                                    ScrService scrService,
                                    BureauCreditoService bureauService,
                                    NotificacaoService notificacaoService,
                                    CoreBancarioClientService coreBancarioClientService) {
        this.repository = repository;
        this.scrService = scrService;
        this.bureauService = bureauService;
        this.notificacaoService = notificacaoService;
        this.coreBancarioClientService = coreBancarioClientService;
    }

    @Transactional
    public SolicitacaoCreditoResponseDTO solicitarCredito(SolicitarCreditoRequest request) {
        String cpfLimpo = request.cpfCliente().replaceAll("\\D", "");

        // 1. Consulta SCR
        boolean temRestricaoScr = scrService.consultarRestricoesScr(cpfLimpo);

        // 2. Consulta Bureau de Credito
        int scoreBureau = bureauService.consultarScoreBureau(cpfLimpo);

        // 3. Consulta Core Bancario
        Map<String, Object> contaCore = coreBancarioClientService.consultarContaCliente(cpfLimpo);

        // 4. Motor de Regras de Credito
        StatusSolicitacao status;
        String parecer;
        int scoreFinal = scoreBureau;

        if (temRestricaoScr) {
            status = StatusSolicitacao.REPROVADO;
            parecer = "Solicitacao reprovada automaticamente devido a restricoes ativas no SCR (Banco Central).";
        } else if (scoreBureau < 500) {
            status = StatusSolicitacao.REPROVADO;
            parecer = "Solicitacao reprovada automaticamente. Score no Bureau de Credito insuficiente (" + scoreBureau + ").";
        } else {
            BigDecimal parcelaEstimada = request.valorSolicitado().divide(BigDecimal.valueOf(request.quantidadeParcelas()), 2, RoundingMode.HALF_UP);
            BigDecimal trintaPercentoRenda = request.rendaMensal().multiply(new BigDecimal("0.30"));

            if (parcelaEstimada.compareTo(trintaPercentoRenda) > 0) {
                status = StatusSolicitacao.EM_ANALISE;
                parecer = "Encaminhado para a Mesa de Operacoes de Credito. Parcela estimada (R$ " + parcelaEstimada + ") compromete mais de 30% da renda informada.";
            } else {
                status = StatusSolicitacao.APROVADO;
                parecer = "Solicitacao aprovada automaticamente pelo Motor de Credito FIAP Bank!";
            }
        }

        SolicitacaoCredito solicitacao = new SolicitacaoCredito(
                null,
                cpfLimpo,
                request.nomeCliente(),
                request.tipoCredito(),
                request.valorSolicitado(),
                request.quantidadeParcelas(),
                request.rendaMensal(),
                scoreFinal,
                status,
                parecer,
                temRestricaoScr,
                scoreBureau,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        SolicitacaoCredito salva = repository.save(solicitacao);

        // 5. Envia Notificacao Externa
        notificacaoService.enviarNotificacao(cpfLimpo, "Sua solicitacao de credito #" + salva.getId() + " esta no status: " + status);

        return SolicitacaoCreditoResponseDTO.fromEntity(salva);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCreditoResponseDTO> consultarStatusPorCpf(ConsultarStatusRequest request) {
        String cpfLimpo = request.cpfCliente().replaceAll("\\D", "");
        return repository.findByCpfClienteOrderByDataSolicitacaoDesc(cpfLimpo).stream()
                .map(SolicitacaoCreditoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SolicitacaoCreditoResponseDTO buscarPorId(BuscarSolicitacaoRequest request) {
        SolicitacaoCredito solicitacao = repository.findById(request.solicitacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitacao nao encontrada com ID: " + request.solicitacaoId()));
        return SolicitacaoCreditoResponseDTO.fromEntity(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCreditoResponseDTO> listarTodasSolicitacoes() {
        return repository.findAllByOrderByDataSolicitacaoDesc().stream()
                .map(SolicitacaoCreditoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SolicitacaoCreditoResponseDTO aprovarSolicitacao(AprovarSolicitacaoRequest request) {
        SolicitacaoCredito solicitacao = repository.findById(request.solicitacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitacao de credito nao encontrada com ID: " + request.solicitacaoId()));

        solicitacao.setStatus(StatusSolicitacao.APROVADO);
        solicitacao.setParecerOperador("Aprovado pela Mesa de Operacoes: " + (request.parecerOperador() != null ? request.parecerOperador() : "OK"));
        solicitacao.setDataAtualizacao(LocalDateTime.now());

        SolicitacaoCredito salva = repository.save(solicitacao);
        notificacaoService.enviarNotificacao(salva.getCpfCliente(), "PARABENS! Sua solicitacao de credito #" + salva.getId() + " foi APROVADA pela Mesa de Operacoes.");
        return SolicitacaoCreditoResponseDTO.fromEntity(salva);
    }

    @Transactional
    public SolicitacaoCreditoResponseDTO reprovarSolicitacao(ReprovarSolicitacaoRequest request) {
        SolicitacaoCredito solicitacao = repository.findById(request.solicitacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitacao de credito nao encontrada com ID: " + request.solicitacaoId()));

        solicitacao.setStatus(StatusSolicitacao.REPROVADO);
        solicitacao.setParecerOperador("Reprovado pela Mesa de Operacoes. Motivo: " + (request.motivoReprovacao() != null ? request.motivoReprovacao() : "Inconformidade nos criterios de risco"));
        solicitacao.setDataAtualizacao(LocalDateTime.now());

        SolicitacaoCredito salva = repository.save(solicitacao);
        notificacaoService.enviarNotificacao(salva.getCpfCliente(), "Sua solicitacao de credito #" + salva.getId() + " foi REPROVADA pela Mesa de Operacoes.");
        return SolicitacaoCreditoResponseDTO.fromEntity(salva);
    }
}
