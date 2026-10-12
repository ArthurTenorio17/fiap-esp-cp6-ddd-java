package br.com.fiap.bank.mesa.client;

import br.com.fiap.bank.mesa.dto.AprovarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.BuscarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.ReprovarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.SolicitacaoCreditoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "plataformaCreditoClient", url = "${plataforma.credito.url:http://localhost:8081}")
public interface PlataformaCreditoFeignClient {

    @PostMapping("/api/credito/listarTodasSolicitacoes")
    List<SolicitacaoCreditoDTO> listarTodasSolicitacoes();

    @PostMapping("/api/credito/buscarSolicitacaoPorId")
    SolicitacaoCreditoDTO buscarSolicitacaoPorId(@RequestBody BuscarSolicitacaoRequestDTO request);

    @PostMapping("/api/credito/aprovarSolicitacao")
    SolicitacaoCreditoDTO aprovarSolicitacao(@RequestBody AprovarSolicitacaoRequestDTO request);

    @PostMapping("/api/credito/reprovarSolicitacao")
    SolicitacaoCreditoDTO reprovarSolicitacao(@RequestBody ReprovarSolicitacaoRequestDTO request);
}
