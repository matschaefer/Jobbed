package com.jobbed.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobbed.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "app.demo-mode.enabled=true",
        "app.demo-mode.email=demo@jobbed.local"
})
class DemoModeIT extends AbstractIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper objectMapper;

    @Test
    void demoLoginCanReadButCannotWrite() throws Exception {
        ResponseEntity<String> login = rest.postForEntity("/api/v1/auth/demo", Map.of(), String.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(login.getBody());
        assertThat(body.get("user").get("role").asText()).isEqualTo("DEMO");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(body.get("accessToken").asText());

        ResponseEntity<String> list = rest.exchange("/api/v1/applications", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(list.getBody()).get("totalElements").asInt()).isGreaterThan(0);

        ResponseEntity<String> create = rest.exchange("/api/v1/companies", HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "Demo Mutation"), headers), String.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(create.getBody()).contains("Demo-Modus");
    }
}
