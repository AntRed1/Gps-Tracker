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

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arojas.gpstracker.entities.User;
import com.arojas.gpstracker.exception.BadRequestException;
import com.arojas.gpstracker.exception.UserNotFoundException;
import com.arojas.gpstracker.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author neta1
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User register(String email, String password, String fullName) {
    log.debug("Intentando registrar usuario: {}", email);

    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Ya existe un usuario con ese email.");
    }

    User user = User.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .fullName(fullName)
        .build();

    return userRepository.save(user);
  }

  public User updateFullName(String email, String fullName) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

    user.setFullName(fullName);
    return userRepository.save(user);
  }

  public void changePassword(String email, String currentPassword, String newPassword) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new BadRequestException("La contraseña actual es incorrecta");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }
}