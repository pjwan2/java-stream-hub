package com.example.streamhub.user;

import com.example.streamhub.common.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.streamhub.user.UserDtos.CreateUserRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
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
