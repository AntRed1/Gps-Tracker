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

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

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
 * Initializes the database with stored procedures and sample data.
 *
 * @author neta1
 */
@Configuration
@Profile("!test") // No ejecutar en el perfil de pruebas
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

  private final JdbcTemplate jdbcTemplate;
  private final UserRepository userRepository;
  private final DeviceRepository deviceRepository;
  private final GpsLocationRepository gpsLocationRepository;
  private final AlertRepository alertRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  CommandLineRunner initDatabase() {
    return args -> {
      log.info("Initializing database with stored procedures and sample data");
      try {
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

  private void createLocationStoredProcedure() {
    try {
      // Eliminar el procedimiento si existe
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_location");

      // Crear el procedimiento almacenado
      String createProcedure = """
          CREATE PROCEDURE insert_location (
              IN p_device_id BIGINT,
              IN p_latitude DOUBLE,
              IN p_longitude DOUBLE
          )
          BEGIN
              INSERT INTO gps_locations (device_id, latitude, longitude, timestamp)
              VALUES (p_device_id, p_latitude, p_longitude, NOW());
          END
          """;
      jdbcTemplate.execute(createProcedure);
      log.info("Created stored procedure: insert_location");
    } catch (DataAccessException e) {
      log.error("Error creating stored procedure 'insert_location': {}", e.getMessage(), e);
      throw e;
    }
  }

  private void createAlertStoredProcedure() {
    try {
      // Eliminar el procedimiento si existe
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_alert");

      // Crear el procedimiento almacenado
      String createProcedure = """
          CREATE PROCEDURE insert_alert (
              IN p_device_id BIGINT,
              IN p_message VARCHAR(255),
              IN p_type VARCHAR(50)
          )
          BEGIN
              INSERT INTO alerts (device_id, message, type, resolved, created_at)
              VALUES (p_device_id, p_message, p_type, FALSE, NOW());
          END
          """;
      jdbcTemplate.execute(createProcedure);
      log.info("Created stored procedure: insert_alert");
    } catch (DataAccessException e) {
      log.error("Error creating stored procedure 'insert_alert': {}", e.getMessage(), e);
      throw e;
    }
  }

  private void createEventStoredProcedure() {
    try {
      // Eliminar el procedimiento si existe
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_event");

      // Crear el procedimiento almacenado
      String createProcedure = """
          CREATE PROCEDURE insert_event (
              IN p_device_id BIGINT,
              IN p_event_type VARCHAR(50)
          )
          BEGIN
              INSERT INTO gps_events (device_id, event_type, timestamp)
              VALUES (p_device_id, p_event_type, NOW());
          END
          """;
      jdbcTemplate.execute(createProcedure);
      log.info("Created stored procedure: insert_event");
    } catch (DataAccessException e) {
      log.error("Error creating stored procedure 'insert_event': {}", e.getMessage(), e);
      throw e;
    }
  }

  private void initializeSampleData() {
    if (userRepository.count() == 0) {
      log.info("No users found, initializing sample data");
      User user = User.builder()
          .email("test@example.com")
          .password(passwordEncoder.encode("password123"))
          .fullName("Test User")
          .createdAt(LocalDateTime.now())
          .build();
      userRepository.save(user);
      log.info("Created sample user: {}", user.getEmail());

      Device device = Device.builder()
          .deviceIdentifier("DEVICE_001")
          .alias("Test Device")
          .activated(true)
          .user(user)
          .build();
      deviceRepository.save(device);
      log.info("Created sample device: {}", device.getAlias());

      try {
        gpsLocationRepository.insertLocation(device.getId(), 40.7128, -74.0060);
        gpsLocationRepository.insertLocation(device.getId(), 40.7130, -74.0050);
        log.info("Created sample locations for device: {}", device.getId());
      } catch (Exception e) {
        log.error("Error inserting sample locations: {}", e.getMessage(), e);
      }

      Alert alert = Alert.builder()
          .message("Location out of bounds")
          .type(Alert.AlertType.LOCATION_OUT_OF_BOUNDS)
          .resolved(false)
          .createdAt(LocalDateTime.now())
          .device(device)
          .build();
      alertRepository.save(alert);
      log.info("Created sample alert for device: {}", device.getId());
    } else {
      log.info("Users already exist, skipping sample data initialization");
    }
  }
}