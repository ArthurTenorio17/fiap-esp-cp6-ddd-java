package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.*;
import br.com.fiap.bank.plataforma.application.service.PlataformaCreditoService;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")

public class CreditoRestController {

    private final PlataformaCreditoService plataformaService;
    private final CoreBancarioGateway coreBancarioGateway;

    public CreditoRestController(
            PlataformaCreditoService plataformaService,
            CoreBancarioGateway coreBancarioGateway) {

        this.plataformaService = plataformaService;
        this.coreBancarioGateway = coreBancarioGateway;
    }

    @PostMapping("/solicitacoes")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> solicitarCredito(
            @Valid @RequestBody SolicitarCreditoRequest request) {

        SolicitacaoCreditoResponseDTO resposta =
                plataformaService.solicitarCredito(request);

        return ResponseEntity
                .created(URI.create("/api/v1/solicitacoes/" + resposta.id()))
                .body(resposta);
    }

    @GetMapping(value = "/solicitacoes", params = "cpf")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> consultarPorCpf(
            @RequestParam("cpf") String cpf) {

        return ResponseEntity.ok(
                plataformaService.consultarStatusPorCpf(
                        new ConsultarStatusRequest(cpf)
                )
        );
    }

    @GetMapping(value = "/solicitacoes", params = "!cpf")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> listarTodas() {

        return ResponseEntity.ok(
                plataformaService.listarTodasSolicitacoes()
        );
    }

    @GetMapping("/solicitacoes/{id}")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                plataformaService.buscarPorId(
                        new BuscarSolicitacaoRequest(id)
                )
        );
    }

    @PutMapping("/solicitacoes/{id}/aprovacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> aprovar(
            @PathVariable Long id,
            @Valid @RequestBody AprovarSolicitacaoRequest request            ) {

        return ResponseEntity.ok(
                plataformaService.aprovarSolicitacao(
                        new AprovarSolicitacaoRequest(
                                id,
                                request.parecerOperador()
                        )
                )
        );
    }

    @PutMapping("/solicitacoes/{id}/reprovacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> reprovar(
            @PathVariable Long id,
            @Valid @RequestBody ReprovarSolicitacaoRequest request) {

        return ResponseEntity.ok(
                plataformaService.reprovarSolicitacao(
                        new ReprovarSolicitacaoRequest(
                                id,
                                request.motivoReprovacao()
                        )
                )
        );
    }

    @GetMapping("/contas/cliente/{cpf}")
    public ResponseEntity<Map<String, Object>> consultarClienteCore(
            @PathVariable String cpf) {

        return ResponseEntity.ok(
                coreBancarioGateway.consultarContaCliente(cpf)
        );
    }
}
