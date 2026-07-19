package com.jobbed.auth;

import com.jobbed.auth.dto.AuthResponse;
import com.jobbed.auth.dto.ForgotPasswordRequest;
import com.jobbed.auth.dto.LoginRequest;
import com.jobbed.auth.dto.MessageResponse;
import com.jobbed.auth.dto.RegisterRequest;
import com.jobbed.auth.dto.ResetPasswordRequest;
import com.jobbed.auth.dto.UserResponse;
import com.jobbed.auth.dto.VerifyEmailRequest;
import com.jobbed.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentifizierungs-Endpunkte. Access-Tokens werden im Body zurückgegeben,
 * Refresh-Tokens ausschließlich als HttpOnly-Cookie gesetzt.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Registrierung, Login, Token-Verwaltung")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService cookieService;

    public AuthController(AuthService authService, AuthCookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrierung", description = "Legt einen Nutzer an und sendet eine Verifikations-E-Mail.")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Liefert ein Access-Token und setzt das Refresh-Cookie.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        AuthResult result = authService.login(request, userAgent(httpRequest), clientIp(httpRequest));
        setRefreshCookie(httpResponse, result.rawRefreshToken());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token erneuern", description = "Rotiert das Refresh-Token und liefert ein neues Access-Token.")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        String rawRefresh = cookieService.readRefreshToken(httpRequest);
        AuthResult result = authService.refresh(rawRefresh, userAgent(httpRequest), clientIp(httpRequest));
        setRefreshCookie(httpResponse, result.rawRefreshToken());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Widerruft das Refresh-Token und löscht das Cookie.")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(cookieService.readRefreshToken(httpRequest));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookieService.buildClearCookieHeader());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    @Operation(summary = "E-Mail bestätigen")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(new MessageResponse("E-Mail-Adresse wurde bestätigt."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Passwort vergessen", description = "Antwortet stets erfolgreich (keine Enumeration).")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        authService.forgotPassword(request.email(), clientIp(httpRequest));
        return ResponseEntity.ok(new MessageResponse(
                "Falls ein Konto existiert, wurde eine E-Mail zum Zurücksetzen versendet."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Passwort zurücksetzen")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Das Passwort wurde erfolgreich geändert."));
    }

    @GetMapping("/me")
    @Operation(summary = "Aktueller Nutzer", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(authService.me(principal.id()));
    }

    // ---------------------------------------------------------------------

    private void setRefreshCookie(HttpServletResponse response, String rawRefreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieService.buildSetCookieHeader(rawRefreshToken));
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
