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

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.arojas.gpstracker.dto.GpsLocationMessage;
import com.arojas.gpstracker.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author neta1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GpsLocationConsumer {

  private final GpsLocationService gpsLocationService;

  @KafkaListener(topics = "gps-locations", groupId = "gps-tracker-group")
  public void processLocation(GpsLocationMessage message) {
    log.info("Processing location for device: {}, lat: {}, lng: {}",
        message.getDeviceId(), message.getLatitude(), message.getLongitude());

    // Validar coordenadas
    if (message.getLatitude() < -90.0 || message.getLatitude() > 90.0) {
      log.error("Invalid latitude: {}", message.getLatitude());
      throw new BadRequestException("Latitude must be between -90 and 90");
    }
    if (message.getLongitude() < -180.0 || message.getLongitude() > 180.0) {
      log.error("Invalid longitude: {}", message.getLongitude());
      throw new BadRequestException("Longitude must be between -180 and 180");
    }

    gpsLocationService.saveLocation(message.getDeviceId(), message.getLatitude(), message.getLongitude());
  }
}
