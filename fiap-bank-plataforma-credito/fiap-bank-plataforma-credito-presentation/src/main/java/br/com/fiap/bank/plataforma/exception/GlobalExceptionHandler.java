
package br.com.fiap.bank.plataforma.exception;

import br.com.fiap.bank.plataforma.application.exception.CredenciaisInvalidasException;
import br.com.fiap.bank.plataforma.domain.exception.CoreBancarioIndisponivelException;
import br.com.fiap.bank.plataforma.domain.exception.SolicitacaoNaoEncontradaException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Padroniza as respostas de erro da API
    private ResponseEntity<Map<String, Object>> resposta(
            HttpStatus status,
            String mensagem) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", mensagem);

        return ResponseEntity.status(status).body(body);
    }

    // Erro de validacao dos dados
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(
            MethodArgumentNotValidException ex) {

        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": "
                        + erro.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("Dados invalidos.");

        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    // E-mail ou senha incorretos no login
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, Object>> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException ex) {

        return resposta(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
    }

    // Corpo da requisicao ausente ou com JSON invalido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(
            HttpMessageNotReadableException ex) {

        return resposta(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisicao esta ausente ou invalido."
        );
    }

    // Argumento invalido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(
            IllegalArgumentException ex) {

        return resposta(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    // Estado invalido
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> tratarEstadoInvalido(
            IllegalStateException ex) {

        return resposta(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    // Autorizacao negada pelo @PreAuthorize
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> tratarAutorizacaoNegada(
            AuthorizationDeniedException ex) {

        return resposta(
                HttpStatus.FORBIDDEN,
                "Voce nao tem permissao para acessar este recurso."
        );
    }

    // Outros erros de acesso negado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> tratarAcessoNegado(
            AccessDeniedException ex) {

        return resposta(
                HttpStatus.FORBIDDEN,
                "Voce nao tem permissao para acessar este recurso."
        );
    }

    // Falha na comunicacao HTTP
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> tratarFalhaHttp(
            RestClientException ex) {

        return resposta(
                HttpStatus.BAD_GATEWAY,
                "Falha na comunicacao com o Core Bancario."
        );
    }

    // Core Bancario indisponivel
    @ExceptionHandler(CoreBancarioIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> tratarCoreIndisponivel(
            CoreBancarioIndisponivelException ex) {

        return resposta(
                HttpStatus.BAD_GATEWAY,
                ex.getMessage()
        );
    }

    // Solicitacao nao encontrada
    @ExceptionHandler(SolicitacaoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> tratarSolicitacaoNaoEncontrada(
            SolicitacaoNaoEncontradaException ex) {

        return resposta(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    // Erro interno inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroInterno(
            Exception ex) {

        // Erros do proprio Spring MVC (rota inexistente, metodo nao suportado,
        // parametro ausente) mantem o status correto em vez de virar 500.
        if (ex instanceof ErrorResponse erroMvc) {
            HttpStatus status = HttpStatus.valueOf(
                    erroMvc.getStatusCode().value()
            );

            return resposta(status, status.getReasonPhrase());
        }

        log.error("Erro interno inesperado", ex);

        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor."
        );
    }
}
