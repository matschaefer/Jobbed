package com.jobbed.auth;

import com.jobbed.AbstractIntegrationTest;
import com.jobbed.auth.dto.AuthResponse;
import com.jobbed.auth.dto.LoginRequest;
import com.jobbed.auth.dto.RegisterRequest;
import com.jobbed.auth.dto.UserResponse;
import com.jobbed.user.User;
import com.jobbed.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;

    @Test
    void fullAuthFlow_register_login_me() {
        // 1. Registrierung
        ResponseEntity<UserResponse> register = rest.postForEntity("/api/v1/auth/register",
                new RegisterRequest("Max", "Muster", "flow@b.de", "Str0ng!Passw0rd"), UserResponse.class);
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(register.getBody()).isNotNull();
        assertThat(register.getBody().emailVerified()).isFalse();

        // 2. E-Mail-Verifikation simulieren (Token käme sonst per Mail)
        User user = userRepository.findByEmail("flow@b.de").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        // 3. Login
        ResponseEntity<AuthResponse> login = rest.postForEntity("/api/v1/auth/login",
                new LoginRequest("flow@b.de", "Str0ng!Passw0rd"), AuthResponse.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).isNotNull();
        assertThat(login.getBody().accessToken()).isNotBlank();
        List<String> cookies = login.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anyMatch(c -> c.startsWith("refreshToken=") && c.contains("HttpOnly"));

        // 4. /me mit Bearer-Token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getBody().accessToken());
        ResponseEntity<UserResponse> me = rest.exchange("/api/v1/auth/me",
                org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), UserResponse.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody()).isNotNull();
        assertThat(me.getBody().email()).isEqualTo("flow@b.de");
    }

    @Test
    void me_withoutToken_returns401() {
        ResponseEntity<String> me = rest.getForEntity("/api/v1/auth/me", String.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(me.getBody()).contains("AUTHENTICATION_REQUIRED");
    }

    @Test
    void login_wrongPassword_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = rest.postForEntity("/api/v1/auth/login",
                new LoginRequest("nobody@b.de", "whatever"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).contains("INVALID_CREDENTIALS");
    }
}
