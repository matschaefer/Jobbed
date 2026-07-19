package com.jobbed.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Anlage/Aktualisierung eines Ansprechpartners. */
public record ContactRequest(
        @NotNull(message = "Ein Unternehmen ist erforderlich.")
        UUID companyId,

        @NotBlank(message = "Der Vorname darf nicht leer sein.")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Der Nachname darf nicht leer sein.")
        @Size(max = 100)
        String lastName,

        @Size(max = 120) String position,
        @Email(message = "Bitte eine gültige E-Mail-Adresse angeben.") @Size(max = 255) String email,
        @Size(max = 50) String phone,
        @Size(max = 255) String linkedInUrl,
        String notes
) {
}
