package br.com.fiap.bank.plataforma.controller;

import br.com.fiap.bank.plataforma.application.dto.LoginRequest;
import br.com.fiap.bank.plataforma.application.dto.LoginResponse;
import br.com.fiap.bank.plataforma.application.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.autenticar(request);

        return ResponseEntity.ok(response);
    }
}
