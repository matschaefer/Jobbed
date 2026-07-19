package com.jobbed.user;

import com.jobbed.auth.dto.UserResponse;
import com.jobbed.security.AuthenticatedUser;
import org.springframework.stereotype.Component;

/** Übersetzt {@link User}-Entities in API-DTOs. */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified());
    }

    public AuthenticatedUser toPrincipal(User user) {
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
    }
}
