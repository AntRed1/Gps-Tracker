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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.gpstracker.dto.ApiResponseWrapper;
import com.arojas.gpstracker.dto.DeviceResponse;
import com.arojas.gpstracker.dto.PasswordChangeRequest;
import com.arojas.gpstracker.dto.UserResponse;
import com.arojas.gpstracker.dto.UserUpdateRequest;
import com.arojas.gpstracker.entities.Device;
import com.arojas.gpstracker.entities.User;
import com.arojas.gpstracker.services.DeviceService;
import com.arojas.gpstracker.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for managing and querying authenticated user data.
 *
 * @author neta1
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final DeviceService deviceService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getFullName());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/me")
  public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody UserUpdateRequest updateRequest) {
    User updatedUser = userService.updateFullName(userDetails.getUsername(), updateRequest.getFullName());
    UserResponse response = new UserResponse(updatedUser.getId(), updatedUser.getEmail(), updatedUser.getFullName());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me/devices")
  public ResponseEntity<ApiResponseWrapper<Page<DeviceResponse>>> listUserDevices(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Pageable pageable = PageRequest.of(page, size);
    Page<Device> devices = deviceService.getUserDevices(user.getId(), pageable);
    Page<DeviceResponse> response = devices
        .map(d -> new DeviceResponse(d.getId(), d.getDeviceIdentifier(), d.getAlias(), d.getActivated()));
    return ResponseEntity.ok(ApiResponseWrapper.success(response));
  }

  @PutMapping("/me/password")
  public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
    userService.changePassword(userDetails.getUsername(),
        passwordChangeRequest.getCurrentPassword(),
        passwordChangeRequest.getNewPassword());
    return ResponseEntity.noContent().build();
  }
}