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

package com.arojas.gpstracker.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arojas.gpstracker.entities.GpsLocation;

/**
 *
 * @author neta1
 *         * Repositorio para gestión de ubicaciones GPS.
 * 
 */
@Repository
public interface GpsLocationRepository extends JpaRepository<GpsLocation, Long> {

  /**
   * Inserts a new location using a stored procedure.
   *
   * @param deviceId  ID of the device
   * @param latitude  Latitude
   * @param longitude Longitude
   */
  @Procedure(name = "insert_location")
  void insertLocation(
      @Param("p_device_id") Long deviceId,
      @Param("p_latitude") double latitude,
      @Param("p_longitude") double longitude);

  /**
   * Retrieves the latest location for a device, ordered by timestamp descending.
   *
   * @param deviceId ID of the device
   * @return Optional containing the latest location, or empty if none exists
   */
  Optional<GpsLocation> findTopByDeviceIdOrderByTimestampDesc(Long deviceId);

  /**
   * Retrieves paginated locations for a device, ordered by timestamp descending.
   *
   * @param deviceId ID of the device
   * @param pageable Pagination information
   * @return Paginated list of locations
   */
  @Query("SELECT l FROM GpsLocation l WHERE l.device.id = :deviceId ORDER BY l.timestamp DESC")
  Page<GpsLocation> findAllByDeviceIdOrderByTimestampDesc(@Param("deviceId") Long deviceId, Pageable pageable);

  /**
   * Retrieves locations for a device within a time range, ordered by timestamp
   * descending.
   *
   * @param deviceId ID of the device
   * @param start    Start of the time range
   * @param end      End of the time range
   * @return List of locations within the specified time range
   */
  @Query("SELECT l FROM GpsLocation l WHERE l.device.id = :deviceId AND l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp DESC")
  List<GpsLocation> findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
      @Param("deviceId") Long deviceId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}