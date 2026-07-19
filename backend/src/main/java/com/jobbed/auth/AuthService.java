package com.jobbed.auth;

import com.jobbed.auth.dto.AuthResponse;
import com.jobbed.auth.dto.LoginRequest;
import com.jobbed.auth.dto.RegisterRequest;
import com.jobbed.auth.dto.UserResponse;
import com.jobbed.auth.email.EmailService;
import com.jobbed.auth.token.UserToken;
import com.jobbed.auth.token.UserTokenRepository;
import com.jobbed.auth.token.UserTokenType;
import com.jobbed.common.error.exception.EmailNotVerifiedException;
import com.jobbed.common.error.exception.ForbiddenException;
import com.jobbed.common.error.exception.InvalidCredentialsException;
import com.jobbed.common.error.exception.InvalidTokenException;
import com.jobbed.common.error.exception.ResourceConflictException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.common.error.exception.SessionExpiredException;
import com.jobbed.common.util.TokenHasher;
import com.jobbed.config.DemoModeProperties;
import com.jobbed.security.AuthProperties;
import com.jobbed.security.AuthenticatedUser;
import com.jobbed.security.JwtService;
import com.jobbed.user.Role;
import com.jobbed.user.User;
import com.jobbed.user.UserMapper;
import com.jobbed.user.UserProfile;
import com.jobbed.user.UserProfileRepository;
import com.jobbed.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Kernlogik der Authentifizierung: Registrierung, Login, Token-Refresh, Logout,
 * E-Mail-Verifikation sowie Passwort-vergessen/-zurücksetzen. Alle Zugriffe auf
 * den aktuellen Nutzer erfolgen server-seitig; die {@code userId} wird nie aus
 * dem Request übernommen.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final LoginRateLimiter rateLimiter;
    private final UserMapper userMapper;
    private final AuthProperties authProperties;
    private final DemoModeProperties demoModeProperties;

    public AuthService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       UserTokenRepository userTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       EmailService emailService,
                       LoginRateLimiter rateLimiter,
                       UserMapper userMapper,
                       AuthProperties authProperties,
                       DemoModeProperties demoModeProperties) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userTokenRepository = userTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.rateLimiter = rateLimiter;
        this.userMapper = userMapper;
        this.authProperties = authProperties;
        this.demoModeProperties = demoModeProperties;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Diese E-Mail-Adresse ist bereits registriert.");
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        userProfileRepository.save(new UserProfile(user));

        String rawToken = createUserToken(user.getId(), UserTokenType.EMAIL_VERIFICATION,
                authProperties.verificationTokenTtlMinutes());
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), rawToken);

        log.info("Neuer Nutzer registriert (id={}).", user.getId());
        return userMapper.toResponse(user);
    }

    @Transactional
    public AuthResult login(LoginRequest request, String userAgent, String ipAddress) {
        String email = normalizeEmail(request.email());
        String rateKey = email + "|" + ipAddress;
        rateLimiter.checkAllowed(rateKey);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            rateLimiter.recordFailure(rateKey);
            throw new InvalidCredentialsException();
        }
        if (!user.isEnabled()) {
            throw new ForbiddenException("Dieses Konto ist deaktiviert.");
        }
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        rateLimiter.recordSuccess(rateKey);
        user.setLastLoginAt(Instant.now());
        return issueTokens(user, userAgent, ipAddress);
    }

    @Transactional
    public AuthResult demoLogin(String userAgent, String ipAddress) {
        if (!demoModeProperties.enabled()) {
            throw new ForbiddenException("Der Demo-Modus ist nicht aktiviert.");
        }
        User user = userRepository.findByEmail(normalizeEmail(demoModeProperties.email()))
                .filter(User::isEnabled)
                .filter(candidate -> candidate.getRole() == Role.DEMO)
                .orElseThrow(() -> new ResourceNotFoundException("Demo-Nutzer wurde nicht gefunden."));
        user.setLastLoginAt(Instant.now());
        return issueTokens(user, userAgent, ipAddress);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        if (rawRefreshToken == null) {
            throw new SessionExpiredException();
        }
        RefreshTokenService.RotationResult rotation =
                refreshTokenService.rotate(rawRefreshToken, userAgent, ipAddress);
        User user = userRepository.findById(rotation.userId())
                .filter(User::isEnabled)
                .orElseThrow(SessionExpiredException::new);

        String accessToken = jwtService.generateAccessToken(userMapper.toPrincipal(user));
        AuthResponse response = AuthResponse.of(accessToken, jwtService.getAccessTokenTtlSeconds(),
                userMapper.toResponse(user));
        return new AuthResult(response, rotation.rawToken());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null) {
            refreshTokenService.revoke(rawRefreshToken);
        }
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        UserToken token = requireUsableToken(rawToken, UserTokenType.EMAIL_VERIFICATION);
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new InvalidTokenException("Ungültiger Token."));
        user.setEmailVerified(true);
        token.setUsedAt(Instant.now());
        log.info("E-Mail bestätigt (userId={}).", user.getId());
    }

    /** Antwortet bewusst immer erfolgreich (keine E-Mail-Enumeration). */
    @Transactional
    public void forgotPassword(String rawEmail, String ipAddress) {
        String email = normalizeEmail(rawEmail);
        String rateKey = "forgot|" + email + "|" + ipAddress;
        rateLimiter.checkAllowed(rateKey);
        rateLimiter.recordFailure(rateKey);

        userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .ifPresent(user -> {
                    userTokenRepository.deleteByUserIdAndType(user.getId(), UserTokenType.PASSWORD_RESET);
                    String rawToken = createUserToken(user.getId(), UserTokenType.PASSWORD_RESET,
                            authProperties.resetTokenTtlMinutes());
                    emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), rawToken);
                });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        UserToken token = requireUsableToken(rawToken, UserTokenType.PASSWORD_RESET);
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new InvalidTokenException("Ungültiger Token."));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsedAt(Instant.now());
        // Alle bestehenden Sitzungen invalidieren.
        refreshTokenService.revokeAllForUser(user.getId());
        log.info("Passwort zurückgesetzt (userId={}).", user.getId());
    }

    @Transactional(readOnly = true)
    public UserResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Nutzer", userId));
        return userMapper.toResponse(user);
    }

    // ---------------------------------------------------------------------

    private AuthResult issueTokens(User user, String userAgent, String ipAddress) {
        AuthenticatedUser principal = userMapper.toPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefresh = refreshTokenService.issue(user.getId(), userAgent, ipAddress);
        AuthResponse response = AuthResponse.of(accessToken, jwtService.getAccessTokenTtlSeconds(),
                userMapper.toResponse(user));
        return new AuthResult(response, rawRefresh);
    }

    private String createUserToken(UUID userId, UserTokenType type, long ttlMinutes) {
        String rawToken = TokenHasher.generateToken();
        UserToken token = new UserToken();
        token.setUserId(userId);
        token.setType(type);
        token.setTokenHash(TokenHasher.sha256(rawToken));
        token.setExpiresAt(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES));
        userTokenRepository.save(token);
        return rawToken;
    }

    private UserToken requireUsableToken(String rawToken, UserTokenType type) {
        UserToken token = userTokenRepository
                .findByTokenHashAndType(TokenHasher.sha256(rawToken), type)
                .orElseThrow(() -> new InvalidTokenException("Der Token ist ungültig oder abgelaufen."));
        if (!token.isUsable()) {
            throw new InvalidTokenException("Der Token ist ungültig oder abgelaufen.");
        }
        return token;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
