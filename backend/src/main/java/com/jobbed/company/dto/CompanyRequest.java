package com.jobbed.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Anlage/Aktualisierung eines Unternehmens. */
public record CompanyRequest(
        @NotBlank(message = "Der Unternehmensname darf nicht leer sein.")
        @Size(max = 200)
        String name,

        @Size(max = 255) String website,
        @Size(max = 120) String industry,
        @Size(max = 50) String companySize,
        @Size(max = 200) String location,
        String description,
        @Size(max = 255) String logoUrl
) {
}
