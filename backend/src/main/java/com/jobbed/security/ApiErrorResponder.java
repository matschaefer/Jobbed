package com.jobbed.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobbed.common.error.ApiError;
import com.jobbed.common.error.ErrorCode;
import com.jobbed.common.logging.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Schreibt das einheitliche {@link ApiError}-Format aus den Security-Handlern
 * (EntryPoint / AccessDeniedHandler), da diese außerhalb des DispatcherServlet
 * laufen und nicht vom {@code @RestControllerAdvice} erfasst werden.
 */
@Component
public class ApiErrorResponder {

    private final ObjectMapper objectMapper;

    public ApiErrorResponder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      ErrorCode code, String message) throws IOException {
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
        ApiError body = ApiError.of(code, message, request.getRequestURI(), correlationId);
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
