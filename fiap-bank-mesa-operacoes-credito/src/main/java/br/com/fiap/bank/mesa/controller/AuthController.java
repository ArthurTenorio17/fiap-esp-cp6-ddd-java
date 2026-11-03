package br.com.fiap.bank.mesa.controller;

import br.com.fiap.bank.mesa.auth.TokenCookieService;
import br.com.fiap.bank.mesa.client.PlataformaCreditoFeignClient;
import br.com.fiap.bank.mesa.dto.LoginRequestDTO;
import br.com.fiap.bank.mesa.dto.LoginResponseDTO;
import feign.FeignException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final PlataformaCreditoFeignClient plataformaClient;
    private final TokenCookieService tokenCookieService;

    public AuthController(
            PlataformaCreditoFeignClient plataformaClient,
            TokenCookieService tokenCookieService) {
        this.plataformaClient = plataformaClient;
        this.tokenCookieService = tokenCookieService;
    }

    @GetMapping("/login")
    public String telaLogin(
            @RequestParam(required = false) String motivo,
            Model model) {

        if ("expirada".equals(motivo)) {
            model.addAttribute("error", "Sessão expirada. Entre novamente.");
        } else if ("sem-permissao".equals(motivo)) {
            model.addAttribute("error", "Este usuário não tem perfil de OPERADOR ou ADMIN.");
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String senha,
            HttpServletResponse response,
            Model model) {

        try {
            LoginResponseDTO login = plataformaClient.login(
                    new LoginRequestDTO(email, senha)
            );

            tokenCookieService.salvar(
                    response,
                    login.accessToken(),
                    login.expiresIn()
            );

            return "redirect:/dashboard";

        } catch (FeignException.Unauthorized | FeignException.BadRequest e) {
            model.addAttribute("error", "E-mail ou senha incorretos.");

        } catch (Exception e) {
            log.warn("Plataforma de Credito indisponivel no login: {}", e.getClass().getSimpleName());
            model.addAttribute("error", "Plataforma de Crédito indisponível. Tente novamente.");
        }

        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        tokenCookieService.limpar(response);
        return "redirect:/login";
    }
}
