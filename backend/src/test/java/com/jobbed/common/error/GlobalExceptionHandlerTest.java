package com.jobbed.common.error;

import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.common.logging.CorrelationIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prüft das einheitliche Fehlerformat des zentralen Exception-Handlers – ohne
 * vollständigen Spring-Kontext (standalone MockMvc, kein Docker nötig).
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void validationError_returnsUnifiedFormatWithFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, notNullValue()))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.correlationId").value(notNullValue()))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("jobTitle"));
    }

    @Test
    void notFound_returnsResourceNotFoundCode() throws Exception {
        mockMvc.perform(get("/test/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void malformedJson_returnsMalformedRequestCode() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void missingRequestParameter_returnsMalformedRequestInsteadOfInternalError() throws Exception {
        mockMvc.perform(post("/test/required-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @RestController
    static class TestController {

        record Payload(@NotBlank String jobTitle) {
        }

        @PostMapping("/test/validate")
        String validate(@Valid @RequestBody Payload payload) {
            return payload.jobTitle();
        }

        @org.springframework.web.bind.annotation.GetMapping("/test/missing")
        String missing() {
            throw ResourceNotFoundException.of("Bewerbung", "abc");
        }

        @PostMapping("/test/required-param")
        String requiredParam(@org.springframework.web.bind.annotation.RequestParam String value) {
            return value;
        }
    }
}
