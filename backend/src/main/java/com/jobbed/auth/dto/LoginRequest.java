package com.jobbed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Login-Anfrage. */
public record LoginRequest(
        @NotBlank(message = "Die E-Mail-Adresse darf nicht leer sein.")
        @Email(message = "Bitte eine gültige E-Mail-Adresse angeben.")
        String email,

        @NotBlank(message = "Das Passwort darf nicht leer sein.")
        String password
) {
}
