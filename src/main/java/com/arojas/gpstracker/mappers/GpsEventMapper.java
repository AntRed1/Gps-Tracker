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

package com.arojas.gpstracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.arojas.gpstracker.dto.GpsEventDTO;
import com.arojas.gpstracker.dto.GpsEventRequest;
import com.arojas.gpstracker.entities.GpsEvent;

/**
 * @author neta1
 */
@Mapper(componentModel = "spring")
public interface GpsEventMapper {
    @Mapping(source = "device.id", target = "deviceId") // Mapear device.id a deviceId
    GpsEventDTO toDto(GpsEvent event);

    @Mapping(target = "id", ignore = true) // Ignorar porque es generado automáticamente
    @Mapping(target = "timestamp", ignore = true) // Ignorar porque tiene valor por defecto
    @Mapping(target = "device", ignore = true) // Ignorar porque se gestiona en otra parte
    GpsEvent toEntity(GpsEventRequest request);
}
