package com.jobbed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registrierungs-Anfrage. */
public record RegisterRequest(
        @NotBlank(message = "Der Vorname darf nicht leer sein.")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Der Nachname darf nicht leer sein.")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Die E-Mail-Adresse darf nicht leer sein.")
        @Email(message = "Bitte eine gültige E-Mail-Adresse angeben.")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Das Passwort darf nicht leer sein.")
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
        String password
) {
}
