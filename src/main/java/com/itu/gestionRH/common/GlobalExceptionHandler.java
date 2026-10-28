package com.itu.gestionRH.common;

import java.time.Instant;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError.Detail(e.getField(), e.getDefaultMessage())).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Les données envoyées sont invalides.", req, details);
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Un ou plusieurs paramètres ou champs sont invalides.", req, List.of());
    }
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), req, List.of());
    }
    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ApiError> business(BusinessRuleException ex, HttpServletRequest req) {
        return response(ex.getStatus(), ex.getCode(), ex.getMessage(), req, List.of());
    }
    @ExceptionHandler(AuthException.class)
    ResponseEntity<ApiError> auth(AuthException ex, HttpServletRequest req) {
        return response(ex.getStatus(), ex.getCode(), ex.getMessage(), req, List.of());
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Accès refusé.", req, List.of());
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> unauthorized(AuthenticationException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentification requise.", req, List.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.error("Violation d'intégrité en traitant {} : {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage(), ex);
        return response(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "Les données entrent en conflit avec une règle d'intégrité.", req, List.of());
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> uploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return response(HttpStatus.valueOf(413), "FICHIER_TROP_VOLUMINEUX", "Le fichier dépasse la taille maximale autorisée.", req, List.of());
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> status(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String code = status == HttpStatus.NOT_FOUND ? "RESOURCE_NOT_FOUND" : "HTTP_" + status.value();
        String message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return response(status, code, message, req, List.of());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Une erreur interne est survenue.", req, List.of());
    }
    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                               HttpServletRequest req, List<ApiError.Detail> details) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code,
                message, req.getRequestURI(), details));
    }
}
