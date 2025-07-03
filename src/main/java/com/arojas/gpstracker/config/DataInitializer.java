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
 * Inicializa la base de datos con tablas, procedimientos almacenados y datos de
 * muestra.
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

  @Bean
  CommandLineRunner initDatabase() {
    return args -> {
      log.info("Inicializando la base de datos con tablas, procedimientos almacenados y datos de muestra");
      try {
        createTables();
        createLocationStoredProcedure();
        createAlertStoredProcedure();
        createEventStoredProcedure();
        initializeSampleData();
      } catch (Exception e) {
        log.error("Error al inicializar la base de datos: {}", e.getMessage(), e);
        throw e;
      }
    };
  }

  private void createTables() {
    try {
      // Crear tabla users
      jdbcTemplate.execute("""
          CREATE TABLE IF NOT EXISTS users (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              email VARCHAR(255) NOT NULL UNIQUE,
              password VARCHAR(255) NOT NULL,
              full_name VARCHAR(255),
              created_at DATETIME NOT NULL
          )
          """);
      log.info("Creada o verificada la tabla: users");

      // Crear tabla devices
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
      log.info("Creada o verificada la tabla: devices");

      // Crear tabla gps_locations
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
      log.info("Creada o verificada la tabla: gps_locations");

      // Crear tabla alerts
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
      log.info("Creada o verificada la tabla: alerts");

      // Crear tabla gps_events
      jdbcTemplate.execute("""
          CREATE TABLE IF NOT EXISTS gps_events (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              device_id BIGINT NOT NULL,
              event_type VARCHAR(50) NOT NULL,
              timestamp DATETIME NOT NULL,
              FOREIGN KEY (device_id) REFERENCES devices(id)
          )
          """);
      log.info("Creada o verificada la tabla: gps_events");
    } catch (DataAccessException e) {
      log.error("Error al crear las tablas de la base de datos: {}", e.getMessage(), e);
      throw e;
    }
  }

  private void createLocationStoredProcedure() {
    try {
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_location");
      String createProcedure = """
          DELIMITER //
          CREATE PROCEDURE insert_location (
              IN p_device_id BIGINT,
              IN p_latitude DOUBLE,
              IN p_longitude DOUBLE,
              IN p_timestamp DATETIME
          )
          BEGIN
              DECLARE device_exists INT;

              -- Verificar si el device_id existe
              SELECT COUNT(*) INTO device_exists FROM devices WHERE id = p_device_id;
              IF device_exists = 0 THEN
                  SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'ID de dispositivo inválido: El dispositivo no existe';
              END IF;

              -- Validar parámetros
              IF p_latitude IS NULL OR p_longitude IS NULL THEN
                  SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'La latitud y la longitud no pueden ser nulas';
              END IF;

              -- Usar NOW() si p_timestamp es NULL
              SET p_timestamp = COALESCE(p_timestamp, NOW());

              INSERT INTO gps_locations (device_id, latitude, longitude, timestamp)
              VALUES (p_device_id, p_latitude, p_longitude, p_timestamp);
          END //
          DELIMITER ;
          """;
      jdbcTemplate.execute(createProcedure);
      log.info("Creado el procedimiento almacenado: insert_location");
    } catch (DataAccessException e) {
      log.error("Error al crear el procedimiento almacenado 'insert_location': {}", e.getMessage(), e);
      throw new RuntimeException("No se pudo crear el procedimiento insert_location", e);
    }
  }

  private void createAlertStoredProcedure() {
    try {
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_alert");
      String createProcedure = """
          DELIMITER //
          CREATE PROCEDURE insert_alert (
              IN p_device_id BIGINT,
              IN p_message VARCHAR(255),
              IN p_type VARCHAR(50),
              IN p_timestamp DATETIME
          )
          BEGIN
              DECLARE device_exists INT;

              -- Verificar si el device_id existe
              SELECT COUNT(*) INTO device_exists FROM devices WHERE id = p_device_id;
              IF device_exists = 0 THEN
                  SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'ID de dispositivo inválido: El dispositivo no existe';
              END IF;

              -- Validar parámetros
              IF p_message IS NULL OR p_type IS NULL THEN
                  SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'El mensaje y el tipo no pueden ser nulos';
              END IF;

              -- Usar NOW() si p_timestamp es NULL
              SET p_timestamp = COALESCE(p_timestamp, NOW());

              INSERT INTO alerts (device_id, message, type, resolved, created_at)
              VALUES (p_device_id, p_message, p_type, FALSE, p_timestamp);
          END //
          DELIMITER ;
          """;
      jdbcTemplate.execute(createProcedure);
      log.info("Creado el procedimiento almacenado: insert_alert");
    } catch (DataAccessException e) {
      log.error("Error al crear el procedimiento almacenado 'insert_alert': {}", e.getMessage(), e);
      throw new RuntimeException("No se pudo crear el procedimiento insert_alert", e);
    }
  }

  private void createEventStoredProcedure() {
    try {
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS insert_event");
      String createProcedure = """
          DELIMITER //
          CREATE PROCEDURE insert_event (
              IN p_device_id BIGINT,
              IN p_event_type VARCHAR(50),
              IN p_timestamp DATETIME
          )
          BEGIN
              DECLARE device_exists INT;

              -- Verificar si el device_id existe
              SELECT COUNT(*) INTO device_exists FROM devices WHERE id = p_device_id;
              IF device_exists = 0 THEN
                  SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'ID de dispositivo inválido: El dispositivo no existe';
              END IF;

              -- Validar parámetros
              IF p_event_type IS NULL THEN
                  SIGNAL SQLSTATE '45000'
                  SET MESSAGE_TEXT = 'El tipo de evento no puede ser nulo';
              END IF;

              -- Usar NOW() si p_timestamp es NULL
              SET p_timestamp = COALESCE(p_timestamp, NOW());

              INSERT INTO gps_events (device_id, event_type, timestamp)
              VALUES (p_device_id, p_event_type, p_timestamp);
          END //
          DELIMITER ;
          """;
      jdbcTemplate.execute(createProcedure);
      log.info("Creado el procedimiento almacenado: insert_event");
    } catch (DataAccessException e) {
      log.error("Error al crear el procedimiento almacenado 'insert_event': {}", e.getMessage(), e);
      throw new RuntimeException("No se pudo crear el procedimiento insert_event", e);
    }
  }

  private void initializeSampleData() {
    if (userRepository.count() == 0) {
      log.info("No se encontraron usuarios, inicializando datos de muestra");
      try {
        // Crear usuario
        User user = User.builder()
            .email("test@example.com")
            .password(passwordEncoder.encode("password123"))
            .fullName("Test User")
            .createdAt(LocalDateTime.now())
            .build();
        userRepository.save(user);
        log.info("Creado usuario de muestra: email={}", user.getEmail());

        // Crear dispositivo
        Device device = Device.builder()
            .deviceIdentifier("DEVICE_001")
            .alias("Test Device")
            .activated(true)
            .user(user)
            .build();
        Device savedDevice = deviceRepository.save(device);
        if (savedDevice.getId() == null) {
          throw new RuntimeException("No se pudo guardar el dispositivo: " + device.getAlias());
        }
        log.info("Creado dispositivo de muestra: id={}, alias={}", savedDevice.getId(), savedDevice.getAlias());

        // Insertar ubicaciones usando el procedimiento almacenado
        jdbcTemplate.update("CALL insert_location(?, ?, ?, ?)", savedDevice.getId(), 40.7128, -74.0060,
            LocalDateTime.now());
        jdbcTemplate.update("CALL insert_location(?, ?, ?, ?)", savedDevice.getId(), 40.7130, -74.0050,
            LocalDateTime.now());
        log.info("Creadas ubicaciones de muestra para el dispositivo: id={}", savedDevice.getId());

        // Crear alerta
        Alert alert = Alert.builder()
            .message("Ubicación fuera de los límites")
            .type(Alert.AlertType.LOCATION_OUT_OF_BOUNDS)
            .resolved(false)
            .createdAt(LocalDateTime.now())
            .device(savedDevice)
            .build();
        alertRepository.save(alert);
        log.info("Creada alerta de muestra para el dispositivo: id={}", savedDevice.getId());
      } catch (Exception e) {
        log.error("Error al inicializar los datos de muestra: {}", e.getMessage(), e);
        throw new RuntimeException("No se pudieron inicializar los datos de muestra", e);
      }
    } else {
      log.info("Ya existen usuarios, omitiendo la inicialización de datos de muestra");
    }
  }
}