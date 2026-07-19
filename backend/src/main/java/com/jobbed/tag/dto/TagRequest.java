package com.jobbed.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "Der Tag-Name darf nicht leer sein.")
        @Size(max = 50)
        String name,

        @Pattern(regexp = "^#([0-9a-fA-F]{6})$", message = "Farbe muss ein Hex-Wert wie #3B82F6 sein.")
        String color
) {
}
