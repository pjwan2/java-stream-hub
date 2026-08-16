package com.example.streamhub.service;

import com.example.streamhub.entity.AppUser;
import com.example.streamhub.entity.Follow;
import com.example.streamhub.entity.FollowType;
import com.example.streamhub.entity.UserRole;
import com.example.streamhub.exception.ConflictException;
import com.example.streamhub.repository.FollowRepository;
import com.example.streamhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.streamhub.dto.FollowDtos.CreateFollowRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;

    private FollowService followService;

    @BeforeEach
    void setUp() {
        followService = new FollowService(followRepository, userRepository);
    }

    @Test
    void shouldCreateFollowForStreamer() {
        AppUser viewer = new AppUser("Alice", "alice@example.com", UserRole.VIEWER);
        AppUser streamer = new AppUser("Bob", "bob@example.com", UserRole.STREAMER);
        var request = new CreateFollowRequest(1L, 2L, FollowType.FOLLOW);
        when(followRepository.existsByFollowerIdAndFollowedIdAndType(1L, 2L, FollowType.FOLLOW))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(viewer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(streamer));
        when(followRepository.save(any(Follow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = followService.create(request);

        assertEquals(FollowType.FOLLOW, response.type());
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void shouldRejectFollowingSelf() {
        var request = new CreateFollowRequest(1L, 1L, FollowType.FOLLOW);

        assertThrows(ConflictException.class, () -> followService.create(request));
        verify(followRepository, never()).save(any());
    }

    @Test
    void shouldRejectFollowingNonStreamer() {
        AppUser viewer = new AppUser("Alice", "alice@example.com", UserRole.VIEWER);
        AppUser anotherViewer = new AppUser("Carol", "carol@example.com", UserRole.VIEWER);
        var request = new CreateFollowRequest(1L, 2L, FollowType.FOLLOW);
        when(userRepository.findById(1L)).thenReturn(Optional.of(viewer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherViewer));

        assertThrows(ConflictException.class, () -> followService.create(request));
        verify(followRepository, never()).save(any());
    }
}
