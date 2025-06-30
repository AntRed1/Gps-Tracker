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

import org.springframework.stereotype.Service;

import com.arojas.gpstracker.entities.Device;
import com.arojas.gpstracker.entities.GpsEvent;
import com.arojas.gpstracker.entities.GpsEvent.EventType;
import com.arojas.gpstracker.exception.NotFoundException;
import com.arojas.gpstracker.repositories.DeviceRepository;
import com.arojas.gpstracker.repositories.GpsEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author neta1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GpsEventService {

  private final GpsEventRepository eventRepository;
  private final DeviceRepository deviceRepository;

  /**
   * Registra un evento GPS (ON/OFF) para un dispositivo.
   *
   * @param deviceId  ID del dispositivo
   * @param eventType Tipo de evento (ON, OFF)
   * @return Evento registrado
   */
  public GpsEvent registerEvent(Long deviceId, EventType eventType) {
    Device device = deviceRepository.findById(deviceId)
        .orElseThrow(() -> new NotFoundException("Dispositivo no encontrado con ID: " + deviceId));

    GpsEvent event = GpsEvent.builder()
        .device(device)
        .eventType(eventType)
        .timestamp(LocalDateTime.now())
        .build();

    log.info("Registrando evento '{}' para dispositivo ID {}", eventType, deviceId);

    return eventRepository.save(event);
  }

  /**
   * Obtiene los últimos N eventos para un dispositivo, ordenados por fecha
   * descendente.
   *
   * @param deviceId ID del dispositivo
   * @param limit    Cantidad máxima de eventos a retornar
   * @return Lista de eventos
   */
  public List<GpsEvent> getRecentEvents(Long deviceId, int limit) {
    log.debug("Consultando últimos {} eventos para dispositivo ID {}", limit, deviceId);
    // Usar el método findTop10ByDeviceIdOrderByTimestampDesc y ajustar el límite si
    // se requiere un método genérico
    return eventRepository.findTop10ByDeviceIdOrderByTimestampDesc(deviceId);
  }

  /**
   * Obtiene eventos filtrados por dispositivo y tipo.
   *
   * @param deviceId  ID del dispositivo
   * @param eventType Tipo de evento
   * @return Lista de eventos filtrados
   */
  public List<GpsEvent> getEventsByType(Long deviceId, EventType eventType) {
    log.debug("Consultando eventos de tipo '{}' para dispositivo ID {}", eventType, deviceId);
    return eventRepository.findByDeviceIdAndEventType(deviceId, eventType);
  }
}