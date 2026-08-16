package com.example.streamhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.streamhub.dto.UserDtos.CreateUserRequest;
import com.example.streamhub.entity.AppUser;
import com.example.streamhub.entity.UserRole;
import com.example.streamhub.exception.ConflictException;
import com.example.streamhub.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, redisTemplate, objectMapper);
    }

    @Test
    void shouldCreateUserWhenEmailIsNew() {
        var request = new CreateUserRequest("Alice", "alice@example.com", UserRole.VIEWER);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.create(request);

        assertEquals("Alice", response.username());
        assertEquals(UserRole.VIEWER, response.role());
        verify(userRepository).save(org.mockito.ArgumentMatchers.any(AppUser.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        var request = new CreateUserRequest("Alice", "alice@example.com", UserRole.VIEWER);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(request));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
