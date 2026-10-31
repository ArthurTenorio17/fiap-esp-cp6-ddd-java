package br.com.fiap.bank.plataforma.exception;

import br.com.fiap.bank.plataforma.domain.exception.SolicitacaoNaoEncontradaException;
import br.com.fiap.bank.plataforma.domain.exception.CoreBancarioIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(
            IllegalArgumentException ex) {

        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> tratarEstadoInvalido(
            IllegalStateException ex) {

        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> tratarFalhaHttp(
            RestClientException ex) {

        return resposta(
                HttpStatus.BAD_GATEWAY,
                "Falha na comunicacao com o Core Bancario."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroInterno(
            Exception ex) {

        ex.printStackTrace();

        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor."
        );
    }

    @ExceptionHandler(CoreBancarioIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> tratarCoreIndisponivel(
            CoreBancarioIndisponivelException ex) {

        return resposta(
                HttpStatus.BAD_GATEWAY,
                ex.getMessage()
        );
    }

    @ExceptionHandler(SolicitacaoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> tratarSolicitacaoNaoEncontrada(
            SolicitacaoNaoEncontradaException ex) {

        return resposta(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

}
