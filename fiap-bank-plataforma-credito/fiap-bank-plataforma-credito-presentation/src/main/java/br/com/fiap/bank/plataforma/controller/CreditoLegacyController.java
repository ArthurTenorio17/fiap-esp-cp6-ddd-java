
package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.*;
import br.com.fiap.bank.plataforma.application.service.PlataformaCreditoService;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Deprecated
@RestController
@RequestMapping("/api/credito")
public class CreditoLegacyController {

    private final PlataformaCreditoService plataformaService;
    private final CoreBancarioGateway coreBancarioGateway;

    public CreditoLegacyController(
            PlataformaCreditoService plataformaService,
            CoreBancarioGateway coreBancarioGateway
    ) {
        this.plataformaService = plataformaService;
        this.coreBancarioGateway = coreBancarioGateway;
    }

    // Solicitar credito
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN') " +
                    "and @cpfAuthorizationService.podeAcessarCpf(#request.cpfCliente())"
    )
    @PostMapping("/solicitarCredito")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> solicitarCredito(
            @P("request") @Valid @RequestBody SolicitarCreditoRequest request
    ) {
        return ResponseEntity.ok(
                plataformaService.solicitarCredito(request)
        );
    }

    // Consultar status de credito
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN') " +
                    "and @cpfAuthorizationService.podeAcessarCpf(#request.cpfCliente())"
    )
    @PostMapping("/consultarStatusCredito")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> consultarStatusCredito(
            @P("request") @Valid @RequestBody ConsultarStatusRequest request
    ) {
        return ResponseEntity.ok(
                plataformaService.consultarStatusPorCpf(request)
        );
    }

    // Buscar solicitacao por ID
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @PostMapping("/buscarSolicitacaoPorId")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> buscarSolicitacaoPorId(
            @Valid @RequestBody BuscarSolicitacaoRequest request
    ) {
        return ResponseEntity.ok(
                plataformaService.buscarPorId(request)
        );
    }

    // Listar todas as solicitacoes
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @PostMapping("/listarTodasSolicitacoes")
    public ResponseEntity<List<SolicitacaoCreditoResponseDTO>> listarTodasSolicitacoes() {
        return ResponseEntity.ok(
                plataformaService.listarTodasSolicitacoes()
        );
    }

    // Aprovar solicitacao
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @PostMapping("/aprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> aprovarSolicitacao(
            @Valid @RequestBody AprovarSolicitacaoRequest request
    ) {
        return ResponseEntity.ok(
                plataformaService.aprovarSolicitacao(request)
        );
    }

    // Reprovar solicitacao
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @PostMapping("/reprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponseDTO> reprovarSolicitacao(
            @Valid @RequestBody ReprovarSolicitacaoRequest request
    ) {
        return ResponseEntity.ok(
                plataformaService.reprovarSolicitacao(request)
        );
    }

    // Consultar cliente no Core Bancario
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @PostMapping("/consultarClienteCore")
    public ResponseEntity<Map<String, Object>> consultarClienteCore(
            @Valid @RequestBody ConsultarStatusRequest request
    ) {
        return ResponseEntity.ok(
                coreBancarioGateway.consultarContaCliente(
                        request.cpfCliente()
                )
        );
    }
}
