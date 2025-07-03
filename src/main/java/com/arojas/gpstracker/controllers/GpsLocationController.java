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

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.gpstracker.dto.ApiResponseWrapper;
import com.arojas.gpstracker.dto.GpsLocationDTO;
import com.arojas.gpstracker.dto.GpsLocationMessage;
import com.arojas.gpstracker.dto.GpsLocationRequest;
import com.arojas.gpstracker.entities.GpsLocation;
import com.arojas.gpstracker.mappers.GpsLocationMapper;
import com.arojas.gpstracker.services.GpsLocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author neta1
 *         * Controller for managing GPS locations.
 *
 */
@RestController
@RequestMapping("/gps-locations")
@RequiredArgsConstructor
@Slf4j
public class GpsLocationController {

  private final GpsLocationService gpsLocationService;
  private final GpsLocationMapper gpsLocationMapper;
  private final KafkaTemplate<String, GpsLocationMessage> kafkaTemplate;

  @PostMapping("/device/{deviceId}")
  public ResponseEntity<ApiResponseWrapper<GpsLocationDTO>> saveLocation(
      @PathVariable Long deviceId,
      @Valid @RequestBody GpsLocationRequest request) {
    log.info("Sending location for device: {}, lat: {}, lng: {}",
        deviceId, request.getLatitude(), request.getLongitude());
    GpsLocationMessage message = new GpsLocationMessage();
    message.setDeviceId(deviceId);
    message.setLatitude(request.getLatitude());
    message.setLongitude(request.getLongitude());
    kafkaTemplate.send("gps-locations", String.valueOf(deviceId), message);
    return ResponseEntity.accepted().build();
  }

  @GetMapping("/device/{deviceId}")
  public ResponseEntity<ApiResponseWrapper<Page<GpsLocationDTO>>> getLocations(
      @PathVariable Long deviceId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<GpsLocation> locations = gpsLocationService.getLocationsForDevice(deviceId, page, size);
    Page<GpsLocationDTO> dtos = locations.map(gpsLocationMapper::toDto);
    return ResponseEntity.ok(ApiResponseWrapper.success(dtos));
  }

  @GetMapping("/device/{deviceId}/last")
  public ResponseEntity<ApiResponseWrapper<GpsLocationDTO>> getLastLocation(@PathVariable Long deviceId) {
    Optional<GpsLocation> location = gpsLocationService.getLastLocation(deviceId);
    return location.map(loc -> ResponseEntity.ok(ApiResponseWrapper.success(gpsLocationMapper.toDto(loc))))
        .orElse(ResponseEntity.ok(ApiResponseWrapper.error("No location found for device ID: " + deviceId)));
  }
}