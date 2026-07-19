package com.jobbed.auth.dto;

/** Antwort auf Login/Refresh: Access-Token im Body, Refresh-Token als Cookie. */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresIn, user);
    }
}
