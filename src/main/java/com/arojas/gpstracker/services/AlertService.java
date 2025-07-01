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

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.arojas.gpstracker.dto.AlertResponse;
import com.arojas.gpstracker.entities.Alert;
import com.arojas.gpstracker.entities.Device;
import com.arojas.gpstracker.exception.NotFoundException;
import com.arojas.gpstracker.mappers.AlertMapper;
import com.arojas.gpstracker.repositories.AlertRepository;
import com.arojas.gpstracker.repositories.DeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing alerts.
 *
 * @author neta1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

  private final AlertRepository alertRepository;
  private final DeviceRepository deviceRepository;
  private final AlertMapper alertMapper;
  private final SimpMessagingTemplate messagingTemplate;

  public Alert createAlert(Long deviceId, Alert alert) {
    Device device = deviceRepository.findById(deviceId)
        .orElseThrow(() -> new NotFoundException("Dispositivo no encontrado con ID: " + deviceId));

    alert.setDevice(device);
    alert.setCreatedAt(LocalDateTime.now());
    alert.setResolved(false);

    Alert saved = alertRepository.save(alert);

    // Notificación WebSocket
    AlertResponse response = alertMapper.toResponse(saved);
    messagingTemplate.convertAndSend("/topic/alerts/" + deviceId, response);
    log.info("Alerta enviada a WebSocket para dispositivo {}: {}", deviceId, response.getMessage());

    return saved;
  }

  public Alert resolveAlert(Long alertId) {
    Alert alert = alertRepository.findById(alertId)
        .orElseThrow(() -> new NotFoundException("Alerta no encontrada con ID: " + alertId));

    alert.setResolved(true);
    alert.setResolvedAt(LocalDateTime.now());

    log.info("Marcando alerta ID {} como resuelta", alertId);

    return alertRepository.save(alert);
  }

  public Page<Alert> getAlertsForDevice(Long deviceId, Boolean resolved, Pageable pageable) {
    log.debug("Consultando alertas para dispositivo ID {}, resueltas: {}, página: {}", deviceId, resolved, pageable);
    if (resolved == null) {
      return alertRepository.findByDeviceId(deviceId, pageable);
    } else {
      return alertRepository.findByDeviceIdAndResolved(deviceId, resolved, pageable);
    }
  }

  public List<Alert> getUnresolvedAlerts() {
    log.debug("Consultando alertas no resueltas");
    return alertRepository.findByResolvedFalseOrderByCreatedAtDesc();
  }
}