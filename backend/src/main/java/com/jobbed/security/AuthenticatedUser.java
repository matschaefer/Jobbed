package com.jobbed.security;

import com.jobbed.user.Role;

import java.util.UUID;

/**
 * Im Security-Context hinterlegter Principal. Der aktuelle Nutzer wird
 * ausschließlich hieraus ermittelt – niemals aus Request-Parametern.
 */
public record AuthenticatedUser(UUID id, String email, Role role) {
}
