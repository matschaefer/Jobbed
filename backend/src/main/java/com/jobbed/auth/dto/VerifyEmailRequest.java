package com.jobbed.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Der Token darf nicht leer sein.")
        String token
) {
}
