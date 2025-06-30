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

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arojas.gpstracker.entities.GpsEvent;
import com.arojas.gpstracker.entities.GpsEvent.EventType;

/**
 *
 * @author neta1
 *         * Repositorio para gestión de eventos GPS.
 * 
 */
@Repository
public interface GpsEventRepository extends JpaRepository<GpsEvent, Long> {

  /**
   * Obtiene los últimos 10 eventos para un dispositivo ordenados por fecha
   * descendente.
   *
   * @param deviceId ID del dispositivo
   * @return Lista de eventos
   */
  List<GpsEvent> findTop10ByDeviceIdOrderByTimestampDesc(Long deviceId);

  /**
   * Obtiene eventos filtrando por dispositivo y tipo de evento.
   *
   * @param deviceId  ID del dispositivo
   * @param eventType Tipo de evento (ON, OFF, etc)
   * @return Lista de eventos filtrados
   */
  List<GpsEvent> findByDeviceIdAndEventType(Long deviceId, EventType eventType);

  /**
   * (Opcional) Método para obtener eventos entre fechas - útil para auditorías.
   */
  List<GpsEvent> findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(Long deviceId,
      java.time.LocalDateTime start, java.time.LocalDateTime end);

}
