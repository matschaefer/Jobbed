package com.jobbed.security;

import com.jobbed.common.error.ErrorCode;
import com.jobbed.user.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class DemoModeWriteProtectionFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final Set<String> AUTH_WRITE_ALLOWLIST = Set.of(
            "/api/v1/auth/demo",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout"
    );

    private final ApiErrorResponder errorResponder;

    public DemoModeWriteProtectionFilter(ApiErrorResponder errorResponder) {
        this.errorResponder = errorResponder;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (isDemoPrincipal() && isWriteRequest(request) && !isAllowlisted(request)) {
            errorResponder.write(request, response, ErrorCode.ACCESS_DENIED,
                    "Der Demo-Modus ist schreibgeschuetzt. Aenderungen sind deaktiviert.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isDemoPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.getPrincipal() instanceof AuthenticatedUser user
                && user.role() == Role.DEMO;
    }

    private boolean isWriteRequest(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod());
    }

    private boolean isAllowlisted(HttpServletRequest request) {
        return AUTH_WRITE_ALLOWLIST.contains(request.getRequestURI());
    }
}
