package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.dto.*;
import br.com.fiap.bank.plataforma.service.CoreBancarioClientService;
import br.com.fiap.bank.plataforma.service.PlataformaCreditoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credito")
@CrossOrigin(origins = "*")
public class CreditoOperacoesNonRestfulController {

    private final PlataformaCreditoService plataformaService;
    private final CoreBancarioClientService coreBancarioClientService;

    public CreditoOperacoesNonRestfulController(PlataformaCreditoService plataformaService,
                                                 CoreBancarioClientService coreBancarioClientService) {
        this.plataformaService = plataformaService;
        this.coreBancarioClientService = coreBancarioClientService;
    }

    /**
     * Endpoint para Inclusoes de solicitacao de credito via POST (Acao: solicitarCredito)
     */
    @PostMapping("/solicitarCredito")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> solicitarCredito(@RequestBody SolicitarCreditoRequest request) {
        return ResponseEntity.ok(plataformaService.solicitarCredito(request));
    }

    /**
     * Endpoint para Consultas de status via POST com corpo da requisicao (Acao: consultarStatusCredito)
     */
    @PostMapping("/consultarStatusCredito")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> consultarStatusCredito(@RequestBody ConsultarStatusRequest request) {
        return ResponseEntity.ok(plataformaService.consultarStatusPorCpf(request));
    }

    /**
     * Endpoint para Busca por ID via POST (Acao: buscarSolicitacaoPorId)
     */
    @PostMapping("/buscarSolicitacaoPorId")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> buscarSolicitacaoPorId(@RequestBody BuscarSolicitacaoRequest request) {
        return ResponseEntity.ok(plataformaService.buscarPorId(request));
    }

    /**
     * Endpoint para Listagem de todas as solicitacoes via POST (Acao: listarTodasSolicitacoes)
     */
    @PostMapping("/listarTodasSolicitacoes")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> listarTodasSolicitacoes() {
        return ResponseEntity.ok(plataformaService.listarTodasSolicitacoes());
    }

    /**
     * Endpoint para Atualizacao/Aprovacao via POST (Acao: aprovarSolicitacao)
     */
    @PostMapping("/aprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> aprovarSolicitacao(@RequestBody AprovarSolicitacaoRequest request) {
        return ResponseEntity.ok(plataformaService.aprovarSolicitacao(request));
    }

    /**
     * Endpoint para Atualizacao/Reprovacao via POST (Acao: reprovarSolicitacao)
     */
    @PostMapping("/reprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> reprovarSolicitacao(@RequestBody ReprovarSolicitacaoRequest request) {
        return ResponseEntity.ok(plataformaService.reprovarSolicitacao(request));
    }

    /**
     * Endpoint para Consulta ao Core Bancario via POST (Acao: consultarClienteCore)
     */
    @PostMapping("/consultarClienteCore")
    public ResponseEntity<Map<String, Object>> consultarClienteCore(@RequestBody ConsultarStatusRequest request) {
        return ResponseEntity.ok(coreBancarioClientService.consultarContaCliente(request.cpfCliente()));
    }
}
