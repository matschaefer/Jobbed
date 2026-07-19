package com.jobbed.security;

import com.jobbed.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Liefert 403 im einheitlichen Fehlerformat bei unzureichender Berechtigung. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ApiErrorResponder responder;

    public RestAccessDeniedHandler(ApiErrorResponder responder) {
        this.responder = responder;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        responder.write(request, response, ErrorCode.ACCESS_DENIED, "Zugriff verweigert.");
    }
}
