package com.learning.movie.controller.advice;

import org.hibernate.metamodel.UnsupportedMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnsupportedMappingException.class)
    public Mono<ResponseEntity<String>> handleUnsupportedMediaType(UnsupportedMappingException ex) {
        LOGGER.warn("Unsupported media type: {}", ex.getMessage());

        return Mono.just(
                ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("Unsupported media type: " + ex.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<String>> handleValidationException(WebExchangeBindException ex) {
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> LOGGER.error("Validation error: {}", error.getDefaultMessage()));

        return Mono.just(ResponseEntity.badRequest().body("Validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleGeneralException(Exception e) {
        LOGGER.error("Exception occurred: {}; due to: {}", e.getMessage(), e.getCause(), e);
        return Mono.just(ResponseEntity.internalServerError().body(Map.of("message", "Error occurred")));
    }
}
