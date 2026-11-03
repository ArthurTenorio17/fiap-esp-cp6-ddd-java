package br.com.fiap.bank.mesa.controller;

import br.com.fiap.bank.mesa.auth.TokenCookieService;
import br.com.fiap.bank.mesa.client.PlataformaCreditoFeignClient;
import br.com.fiap.bank.mesa.dto.AprovarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.BuscarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.ReprovarSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.SolicitacaoCreditoDTO;
import feign.FeignException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MesaOperacoesController {

    private final PlataformaCreditoFeignClient plataformaClient;
    private final TokenCookieService tokenCookieService;

    public MesaOperacoesController(
            PlataformaCreditoFeignClient plataformaClient,
            TokenCookieService tokenCookieService) {
        this.plataformaClient = plataformaClient;
        this.tokenCookieService = tokenCookieService;
    }

    // 401 da Plataforma: sem token, token invalido ou expirado.
    @ExceptionHandler(FeignException.Unauthorized.class)
    public String tratarNaoAutenticado(HttpServletResponse response) {
        tokenCookieService.limpar(response);
        return "redirect:/login?motivo=expirada";
    }

    // 403 da Plataforma: autenticado, mas sem perfil de OPERADOR ou ADMIN.
    @ExceptionHandler(FeignException.Forbidden.class)
    public String tratarSemPermissao(HttpServletResponse response) {
        tokenCookieService.limpar(response);
        return "redirect:/login?motivo=sem-permissao";
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model,
                            @RequestParam(required = false) String success,
                            @RequestParam(required = false) String error) {
        List<SolicitacaoCreditoDTO> solicitacoes;
        try {
            solicitacoes = plataformaClient.listarTodasSolicitacoes();
        } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[MESA DE OPERACOES] Plataforma de Credito indisponivel via Feign Client: " + e.getClass().getSimpleName());
            solicitacoes = new ArrayList<>();
            model.addAttribute("warning", "Aviso: Nao foi possivel conectar a Plataforma de Credito. Exibindo modo offline.");
        }

        long totalCount = solicitacoes.size();
        long pendentes = solicitacoes.stream().filter(s -> "EM_ANALISE".equalsIgnoreCase(s.status())).count();
        long aprovadas = solicitacoes.stream().filter(s -> "APROVADO".equalsIgnoreCase(s.status())).count();
        long reprovadas = solicitacoes.stream().filter(s -> "REPROVADO".equalsIgnoreCase(s.status())).count();
        BigDecimal volumeTotal = solicitacoes.stream()
                .map(SolicitacaoCreditoDTO::valorSolicitado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("solicitacoes", solicitacoes);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendentes", pendentes);
        model.addAttribute("aprovadas", aprovadas);
        model.addAttribute("reprovadas", reprovadas);
        model.addAttribute("volumeTotal", volumeTotal);
        model.addAttribute("success", success);
        model.addAttribute("error", error);

        return "dashboard";
    }

    @GetMapping("/solicitacao/{id}")
    public String telaAnaliseSolicitacao(@PathVariable("id") Long id, Model model,
                                         RedirectAttributes redirect) {
        try {
            SolicitacaoCreditoDTO solicitacao = plataformaClient.buscarSolicitacaoPorId(new BuscarSolicitacaoRequestDTO(id));
            model.addAttribute("solicitacao", solicitacao);

            // Calculos auxiliares de analise de risco para a tela
            if (solicitacao.valorSolicitado() != null && solicitacao.quantidadeParcelas() != null && solicitacao.quantidadeParcelas() > 0) {
                BigDecimal parcelaEstimada = solicitacao.valorSolicitado().divide(BigDecimal.valueOf(solicitacao.quantidadeParcelas()), 2, RoundingMode.HALF_UP);
                model.addAttribute("parcelaEstimada", parcelaEstimada);

                if (solicitacao.rendaMensal() != null && solicitacao.rendaMensal().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal comprometimentoRenda = parcelaEstimada.divide(solicitacao.rendaMensal(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    model.addAttribute("comprometimentoRenda", comprometimentoRenda);
                }
            }

            return "analise";
        } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[MESA DE OPERACOES] Erro ao buscar solicitacao #" + id + ": " + e.getClass().getSimpleName());
            redirect.addAttribute("error", "Erro ao carregar solicitacao " + id);
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/aprovar")
    public String aprovar(@RequestParam("solicitacaoId") Long solicitacaoId,
                          @RequestParam(value = "parecerOperador", required = false) String parecerOperador,
                          RedirectAttributes redirect) {
        try {
            plataformaClient.aprovarSolicitacao(new AprovarSolicitacaoRequestDTO(solicitacaoId, parecerOperador));
            redirect.addAttribute("success", "Solicitacao " + solicitacaoId + " APROVADA com sucesso!");
        } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
            throw e;
        } catch (Exception e) {
            redirect.addAttribute("error", "Erro ao aprovar solicitacao " + solicitacaoId + ". " + motivoFalha(e));
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/reprovar")
    public String reprovar(@RequestParam("solicitacaoId") Long solicitacaoId,
                           @RequestParam(value = "motivoReprovacao", required = false) String motivoReprovacao,
                           RedirectAttributes redirect) {
        try {
            plataformaClient.reprovarSolicitacao(new ReprovarSolicitacaoRequestDTO(solicitacaoId, motivoReprovacao));
            redirect.addAttribute("success", "Solicitacao " + solicitacaoId + " REPROVADA com sucesso!");
        } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
            throw e;
        } catch (Exception e) {
            redirect.addAttribute("error", "Erro ao reprovar solicitacao " + solicitacaoId + ". " + motivoFalha(e));
        }
        return "redirect:/dashboard";
    }

    // Nao expoe na URL o texto bruto do Feign, que contem endereco interno e corpo da resposta.
    private String motivoFalha(Exception e) {
        if (e instanceof FeignException.NotFound) {
            return "Solicitacao nao encontrada.";
        }
        if (e instanceof FeignException.Conflict) {
            return "A solicitacao ja foi finalizada.";
        }
        if (e instanceof FeignException.BadRequest) {
            return "Dados invalidos.";
        }
        return "Plataforma de Credito indisponivel.";
    }
}
