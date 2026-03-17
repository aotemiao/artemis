package com.aotemiao.artemis.auth.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 认证相关异常处理，返回 401 等时的统一 JSON 体。
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthController.InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(AuthController.InvalidCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("code", "UNAUTHORIZED", "message", e.getMessage()));
    }
}
