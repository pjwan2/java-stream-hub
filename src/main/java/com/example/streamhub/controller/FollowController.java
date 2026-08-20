package com.example.streamhub.controller;

import com.example.streamhub.dto.FollowDtos.LeaderboardEntry;
import com.example.streamhub.dto.UserDtos.UserResponse;
import com.example.streamhub.service.FollowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static com.example.streamhub.dto.FollowDtos.CreateFollowRequest;
import static com.example.streamhub.dto.FollowDtos.FollowResponse;

@RestController
@RequestMapping("/api")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follows")
    public ResponseEntity<FollowResponse> create(@Valid @RequestBody CreateFollowRequest request) {
        FollowResponse response = followService.create(request);
        return ResponseEntity.created(URI.create("/api/follows/" + response.id())).body(response);
    }

    @GetMapping("/users/{userId}/follows")
    public List<FollowResponse> listByFollower(@PathVariable Long userId) {
        return followService.listByFollower(userId);
    }

    @DeleteMapping("/follows/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        followService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendations")
    public List<UserResponse> recommend(@RequestParam(defaultValue = "10") int limit) {
        return followService.recommend(limit);
    }

    @GetMapping("/streamers/leaderboard")
    public List<LeaderboardEntry> leaderboard(@RequestParam(defaultValue = "10") int limit) {
        return followService.leaderboard(limit);
    }
}
