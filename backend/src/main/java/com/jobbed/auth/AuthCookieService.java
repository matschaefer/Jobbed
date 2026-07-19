package com.jobbed.auth;

import com.jobbed.security.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Baut das HttpOnly-Refresh-Cookie (Secure, SameSite=Strict, enger Path) und
 * liest es aus eingehenden Requests.
 */
@Service
public class AuthCookieService {

    private final AuthProperties authProperties;

    public AuthCookieService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public String buildSetCookieHeader(String rawRefreshToken) {
        AuthProperties.Cookie cfg = authProperties.cookie();
        return ResponseCookie.from(cfg.name(), rawRefreshToken)
                .httpOnly(true)
                .secure(cfg.secure())
                .sameSite(cfg.sameSite())
                .path(cfg.path())
                .maxAge(Duration.ofSeconds(authProperties.refreshTokenTtlSeconds()))
                .build()
                .toString();
    }

    public String buildClearCookieHeader() {
        AuthProperties.Cookie cfg = authProperties.cookie();
        return ResponseCookie.from(cfg.name(), "")
                .httpOnly(true)
                .secure(cfg.secure())
                .sameSite(cfg.sameSite())
                .path(cfg.path())
                .maxAge(0)
                .build()
                .toString();
    }

    public String readRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        String cookieName = authProperties.cookie().name();
        for (var cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
