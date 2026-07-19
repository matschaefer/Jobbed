package com.jobbed.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountDeletionRequest(
        @NotBlank String password
) {
}
