package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.*;
import br.com.fiap.bank.plataforma.application.service.PlataformaCreditoService;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credito")
@CrossOrigin(origins = "*")
public class CreditoLegacyController {

    private final PlataformaCreditoService plataformaService;
    private final CoreBancarioGateway coreBancarioGateway;

    public CreditoLegacyController(
            PlataformaCreditoService plataformaService,
            CoreBancarioGateway coreBancarioGateway) {

        this.plataformaService = plataformaService;
        this.coreBancarioGateway = coreBancarioGateway;
    }

    @PostMapping("/solicitarCredito")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> solicitarCredito(
            @RequestBody SolicitarCreditoRequest request) {

        return ResponseEntity.ok(
                plataformaService.solicitarCredito(request)
        );
    }

    @PostMapping("/consultarStatusCredito")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> consultarStatusCredito(
            @RequestBody ConsultarStatusRequest request) {

        return ResponseEntity.ok(
                plataformaService.consultarStatusPorCpf(request)
        );
    }

    @PostMapping("/buscarSolicitacaoPorId")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> buscarSolicitacaoPorId(
            @RequestBody BuscarSolicitacaoRequest request) {

        return ResponseEntity.ok(
                plataformaService.buscarPorId(request)
        );
    }

    @PostMapping("/listarTodasSolicitacoes")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> listarTodasSolicitacoes() {

        return ResponseEntity.ok(
                plataformaService.listarTodasSolicitacoes()
        );
    }

    @PostMapping("/aprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> aprovarSolicitacao(
            @RequestBody AprovarSolicitacaoRequest request) {

        return ResponseEntity.ok(
                plataformaService.aprovarSolicitacao(request)
        );
    }

    @PostMapping("/reprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> reprovarSolicitacao(
            @RequestBody ReprovarSolicitacaoRequest request) {

        return ResponseEntity.ok(
                plataformaService.reprovarSolicitacao(request)
        );
    }

    @PostMapping("/consultarClienteCore")
    public ResponseEntity<Map<String, Object>> consultarClienteCore(
            @RequestBody ConsultarStatusRequest request) {

        return ResponseEntity.ok(
                coreBancarioGateway.consultarContaCliente(
                        request.cpfCliente()
                )
        );
    }
}
