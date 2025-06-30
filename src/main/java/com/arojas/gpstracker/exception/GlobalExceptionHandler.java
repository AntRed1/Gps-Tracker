/*
 * The MIT License
 *
 * Copyright 2025 neta1.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.arojas.gpstracker.exception;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author neta1
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
    log.warn("UserNotFoundException: {}", ex.getMessage());
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
    log.warn("InvalidCredentialsException: {}", ex.getMessage());
    return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
    log.warn("BadRequestException: {}", ex.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
    log.warn("UnauthorizedException: {}", ex.getMessage());
    return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  // Manejo de validación @Valid (captura todas las violaciones de restricción)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    log.warn("Validation errors: {}", errors);
    return ResponseEntity.badRequest().body(errors);
  }

  // Para manejar excepción cuando un recurso no se encuentra, si usas
  // Optional.orElseThrow
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
    log.warn("NotFoundException: {}", ex.getMessage());
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  // Manejo de excepciones de acceso denegado (Security)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
    log.warn("AccessDeniedException: {}", ex.getMessage());
    return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado");
  }

  // Manejo de excepciones de autenticación (AuthenticationException)
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
    log.warn("AuthenticationException: {}", ex.getMessage());
    return buildResponse(HttpStatus.UNAUTHORIZED, "Error de autenticación");
  }

  // Manejo genérico para excepciones no controladas
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleAllUncaughtException(Exception ex) {
    log.error("Error inesperado: ", ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
  }

  // Método helper para evitar repetir código
  private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of("error", message));
  }
}