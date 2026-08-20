package com.example.streamhub.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.streamhub.event.FollowCreatedEvent;
import com.example.streamhub.event.LeaderboardKeys;

import tools.jackson.databind.ObjectMapper;

@Component
public class LeaderboardAggregator {

    private static final Logger log = LogManager.getLogger(LeaderboardAggregator.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public LeaderboardAggregator(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "follow-events", groupId = "leaderboard-aggregator")
    public void onFollowCreated(String payload) {
        FollowCreatedEvent event = objectMapper.readValue(payload, FollowCreatedEvent.class);
        redisTemplate.opsForZSet().incrementScore(LeaderboardKeys.STREAMER_LEADERBOARD, event.followedId().toString(), 1);
        log.info("Incremented leaderboard score for streamer id={}", event.followedId());
    }
}
