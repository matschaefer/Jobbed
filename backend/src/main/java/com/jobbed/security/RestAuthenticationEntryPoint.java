package com.jobbed.security;

import com.jobbed.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Liefert 401 im einheitlichen Fehlerformat bei fehlender/ungültiger Authentifizierung. */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ApiErrorResponder responder;

    public RestAuthenticationEntryPoint(ApiErrorResponder responder) {
        this.responder = responder;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        responder.write(request, response, ErrorCode.AUTHENTICATION_REQUIRED,
                "Authentifizierung erforderlich.");
    }
}
