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

package com.arojas.gpstracker.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.gpstracker.dto.LoginRequest;
import com.arojas.gpstracker.dto.LogoutRequest;
import com.arojas.gpstracker.dto.RefreshTokenRequest;
import com.arojas.gpstracker.dto.RegisterRequest;
import com.arojas.gpstracker.dto.TokenResponse;
import com.arojas.gpstracker.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse; // Import correcto de Swagger
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * @author arojas
 *         Controller responsible for authentication and token management.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "Register a new user", description = "Creates a new user and returns JWT tokens")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User registered successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  @PostMapping("/register")
  public ResponseEntity<com.arojas.gpstracker.dto.ApiResponse<TokenResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(com.arojas.gpstracker.dto.ApiResponse.success(authService.register(request)));
  }

  @Operation(summary = "User login", description = "Authenticates a user and returns JWT tokens")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login successful"),
      @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  @PostMapping("/login")
  public ResponseEntity<com.arojas.gpstracker.dto.ApiResponse<TokenResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(com.arojas.gpstracker.dto.ApiResponse.success(authService.login(request)));
  }

  @Operation(summary = "Refresh tokens", description = "Refreshes access and refresh tokens")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
      @ApiResponse(responseCode = "401", description = "Invalid refresh token")
  })
  @PostMapping("/refresh")
  public ResponseEntity<com.arojas.gpstracker.dto.ApiResponse<TokenResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(com.arojas.gpstracker.dto.ApiResponse.success(
        authService.refresh(request.getRefreshToken())));
  }

  @Operation(summary = "Logout user", description = "Revokes refresh token in Redis")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Logout successful")
  })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
    authService.logout(request.getEmail());
    return ResponseEntity.noContent().build();
  }
}
