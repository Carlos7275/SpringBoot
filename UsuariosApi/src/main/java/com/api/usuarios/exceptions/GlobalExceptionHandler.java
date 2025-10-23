package com.api.usuarios.exceptions;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.api.usuarios.utilities.ResponseUtil;
import com.thewaterfall.throttler.configuration.exception.ThrottleException;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

       private static final Pattern DUPLICATE_KEY_PATTERN =
    Pattern.compile("index: (.+?) dup key: \\{ (.+?): \"([^\"]+)\" \\}");
        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Maneja errores de validación en DTOs anotados con @Valid.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
                List<String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                                .collect(Collectors.toList());

                log.warn("Error de validación: {}", errors);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ResponseUtil.Response("Error de validación", errors));
        }

        /**
         * Maneja excepciones lanzadas con ResponseStatusException.
         */
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
                log.warn("Error HTTP controlado: {}", ex.getReason());
                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(ResponseUtil.Response(ex.getReason()));
        }

        /**
         * Maneja cuerpos vacíos o JSON mal formados.
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<?> handleMissingRequestBody(HttpMessageNotReadableException ex) {
                log.warn("Petición mal formada: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ResponseUtil.Response("Cuerpo de la solicitud faltante o mal formado"));
        }

        /**
         * Maneja intentos de acceso no autorizados.
         */
        @ExceptionHandler(AccessDeniedException.class)

        public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
                log.warn("Acceso denegado: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ResponseUtil.Response("No tienes permisos para realizar esta acción"));
        }

        /**
         * Captura cualquier excepción no manejada.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleGenericException(Exception ex) {
                log.error("Error interno del servidor", ex);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ResponseUtil.Response("Error interno del servidor: " + ex.getMessage()));
        }

      @ExceptionHandler(DuplicateKeyException.class)
public ResponseEntity<?> handleDuplicateKey(DuplicateKeyException ex) {
    String message = "Valor duplicado en la base de datos";
    String mongoMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

    if (mongoMessage != null) {
        Matcher matcher = DUPLICATE_KEY_PATTERN.matcher(mongoMessage);
        if (matcher.find()) {
            String indexName = matcher.group(1);       // nombre del índice
            String fieldName = matcher.group(2);       // campo duplicado
            String duplicateValue = matcher.group(3);  // valor duplicado
            message = "El valor '" + duplicateValue + "' ya existe en el campo '" + fieldName + "'";
        
        }
    }

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ResponseUtil.Response(message));
}


        @ExceptionHandler(JwtException.class)
        public ResponseEntity<?> handleJwtException(JwtException ex) {
                log.warn("JWT inválido: {}", ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ResponseUtil.Response("Acceso no autorizado: " + ex.getMessage()));
        }

        @ExceptionHandler(value = ThrottleException.class)
        protected ResponseEntity<Object> handleThrottleException(ThrottleException e,
                        HttpServletRequest request) {
                String mensaje = "Has excedido el número de intentos permitidos. " +
                                "Por favor, inténtalo nuevamente más tarde.";
                return ResponseEntity
                                .status(HttpStatus.TOO_MANY_REQUESTS)
                                .body(ResponseUtil.Response(mensaje));
        }

}
