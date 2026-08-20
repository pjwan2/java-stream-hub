package com.example.streamhub.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Instant;

import com.example.streamhub.dto.UserDtos.CreateUserRequest;
import com.example.streamhub.dto.UserDtos.UserResponse;
import com.example.streamhub.entity.AppUser;
import com.example.streamhub.entity.UserRole;
import com.example.streamhub.exception.ConflictException;
import com.example.streamhub.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWhenEmailIsNew() {
        var request = new CreateUserRequest("Alice", "alice@example.com", UserRole.VIEWER);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = userService.create(request);
        assertEquals("Alice", response.username());
        assertEquals(UserRole.VIEWER, response.role());
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        var request = new CreateUserRequest("Alice", "alice@example.com", UserRole.VIEWER);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }
    @Test
    void shouldReturnFromCacheWhenPresent() {
        var cachedResponse = new UserResponse(1L, "Alice", "alice@example.com", UserRole.VIEWER, Instant.now());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:1")).thenReturn("cached-json");
        when(objectMapper.readValue("cached-json", UserResponse.class)).thenReturn(cachedResponse);

        var response = userService.get(1L);

        assertEquals(cachedResponse, response);
        verify(userRepository, never()).findById(any());
}



    


}
