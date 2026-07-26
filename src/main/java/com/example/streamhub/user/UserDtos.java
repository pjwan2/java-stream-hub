package com.example.streamhub.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 80) String username,
            @NotBlank @Email @Size(max = 160) String email,
            @NotNull UserRole role
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank @Size(max = 80) String username,
            @NotNull UserRole role
    ) {
    }

    public record UserResponse(
            Long id,
            String username,
            String email,
            UserRole role,
            Instant createdAt
    ) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getCreatedAt()
            );
        }
    }
}
