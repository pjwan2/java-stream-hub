package com.example.streamhub.event;

import com.example.streamhub.entity.FollowType;

public record FollowCreatedEvent(
        Long followerId,
        Long followedId,
        FollowType type
) {
}
