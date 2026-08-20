package com.example.streamhub.dto;

import com.example.streamhub.entity.Follow;
import com.example.streamhub.entity.FollowType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class FollowDtos {

    private FollowDtos() {
    }

    public record CreateFollowRequest(
            @NotNull Long followerId,
            @NotNull Long followedId,
            @NotNull FollowType type
    ) {
    }

    public record FollowResponse(
            Long id,
            Long followerId,
            Long followedId,
            FollowType type,
            Instant createdAt
    ) {
        public static FollowResponse from(Follow follow) {
            return new FollowResponse(
                    follow.getId(),
                    follow.getFollower().getId(),
                    follow.getFollowed().getId(),
                    follow.getType(),
                    follow.getCreatedAt()
            );
        }
    }

    public record LeaderboardEntry(
            Long streamerId,
            String username,
            long followerCount
    ) {
    }
}
