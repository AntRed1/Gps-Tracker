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

package com.arojas.gpstracker.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.PostConstruct;

/**
 *
 * @author neta1
 */
@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token.expiration}")
  private long accessTokenExpirationMs;

  @Value("${jwt.refresh-token.expiration}")
  private long refreshTokenExpirationMs;

  private Algorithm algorithm;

  @PostConstruct
  public void init() {
    algorithm = Algorithm.HMAC256(secret);
  }

  public String generateAccessToken(String email) {
    return JWT.create()
        .withSubject(email)
        .withClaim("type", "access")
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
        .sign(algorithm);
  }

  public String generateRefreshToken(String email) {
    return JWT.create()
        .withSubject(email)
        .withClaim("type", "refresh")
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
        .sign(algorithm);
  }

  public boolean validateToken(String token) {
    try {
      JWT.require(algorithm).build().verify(token);
      return true;
    } catch (JWTVerificationException e) {
      return false;
    }
  }

  public String extractEmail(String token) {
    DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
    return decodedJWT.getSubject();
  }

  public boolean isRefreshToken(String token) {
    return JWT.decode(token).getClaim("type").asString().equals("refresh");
  }
}