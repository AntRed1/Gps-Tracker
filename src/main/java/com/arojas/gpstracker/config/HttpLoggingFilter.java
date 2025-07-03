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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
		long startTime = System.currentTimeMillis();

		try {
			filterChain.doFilter(request, response);
		} finally {
			long responseTimeMs = System.currentTimeMillis() - startTime;

			HttpLog httpLog = new HttpLog();
			httpLog.setMethod(request.getMethod());
			httpLog.setUrl(request.getRequestURL().toString());
			httpLog.setQueryParams(request.getQueryString());
			httpLog.setStatusCode(response.getStatus());
			httpLog.setResponseTimeMs(responseTimeMs);
			httpLog.setClientIp(request.getRemoteAddr());
			httpLog.setUserAgent(request.getHeader("User-Agent"));

			try {
				httpLogRepository.save(httpLog);
				logger.debug("HTTP Log saved: method={}, url={}, status={}, responseTime={}ms",
						httpLog.getMethod(), httpLog.getUrl(), httpLog.getStatusCode(), httpLog.getResponseTimeMs());
			} catch (Exception e) {
				logger.error("Failed to save HTTP log: {}", e.getMessage());
			}
		}
	}
}
