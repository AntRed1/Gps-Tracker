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

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.arojas.gpstracker.dto.DeviceRegisterRequest;
import com.arojas.gpstracker.entities.Device;
import com.arojas.gpstracker.entities.User;
import com.arojas.gpstracker.exception.BadRequestException;
import com.arojas.gpstracker.exception.UserNotFoundException;
import com.arojas.gpstracker.repositories.DeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing devices associated with users.
 *
 * @author neta1
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceRepository deviceRepository;

  public Device registerDevice(DeviceRegisterRequest request, User user) {
    log.info("Registrando dispositivo '{}' para el usuario '{}'", request.getDeviceIdentifier(), user.getEmail());

    if (deviceRepository.findByDeviceIdentifier(request.getDeviceIdentifier()).isPresent()) {
      log.warn("Dispositivo ya registrado: {}", request.getDeviceIdentifier());
      throw new BadRequestException("Este dispositivo ya está registrado.");
    }

    Device device = Device.builder()
        .deviceIdentifier(request.getDeviceIdentifier())
        .alias(request.getAlias())
        .activated(false)
        .user(user)
        .build();

    return deviceRepository.save(device);
  }

  @Cacheable(value = "devices", key = "#userId")
  public Page<Device> getUserDevices(Long userId, Pageable pageable) {
    log.debug("Consultando dispositivos para usuario con ID: {}", userId);
    return deviceRepository.findByUserId(userId, pageable);
  }

  public Device toggleDeviceActivation(Long id, User user, boolean activate) {
    Device device = deviceRepository.findById(id)
        .filter(d -> d.getUser().getId().equals(user.getId()))
        .orElseThrow(() -> new UserNotFoundException("Dispositivo no encontrado o no pertenece al usuario."));

    device.setActivated(activate);
    return deviceRepository.save(device);
  }

  public void deleteDevice(Long id, User user) {
    Device device = deviceRepository.findById(id)
        .filter(d -> d.getUser().getId().equals(user.getId()))
        .orElseThrow(() -> new UserNotFoundException("Dispositivo no encontrado o no pertenece al usuario."));

    deviceRepository.delete(device);
    log.info("Dispositivo eliminado: {}", id);
  }

  public Device getDeviceById(Long id, User user) {
    return deviceRepository.findById(id)
        .filter(d -> d.getUser().getId().equals(user.getId()))
        .orElseThrow(() -> new UserNotFoundException("Dispositivo no encontrado o no pertenece al usuario."));
  }
}