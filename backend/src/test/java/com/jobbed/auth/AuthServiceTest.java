package com.jobbed.auth;

import com.jobbed.auth.dto.LoginRequest;
import com.jobbed.auth.dto.RegisterRequest;
import com.jobbed.auth.email.EmailService;
import com.jobbed.auth.token.UserTokenRepository;
import com.jobbed.common.error.exception.EmailNotVerifiedException;
import com.jobbed.common.error.exception.InvalidCredentialsException;
import com.jobbed.common.error.exception.ResourceConflictException;
import com.jobbed.config.DemoModeProperties;
import com.jobbed.security.AuthProperties;
import com.jobbed.security.JwtService;
import com.jobbed.user.Role;
import com.jobbed.user.User;
import com.jobbed.user.UserMapper;
import com.jobbed.user.UserProfileRepository;
import com.jobbed.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock UserTokenRepository userTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock EmailService emailService;
    @Spy LoginRateLimiter rateLimiter = new LoginRateLimiter();
    @Spy UserMapper userMapper = new UserMapper();

    AuthService authService;

    private final AuthProperties props = new AuthProperties("test-secret-1234567890-abcdefghijklmnop",
            900, 604800, 1440, 30, "http://localhost:4200",
            new AuthProperties.Cookie("refreshToken", "/api/v1/auth", false, "Strict"));

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, userProfileRepository, userTokenRepository,
                passwordEncoder, jwtService, refreshTokenService, emailService, rateLimiter,
                userMapper, props, new DemoModeProperties(false, "demo@jobbed.local"));
    }

    private User verifiedUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Max");
        user.setLastName("Muster");
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
    }

    @Test
    void register_persistsUserAndSendsVerification() {
        when(userRepository.existsByEmail("max@b.de")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = authService.register(new RegisterRequest("Max", "Muster", "Max@B.de", "Str0ng!Passw0rd"));

        assertThat(response.email()).isEqualTo("max@b.de"); // normalisiert
        verify(userProfileRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("max@b.de"), eq("Max"), anyString());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("max@b.de")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Max", "Muster", "max@b.de", "Str0ng!Passw0rd")))
                .isInstanceOf(ResourceConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsTokens() {
        User user = verifiedUser("max@b.de");
        when(userRepository.findByEmail("max@b.de")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Str0ng!Passw0rd", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any(), any(), any())).thenReturn("raw-refresh");

        AuthResult result = authService.login(new LoginRequest("max@b.de", "Str0ng!Passw0rd"), "UA", "127.0.0.1");

        assertThat(result.response().accessToken()).isEqualTo("access-token");
        assertThat(result.rawRefreshToken()).isEqualTo("raw-refresh");
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        User user = verifiedUser("max@b.de");
        when(userRepository.findByEmail("max@b.de")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("max@b.de", "wrong"), "UA", "127.0.0.1"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail("nobody@b.de")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("nobody@b.de", "whatever"), "UA", "127.0.0.1"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unverifiedEmail_throwsEmailNotVerified() {
        User user = verifiedUser("max@b.de");
        user.setEmailVerified(false);
        when(userRepository.findByEmail("max@b.de")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("max@b.de", "Str0ng!Passw0rd"), "UA", "127.0.0.1"))
                .isInstanceOf(EmailNotVerifiedException.class);
    }
}
