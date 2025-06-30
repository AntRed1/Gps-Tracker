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

package com.arojas.gpstracker.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.arojas.gpstracker.dto.LoginRequest;
import com.arojas.gpstracker.dto.RegisterRequest;
import com.arojas.gpstracker.dto.TokenResponse;
import com.arojas.gpstracker.entities.User;
import com.arojas.gpstracker.exception.BadRequestException;
import com.arojas.gpstracker.exception.InvalidCredentialsException;
import com.arojas.gpstracker.exception.UnauthorizedException;
import com.arojas.gpstracker.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author neta1
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final RedisService redisService;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  /**
   * Registra un nuevo usuario y genera tokens.
   *
   * @param request Datos del registro
   * @return Tokens de acceso y refresh
   */
  public TokenResponse register(RegisterRequest request) {
    log.info("Registrando nuevo usuario: {}", request.getEmail());
    try {
      User user = userService.register(
          request.getEmail(),
          request.getPassword(),
          request.getFullName());
      return generateAndStoreTokens(user.getEmail());
    } catch (IllegalArgumentException e) {
      log.warn("Registro fallido para usuario {}: {}", request.getEmail(), e.getMessage());
      throw new BadRequestException("Datos inválidos para registro");
    }
  }

  /**
   * Autentica usuario y genera tokens.
   *
   * @param request Datos para login
   * @return Tokens de acceso y refresh
   */
  public TokenResponse login(LoginRequest request) {
    log.info("Intento de login para usuario: {}", request.getEmail());

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getEmail(),
              request.getPassword()));
    } catch (AuthenticationException e) {
      log.warn("Login fallido para usuario: {}", request.getEmail());
      throw new InvalidCredentialsException("Credenciales inválidas");
    }

    return generateAndStoreTokens(request.getEmail());
  }

  /**
   * Refresca tokens validando el refresh token.
   *
   * @param refreshToken Refresh token enviado
   * @return Nuevos tokens de acceso y refresh
   */
  public TokenResponse refresh(String refreshToken) {
    if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
      log.warn("Refresh token inválido o expirado");
      throw new BadRequestException("Refresh token inválido o expirado");
    }

    String email = jwtUtil.extractEmail(refreshToken);

    if (!redisService.isValidRefreshToken(email, refreshToken)) {
      log.warn("Refresh token revocado o no registrado para usuario {}", email);
      throw new UnauthorizedException("Refresh token revocado o no registrado");
    }

    return generateAndStoreTokens(email);
  }

  /**
   * Elimina el refresh token para cerrar sesión.
   *
   * @param email Email del usuario que cierra sesión
   */
  public void logout(String email) {
    log.info("Cerrando sesión para usuario: {}", email);
    redisService.deleteRefreshToken(email);
  }

  /**
   * Genera y almacena tokens para un usuario.
   *
   * @param email Email del usuario
   * @return TokenResponse con access y refresh tokens
   */
  private TokenResponse generateAndStoreTokens(String email) {
    String access = jwtUtil.generateAccessToken(email);
    String refresh = jwtUtil.generateRefreshToken(email);
    redisService.storeRefreshToken(email, refresh);
    log.debug("Tokens generados y almacenados para usuario {}", email);
    return new TokenResponse(access, refresh);
  }
}