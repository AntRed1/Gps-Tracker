/*
 * The MIT License
 *
 * Copyright 2025 arojas.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.arojas.gpstracker.entities.HttpLog;
import com.arojas.gpstracker.repositories.HttpLogRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author arojas
 */
@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(HttpLoggingFilter.class);

	@Autowired
	private HttpLogRepository httpLogRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// Envolver la solicitud y la respuesta para capturar el cuerpo
		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

		long startTime = System.currentTimeMillis();

		try {
			// Continuar con el procesamiento de la solicitud
			filterChain.doFilter(wrappedRequest, wrappedResponse);
		} finally {
			long responseTimeMs = System.currentTimeMillis() - startTime;

			// Construir el objeto HttpLog
			HttpLog httpLog = new HttpLog();
			httpLog.setMethod(wrappedRequest.getMethod());
			httpLog.setUrl(wrappedRequest.getRequestURL().toString());
			httpLog.setQueryParams(wrappedRequest.getQueryString());
			httpLog.setStatusCode(wrappedResponse.getStatus());
			httpLog.setResponseTimeMs(responseTimeMs);
			httpLog.setClientIp(wrappedRequest.getRemoteAddr());
			httpLog.setUserAgent(wrappedRequest.getHeader("User-Agent"));

			// Capturar el cuerpo de la solicitud
			String requestBody = getRequestBody(wrappedRequest);
			httpLog.setRequestBody(truncate(requestBody, 65535)); // Limitar a tamaño de TEXT en MySQL

			// Capturar el cuerpo de la respuesta
			String responseBody = getResponseBody(wrappedResponse);
			httpLog.setResponseBody(truncate(responseBody, 65535));

			// Obtener el usuario autenticado (si existe)
			String userEmail = getUserEmail();
			httpLog.setUserEmail(userEmail);

			// Guardar el log en la base de datos
			try {
				httpLogRepository.save(httpLog);
				logger.info(
						"HTTP Log saved: method={}, url={}, status={}, responseTime={}ms, userEmail={}, requestBody={}, responseBody={}",
						httpLog.getMethod(), httpLog.getUrl(), httpLog.getStatusCode(), httpLog.getResponseTimeMs(),
						httpLog.getUserEmail(), httpLog.getRequestBody(), truncate(httpLog.getResponseBody(), 100));
			} catch (Exception e) {
				logger.error("Failed to save HTTP log: {}", e.getMessage(), e);
			}

			// Copiar el contenido de la respuesta al response original
			wrappedResponse.copyBodyToResponse();
		}
	}

	private String getRequestBody(ContentCachingRequestWrapper request) {
		byte[] content = request.getContentAsByteArray();
		if (content.length > 0) {
			return new String(content, StandardCharsets.UTF_8);
		}
		return null;
	}

	private String getResponseBody(ContentCachingResponseWrapper response) {
		byte[] content = response.getContentAsByteArray();
		if (content.length > 0) {
			return new String(content, StandardCharsets.UTF_8);
		}
		return null;
	}

	private String getUserEmail() {
		try {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof UserDetails) {
				return ((UserDetails) principal).getUsername(); // Asumiendo que el username es el email
			}
		} catch (Exception e) {
			logger.warn("Could not retrieve user email from SecurityContext: {}", e.getMessage());
		}
		return null;
	}

	private String truncate(String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			return value.substring(0, maxLength);
		}
		return value;
	}
}
