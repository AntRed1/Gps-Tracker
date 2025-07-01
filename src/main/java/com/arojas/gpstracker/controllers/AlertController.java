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

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.gpstracker.dto.AlertRequest;
import com.arojas.gpstracker.dto.AlertResponse;
import com.arojas.gpstracker.dto.ApiResponse;
import com.arojas.gpstracker.entities.Alert;
import com.arojas.gpstracker.mappers.AlertMapper;
import com.arojas.gpstracker.services.AlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing alerts.
 *
 * @author neta1
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {
  private final AlertService alertService;
  private final AlertMapper alertMapper;

  @PostMapping("/device/{deviceId}")
  public ResponseEntity<ApiResponse<AlertResponse>> createAlert(
      @PathVariable Long deviceId,
      @Valid @RequestBody AlertRequest alertRequest) {
    log.info("Creating alert for device: {}", deviceId);
    Alert alert = alertMapper.toEntity(alertRequest);
    Alert created = alertService.createAlert(deviceId, alert);
    return ResponseEntity.ok(ApiResponse.success(alertMapper.toResponse(created)));
  }

  @PutMapping("/{id}/resolve")
  public ResponseEntity<ApiResponse<AlertResponse>> resolveAlert(@PathVariable Long id) {
    log.info("Resolving alert with ID: {}", id);
    Alert resolved = alertService.resolveAlert(id);
    return ResponseEntity.ok(ApiResponse.success(alertMapper.toResponse(resolved)));
  }

  @GetMapping("/device/{deviceId}")
  public ResponseEntity<ApiResponse<Page<AlertResponse>>> getAlertsForDevice(
      @PathVariable Long deviceId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Boolean resolved) {
    log.info("Fetching alerts for device: {}, page: {}, size: {}, resolved: {}", deviceId, page, size, resolved);
    Pageable pageable = PageRequest.of(page, size);
    Page<Alert> alerts = alertService.getAlertsForDevice(deviceId, resolved, pageable);
    Page<AlertResponse> dtos = alerts.map(alertMapper::toResponse);
    return ResponseEntity.ok(ApiResponse.success(dtos));
  }

  @GetMapping("/unresolved")
  public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnresolvedAlerts() {
    log.info("Fetching unresolved alerts");
    List<AlertResponse> dtos = alertService.getUnresolvedAlerts()
        .stream()
        .map(alertMapper::toResponse)
        .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(dtos));
  }
}