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
import java.util.Optional;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.arojas.gpstracker.dto.GpsLocationDTO;
import com.arojas.gpstracker.entities.GpsLocation;
import com.arojas.gpstracker.exception.BadRequestException;
import com.arojas.gpstracker.exception.NotFoundException;
import com.arojas.gpstracker.mappers.GpsLocationMapper;
import com.arojas.gpstracker.repositories.DeviceRepository;
import com.arojas.gpstracker.repositories.GpsLocationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author neta1
 *         * Service for managing GPS locations of devices.
 * 
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class GpsLocationService {

  private final GpsLocationRepository locationRepository;
  private final DeviceRepository deviceRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final GpsLocationMapper gpsLocationMapper;

  @Transactional
  public GpsLocation saveLocation(
      Long deviceId,
      @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90") @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") double latitude,
      @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180") @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") double longitude) {
    if (!deviceRepository.existsById(deviceId)) {
      throw new NotFoundException("Device not found with ID: " + deviceId);
    }

    log.info("Saving location for device {}: ({}, {})", deviceId, latitude, longitude);

    locationRepository.insertLocation(deviceId, latitude, longitude);

    GpsLocation location = locationRepository.findTopByDeviceIdOrderByTimestampDesc(deviceId)
        .orElseThrow(() -> new NotFoundException("Failed to retrieve newly inserted location for device: " + deviceId));

    GpsLocationDTO dto = gpsLocationMapper.toDto(location);
    messagingTemplate.convertAndSend("/topic/gps-updates/" + deviceId, dto);
    log.debug("Sent WebSocket notification for device {}: {}", deviceId, dto);

    return location;
  }

  @Cacheable(value = "deviceLocations", key = "#deviceId + '-' + #page + '-' + #size")
  @Transactional(readOnly = true)
  public Page<GpsLocation> getLocationsForDevice(Long deviceId, int page, int size) {
    if (!deviceRepository.existsById(deviceId)) {
      throw new NotFoundException("Device not found with ID: " + deviceId);
    }

    log.debug("Fetching locations for device ID {} (page: {}, size: {})", deviceId, page, size);
    Pageable pageable = PageRequest.of(page, size);
    return locationRepository.findAllByDeviceIdOrderByTimestampDesc(deviceId, pageable);
  }

  @Cacheable(value = "lastLocation", key = "#deviceId")
  @Transactional(readOnly = true)
  public Optional<GpsLocation> getLastLocation(Long deviceId) {
    log.debug("Fetching last location for device ID {}", deviceId);
    return locationRepository.findTopByDeviceIdOrderByTimestampDesc(deviceId);
  }

  @Transactional(readOnly = true)
  public List<GpsLocation> getLocationsByTimeRange(Long deviceId, LocalDateTime start, LocalDateTime end) {
    if (!deviceRepository.existsById(deviceId)) {
      throw new NotFoundException("Device not found with ID: " + deviceId);
    }

    if (start.isAfter(end)) {
      throw new BadRequestException("Start time must be before end time");
    }

    log.debug("Fetching locations for device ID {} between {} and {}", deviceId, start, end);
    return locationRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(deviceId, start, end);
  }
}