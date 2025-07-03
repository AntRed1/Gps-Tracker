# 📍 GPS Tracker System

```bash
Un sistema GPS moderno que permite rastrear vehículos en tiempo real utilizando hardware embebido (ESP32 + SIM800L + GPS), y una arquitectura backend robusta con notificaciones vía WebSocket, autenticación JWT y almacenamiento en base de datos. Incluye una app móvil Flutter para consulta de eventos, alertas y ubicaciones.
```

---

## 🗂 Estructura del Proyecto

```bash
src/main/java/com/arojas/gpstracker/
│
├── config/
│   └── WebSocketConfig.java
│
├── controllers/
│   ├── AlertController.java
│   ├── AuthController.java
│   ├── DeviceController.java
│   ├── GpsEventController.java
│   ├── GpsLocationController.java
│   └── UserController.java
│
├── dto/
│   ├── AlertDTO.java
│   ├── AlertRequest.java
│   ├── AlertResponse.java
│   ├── AuthResponse.java
│   ├── DeviceRegisterRequest.java
│   ├── DeviceResponse.java
│   ├── GpsEventDTO.java
│   ├── GpsLocationDTO.java
│   ├── LoginRequest.java
│   ├── LogoutRequest.java
│   ├── PasswordChangeRequest.java
│   ├── RefreshTokenRequest.java
│   ├── RegisterRequest.java
│   ├── TokenResponse.java
│   ├── UserResponse.java
│   └── UserUpdateRequest.java
│
├── entities/
│   ├── Alert.java
│   ├── Device.java
│   ├── GpsEvent.java
│   ├── GpsLocation.java
│   └── User.java
│
├── exception/
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidCredentialsException.java
│   ├── NotFoundException.java
│   ├── UnauthorizedException.java
│   └── UserNotFoundException.java
│
├── mappers/
│   └── AlertMapper.java
│
├── repositories/
│   ├── AlertRepository.java
│   ├── DeviceRepository.java
│   ├── GpsEventRepository.java
│   ├── GpsLocationRepository.java
│   └── UserRepository.java
│
├── security/
│   ├── JwtFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
│
└── services/
    ├── AlertService.java
    ├── AuthService.java
    ├── CustomUserDetailsService.java
    ├── DeviceService.java
    ├── GpsEventService.java
    ├── GpsLocationService.java
    ├── RedisService.java
    └── UserService.java
```

---

## 🧠 Arquitectura del Sistema

```bash
+-------------------+          GSM/GPRS        +---------------------+
|  Vehículo con GPS | -----------------------> |     Backend/API     |
| (ESP32 + GPS +    |                         | (Cloud, VPS o local) |
|  SIM800L + Sensor)|                         +---------------------+
|                   |                         ^           |
| - Detecta ON/OFF  |                         |           |
| - Captura lat/lng |                         |     REST API
+-------------------+                         |           v
                                              |    +---------------+
                                    HTTPS      --> |     Base de     |
                                                    |     Datos       |
                                                    +---------------+
                                                           |
                                                           |
                                                   +---------------+
                                                   |     App móvil  |
                                                   |  (Flutter/React|
                                                   |   Native/iOS)  |
                                                   +---------------+
```

---

## 🛠 Tecnologías

```bash
- **Backend:** Spring Boot 3.5.3 + JWT + WebSocket (STOMP)
- **Base de datos:** MySQL + Redis
- **Hardware:** ESP32, SIM800L, GPS Module
- **Frontend móvil:** Flutter
- **Mensajería:** STOMP over WebSocket
- **Seguridad:** Spring Security, BCrypt, Tokens de Refresh con Redis
```

---

## 📚 API Reference

#### Obtener todos los ítems

```http
GET /api/items
```

| Parámeter | Tipo     | Descripción                     |
| :-------- | :------- | :------------------------------ |
| `api_key` | `string` | **Requerido**. Tu API key       |

#### Obtener ítem por ID

```http
GET /api/items/${id}
```

| Parámeter | Tipo     | Descripción                     |
| :-------- | :------- | :------------------------------ |
| `id`      | `string` | **Requerido**. ID del ítem      |

---

## 👤 Authors

- [@octokatherine](https://www.github.com/octokatherine)

---

## 🏷️ Badges

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)](https://opensource.org/licenses/)
[![AGPL License](https://img.shields.io/badge/license-AGPL-blue.svg)](http://www.gnu.org/licenses/agpl-3.0)

---

## 📄 Documentación

[Ver documentación](https://linktodocumentation)

---

## 📜 Licencia

Este proyecto está licenciado bajo la [MIT License](https://choosealicense.com/licenses/mit/).

---

## 🏢 Usado por

Este proyecto es usado por las siguientes compañías:

- Company 1
- Company 2
