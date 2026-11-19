package com.covoiturage.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Erreur de validation sur {} : {}", request.getRequestURI(), errors);

        ProblemDetail problem = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Erreur de validation",
                "Les données envoyées ne sont pas valides",
                request.getRequestURI()
        );
        problem.setProperty("erreurs", errors);
        return problem;
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("Requête invalide (400) sur {} : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Requête invalide", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ProblemDetail handleForbidden(ForbiddenActionException ex, HttpServletRequest request) {
        log.warn("Accès refusé (403) sur {} : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.FORBIDDEN, "Action non autorisée", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Ressource introuvable (404) sur {} : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        log.warn("Règle métier refusée (409) sur {} : {}", request.getRequestURI(), ex.getMessage());
        return buildProblemDetail(HttpStatus.CONFLICT, "Conflit avec une règle métier", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erreur interne (500) sur {} : ", request.getRequestURI(), ex);
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne du serveur",
                "Une erreur inattendue s'est produite",
                request.getRequestURI()
        );
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(path));
        problem.setProperty("timestamp", LocalDateTime.now());
        problem.setProperty("codeHttp", status.value());
        problem.setProperty("typeErreur", title);
        problem.setProperty("chemin", path);
        return problem;
    }
}