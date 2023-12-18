package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity handlePostNotFoundException(PostNotFoundException exception) {
        log.debug("handling exception: {}", exception);
        return status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }
}
