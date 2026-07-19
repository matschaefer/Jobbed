package com.jobbed.auth.dto;

import com.jobbed.user.Role;

import java.util.UUID;

/** Nutzer-Repräsentation für Auth-Antworten und {@code /auth/me}. */
public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean emailVerified
) {
}
