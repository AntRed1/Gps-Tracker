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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arojas.gpstracker.entities.Alert;
import com.arojas.gpstracker.entities.Alert.AlertType;

/**
 *
 * @author neta1
 *         * Repositorio para gestión de alertas.
 * 
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
  Page<Alert> findByDeviceId(Long deviceId, Pageable pageable);

  Page<Alert> findByDeviceIdAndResolved(Long deviceId, boolean resolved, Pageable pageable);

  List<Alert> findByDeviceIdAndType(Long deviceId, AlertType type);

  List<Alert> findByResolvedFalseOrderByCreatedAtDesc();

  List<Alert> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

  Optional<Alert> findByIdAndDeviceId(Long id, Long deviceId);

  @Modifying
  @Query("UPDATE Alert a SET a.resolved = true, a.resolvedAt = :resolvedAt WHERE a.id = :alertId")
  void resolveAlert(@Param("alertId") Long alertId, @Param("resolvedAt") LocalDateTime resolvedAt);

  @Query(value = "CALL insert_alert(:p_device_id, :p_message, :p_type, NOW())", nativeQuery = true)
  void insertAlert(Long p_device_id, String p_message, String p_type);

  List<Alert> findByDeviceIdAndResolvedFalse(Long deviceId);

  @Modifying
  @Query("UPDATE Alert a SET a.resolved = true WHERE a.id = :alertId")
  void resolveAlert(Long alertId);

}