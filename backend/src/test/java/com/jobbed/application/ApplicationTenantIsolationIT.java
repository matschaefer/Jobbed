package com.jobbed.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobbed.AbstractIntegrationTest;
import com.jobbed.user.User;
import com.jobbed.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sicherheitskritisch: Ein Nutzer darf niemals fremde Bewerbungen abrufen,
 * ändern oder löschen. Fremdzugriffe müssen als 404 erscheinen.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTenantIsolationIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepository;
    @Autowired ObjectMapper objectMapper;

    private String registerAndLogin(String email) {
        rest.postForEntity("/api/v1/auth/register",
                Map.of("firstName", "T", "lastName", "U", "email", email, "password", "Str0ng!Passw0rd"),
                String.class);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);
        ResponseEntity<String> login = rest.postForEntity("/api/v1/auth/login",
                Map.of("email", email, "password", "Str0ng!Passw0rd"), String.class);
        return json(login.getBody()).get("accessToken").asText();
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private JsonNode json(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String postId(String url, Object body, String token) {
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body, auth(token)), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return json(resp.getBody()).get("id").asText();
    }

    @Test
    void userCannotAccessAnotherUsersApplication() {
        String tokenA = registerAndLogin("owner@b.de");
        String tokenB = registerAndLogin("intruder@b.de");

        String companyId = postId("/api/v1/companies", Map.of("name", "Acme GmbH"), tokenA);
        String appId = postId("/api/v1/applications",
                Map.of("companyId", companyId, "jobTitle", "Senior Java Developer"), tokenA);

        // Besitzer sieht die Bewerbung
        ResponseEntity<String> ownerGet = rest.exchange("/api/v1/applications/" + appId, HttpMethod.GET,
                new HttpEntity<>(auth(tokenA)), String.class);
        assertThat(ownerGet.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Fremder Nutzer: GET/PUT/PATCH-Status/DELETE -> jeweils 404
        assertThat(get("/api/v1/applications/" + appId, tokenB)).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> put = rest.exchange("/api/v1/applications/" + appId, HttpMethod.PUT,
                new HttpEntity<>(Map.of("companyId", companyId, "jobTitle", "Hacked"), auth(tokenB)), String.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> status = rest.exchange("/api/v1/applications/" + appId + "/status",
                HttpMethod.PATCH, new HttpEntity<>(Map.of("newStatus", "REJECTED"), auth(tokenB)), String.class);
        assertThat(status.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> delete = rest.exchange("/api/v1/applications/" + appId, HttpMethod.DELETE,
                new HttpEntity<>(auth(tokenB)), String.class);
        assertThat(delete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Fremder Nutzer sieht die Bewerbung nicht in seiner Liste
        ResponseEntity<String> listB = rest.exchange("/api/v1/applications", HttpMethod.GET,
                new HttpEntity<>(auth(tokenB)), String.class);
        assertThat(json(listB.getBody()).get("totalElements").asInt()).isZero();

        // Bewerbung existiert weiterhin für den Besitzer
        assertThat(get("/api/v1/applications/" + appId, tokenA)).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listFiltersByStatus() {
        String token = registerAndLogin("filter@b.de");
        String companyId = postId("/api/v1/companies", Map.of("name", "Filter Co"), token);
        String appId = postId("/api/v1/applications",
                Map.of("companyId", companyId, "jobTitle", "Dev", "currentStatus", "APPLIED"), token);
        assertThat(appId).isNotBlank();

        ResponseEntity<String> applied = rest.exchange("/api/v1/applications?status=APPLIED", HttpMethod.GET,
                new HttpEntity<>(auth(token)), String.class);
        assertThat(json(applied.getBody()).get("totalElements").asInt()).isEqualTo(1);

        ResponseEntity<String> offer = rest.exchange("/api/v1/applications?status=OFFER", HttpMethod.GET,
                new HttpEntity<>(auth(token)), String.class);
        assertThat(json(offer.getBody()).get("totalElements").asInt()).isZero();
    }

    private HttpStatus get(String url, String token) {
        return (HttpStatus) rest.exchange(url, HttpMethod.GET, new HttpEntity<>(auth(token)), String.class)
                .getStatusCode();
    }
}
