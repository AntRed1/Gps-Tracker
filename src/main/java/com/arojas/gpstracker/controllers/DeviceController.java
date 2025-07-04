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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.gpstracker.dto.ApiResponseWrapper;
import com.arojas.gpstracker.dto.DeviceRegisterRequest;
import com.arojas.gpstracker.dto.DeviceResponse;
import com.arojas.gpstracker.entities.Device;
import com.arojas.gpstracker.entities.User;
import com.arojas.gpstracker.exception.UserNotFoundException;
import com.arojas.gpstracker.mappers.DeviceMapper;
import com.arojas.gpstracker.services.DeviceService;
import com.arojas.gpstracker.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing devices.
 *
 * @author neta1
 */
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

  private final DeviceService deviceService;
  private final UserService userService;
  private final DeviceMapper deviceMapper;

  @PostMapping("/register")
  public ResponseEntity<ApiResponseWrapper<DeviceResponse>> registerDevice(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody DeviceRegisterRequest request) {
    log.info("Solicitud para registrar dispositivo: {}", request.getDeviceIdentifier());
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    Device device = deviceService.registerDevice(request, user);
    return ResponseEntity.ok(ApiResponseWrapper.success(deviceMapper.toResponse(device)));
  }

  @GetMapping
  public ResponseEntity<ApiResponseWrapper<Page<DeviceResponse>>> getAllUserDevices(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.debug("Listando dispositivos del usuario: {}", userDetails.getUsername());
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    Pageable pageable = PageRequest.of(page, size);
    Page<Device> devices = deviceService.getUserDevices(user.getId(), pageable);
    Page<DeviceResponse> response = devices.map(deviceMapper::toResponse);
    return ResponseEntity.ok(ApiResponseWrapper.success(response));
  }

  @PatchMapping("/{deviceId}/activation")
  public ResponseEntity<ApiResponseWrapper<DeviceResponse>> toggleDeviceActivation(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long deviceId,
      @RequestParam boolean activate) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    Device device = deviceService.toggleDeviceActivation(deviceId, user, activate);
    return ResponseEntity.ok(ApiResponseWrapper.success(deviceMapper.toResponse(device)));
  }

  @DeleteMapping("/{deviceId}")
  public ResponseEntity<Void> deleteDevice(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long deviceId) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    deviceService.deleteDevice(deviceId, user);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{deviceId}")
  public ResponseEntity<ApiResponseWrapper<DeviceResponse>> getDeviceById(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long deviceId) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    Device device = deviceService.getDeviceById(deviceId, user);
    return ResponseEntity.ok(ApiResponseWrapper.success(deviceMapper.toResponse(device)));
  }
}