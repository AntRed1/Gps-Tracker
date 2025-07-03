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

package com.arojas.gpstracker.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StreamUtils;

import com.arojas.gpstracker.entities.Alert;
import com.arojas.gpstracker.entities.Device;
import com.arojas.gpstracker.entities.User;
import com.arojas.gpstracker.repositories.AlertRepository;
import com.arojas.gpstracker.repositories.DeviceRepository;
import com.arojas.gpstracker.repositories.GpsLocationRepository;
import com.arojas.gpstracker.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes the database with tables, stored procedures, and sample data.
 *
 * @author neta1
 */
@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

  private final JdbcTemplate jdbcTemplate;
  private final UserRepository userRepository;
  private final DeviceRepository deviceRepository;
  private final GpsLocationRepository gpsLocationRepository;
  private final AlertRepository alertRepository;
  private final PasswordEncoder passwordEncoder;
  private final ResourceLoader resourceLoader;

  @Bean
  CommandLineRunner initDatabase() {
    return args -> {
      log.info("Initializing database with tables, stored procedures, and sample data");
      try {
        createTables();
        createLocationStoredProcedure();
        createAlertStoredProcedure();
        createEventStoredProcedure();
        initializeSampleData();
      } catch (Exception e) {
        log.error("Error initializing database: {}", e.getMessage(), e);
        throw e;
      }
    };
  }

  private void createTables() {
    try {
      // Create users table
      jdbcTemplate.execute("""
          CREATE TABLE IF NOT EXISTS users (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              email VARCHAR(255) NOT NULL UNIQUE,
              password VARCHAR(255) NOT NULL,
              full_name VARCHAR(255),
              created_at DATETIME NOT NULL
          )
          """);
      log.info("Created or verified table: users");

      // Create devices table
      jdbcTemplate.execute("""
          CREATE TABLE IF NOT EXISTS devices (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              device_identifier VARCHAR(255) NOT NULL,
              alias VARCHAR(255),
              activated BOOLEAN NOT NULL,
              user_id BIGINT,
              FOREIGN KEY (user_id) REFERENCES users(id)
          )
          """);
      log.info("Created or verified table: devices");

      // Create gps_locations table
      jdbcTemplate.execute("""
          CREATE TABLE IF NOT EXISTS gps_locations (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              device_id BIGINT NOT NULL,
              latitude DOUBLE NOT NULL,
              longitude DOUBLE NOT NULL,
              timestamp DATETIME NOT NULL,
              FOREIGN KEY (device_id) REFERENCES devices(id)
          )
          """);
      log.info("Created or verified table: gps_locations");

      // Create alerts table
      jdbcTemplate.execute("""
          CREATE TABLE IF NOT EXISTS alerts (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              device_id BIGINT NOT NULL,
              message VARCHAR(255) NOT NULL,
              type VARCHAR(50) NOT NULL,
              resolved BOOLEAN NOT NULL,
              created_at DATETIME NOT NULL,
              FOREIGN KEY (device_id) REFERENCES devices(id)
          )
          """);
      log.info("Created or verified table: alerts");

      // Create gps_events table
      jdbcTemplate.execute("""
          CREATE TABLE IF NOT EXISTS gps_events (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              device_id BIGINT NOT NULL,
              event_type VARCHAR(50) NOT NULL,
              timestamp DATETIME NOT NULL,
              FOREIGN KEY (device_id) REFERENCES devices(id)
          )
          """);
      log.info("Created or verified table: gps_events");
    } catch (DataAccessException e) {
      log.error("Error creating database tables: {}", e.getMessage(), e);
      throw e;
    }
  }

  private void createLocationStoredProcedure() {
    try {
      Resource resource = resourceLoader.getResource("classpath:sql/insert_location.sql");
      String createProcedure = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_location");
      jdbcTemplate.execute(createProcedure);
      log.info("Created stored procedure: insert_location");
    } catch (IOException | DataAccessException e) {
      log.error("Error creating stored procedure 'insert_location': {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create insert_location procedure", e);
    }
  }

  private void createAlertStoredProcedure() {
    try {
      Resource resource = resourceLoader.getResource("classpath:sql/insert_alert.sql");
      String createProcedure = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_alert");
      jdbcTemplate.execute(createProcedure);
      log.info("Created stored procedure: insert_alert");
    } catch (IOException | DataAccessException e) {
      log.error("Error creating stored procedure 'insert_alert': {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create insert_alert procedure", e);
    }
  }

  private void createEventStoredProcedure() {
    try {
      Resource resource = resourceLoader.getResource("classpath:sql/insert_event.sql");
      String createProcedure = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_event");
      jdbcTemplate.execute(createProcedure);
      log.info("Created stored procedure: insert_event");
    } catch (IOException | DataAccessException e) {
      log.error("Error creating stored procedure 'insert_event': {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create insert_event procedure", e);
    }
  }

  private void initializeSampleData() {
    if (userRepository.count() == 0) {
      log.info("No users found, initializing sample data");
      try {
        // Create user
        User user = User.builder()
            .email("test@example.com")
            .password(passwordEncoder.encode("password123"))
            .fullName("Test User")
            .createdAt(LocalDateTime.now())
            .build();
        userRepository.save(user);
        log.info("Created sample user: email={}", user.getEmail());

        // Create device
        Device device = Device.builder()
            .deviceIdentifier("DEVICE_001")
            .alias("Test Device")
            .activated(true)
            .user(user)
            .build();
        Device savedDevice = deviceRepository.save(device);
        if (savedDevice.getId() == null) {
          throw new RuntimeException("Failed to save device: " + device.getAlias());
        }
        log.info("Created sample device: id={}, alias={}", savedDevice.getId(), savedDevice.getAlias());

        // Insert locations using stored procedure
        jdbcTemplate.update("CALL insert_location(?, ?, ?, ?)", savedDevice.getId(), 40.7128, -74.0060,
            LocalDateTime.now());
        jdbcTemplate.update("CALL insert_location(?, ?, ?, ?)", savedDevice.getId(), 40.7130, -74.0050,
            LocalDateTime.now());
        log.info("Created sample locations for device: id={}", savedDevice.getId());

        // Create alert
        Alert alert = Alert.builder()
            .message("Location out of bounds")
            .type(Alert.AlertType.LOCATION_OUT_OF_BOUNDS)
            .resolved(false)
            .createdAt(LocalDateTime.now())
            .device(savedDevice)
            .build();
        alertRepository.save(alert);
        log.info("Created sample alert for device: id={}", savedDevice.getId());
      } catch (RuntimeException e) {
        log.error("Error initializing sample data: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to initialize sample data", e);
      }
    } else {
      log.info("Users already exist, skipping sample data initialization");
    }
  }
}