package com.jobbed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank(message = "Der Token darf nicht leer sein.")
        String token,

        @NotBlank(message = "Das Passwort darf nicht leer sein.")
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
        String newPassword
) {
}
