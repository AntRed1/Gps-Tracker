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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.gpstracker.dto.ApiResponseWrapper;
import com.arojas.gpstracker.dto.GpsEventDTO;
import com.arojas.gpstracker.dto.GpsEventRequest;
import com.arojas.gpstracker.entities.GpsEvent;
import com.arojas.gpstracker.entities.GpsEvent.EventType;
import com.arojas.gpstracker.mappers.GpsEventMapper;
import com.arojas.gpstracker.services.GpsEventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing GPS events.
 *
 * @author neta1
 */
@RestController
@RequestMapping("/gps-events")
@RequiredArgsConstructor
@Slf4j
public class GpsEventController {
  private final GpsEventService gpsEventService;
  private final GpsEventMapper gpsEventMapper;

  @PostMapping("/device/{deviceId}")
  public ResponseEntity<ApiResponseWrapper<GpsEventDTO>> registerEvent(
      @PathVariable Long deviceId,
      @Valid @RequestBody GpsEventRequest request) {
    log.info("Registering event for device: {}", deviceId);
    GpsEvent event = gpsEventService.registerEvent(deviceId, request.getEventType());
    return ResponseEntity.ok(ApiResponseWrapper.success(gpsEventMapper.toDto(event)));
  }

  @GetMapping("/device/{deviceId}")
  public ResponseEntity<ApiResponseWrapper<Page<GpsEventDTO>>> getRecentEvents(
      @PathVariable Long deviceId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching recent events for device: {}, page: {}, size: {}", deviceId, page, size);
    Pageable pageable = PageRequest.of(page, size);
    Page<GpsEvent> events = gpsEventService.getRecentEvents(deviceId, pageable);
    Page<GpsEventDTO> dtos = events.map(gpsEventMapper::toDto);
    return ResponseEntity.ok(ApiResponseWrapper.success(dtos));
  }

  @GetMapping("/device/{deviceId}/type")
  public ResponseEntity<ApiResponseWrapper<List<GpsEventDTO>>> getEventsByType(
      @PathVariable Long deviceId,
      @RequestParam EventType type) {
    log.info("Fetching events of type {} for device: {}", type, deviceId);
    List<GpsEventDTO> dtos = gpsEventService.getEventsByType(deviceId, type)
        .stream()
        .map(gpsEventMapper::toDto)
        .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponseWrapper.success(dtos));
  }
}