package com.example.streamhub.service;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.streamhub.dto.FollowDtos.CreateFollowRequest;
import com.example.streamhub.dto.FollowDtos.FollowResponse;
import com.example.streamhub.dto.FollowDtos.LeaderboardEntry;
import com.example.streamhub.dto.UserDtos.UserResponse;
import com.example.streamhub.entity.AppUser;
import com.example.streamhub.entity.Follow;
import com.example.streamhub.entity.FollowType;
import com.example.streamhub.entity.OutboxEvent;
import com.example.streamhub.entity.UserRole;
import com.example.streamhub.event.FollowCreatedEvent;
import com.example.streamhub.event.LeaderboardKeys;
import com.example.streamhub.exception.ConflictException;
import com.example.streamhub.exception.NotFoundException;
import com.example.streamhub.repository.FollowRepository;
import com.example.streamhub.repository.OutboxEventRepository;
import com.example.streamhub.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class FollowService {

    private static final Logger log = LogManager.getLogger(FollowService.class);

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public FollowService(FollowRepository followRepository, UserRepository userRepository,
                          OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper,
                          StringRedisTemplate redisTemplate) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public FollowResponse create(CreateFollowRequest request) {
        if (request.followerId().equals(request.followedId())) {
            throw new ConflictException("用户不能关注自己");
        }
        if (followRepository.existsByFollowerIdAndFollowedIdAndType(
                request.followerId(), request.followedId(), request.type())) {
            throw new ConflictException("该关注/订阅关系已存在");
        }

        AppUser follower = findUser(request.followerId());
        AppUser followed = findUser(request.followedId());
        if (followed.getRole() != UserRole.STREAMER) {
            throw new ConflictException("只能关注或订阅主播");
        }

        Follow saved = followRepository.save(new Follow(follower, followed, request.type()));
        var event = new FollowCreatedEvent(follower.getId(), followed.getId(), request.type());
        outboxEventRepository.save(new OutboxEvent("FollowCreated", objectMapper.writeValueAsString(event)));

        log.info("Created relation follower={}, followed={}, type={}",
                follower.getId(), followed.getId(), request.type());
        return FollowResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FollowResponse> listByFollower(Long followerId) {
        log.debug("Listing follows for followerId={}", followerId);
        findUser(followerId);

        return followRepository.findByFollowerId(followerId).stream()
                .map(FollowResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Follow follow = followRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("关注关系不存在: " + id));
        followRepository.delete(follow);
        log.info("Deleted relation id={}", id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> recommend(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        log.debug("Recommending streamers, limit={}, safeLimit={}", limit, safeLimit);
        List<Long> rankedIds = followRepository.rankStreamerIds(FollowType.FOLLOW).stream()
                .limit(safeLimit)
                .map(row -> (Long) row[0])
                .toList();

        var usersById = userRepository.findAllById(rankedIds).stream()
                .collect(java.util.stream.Collectors.toMap(AppUser::getId, user -> user));

        return rankedIds.stream()
                .map(usersById::get)
                .filter(java.util.Objects::nonNull)
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        Set<ZSetOperations.TypedTuple<String>> topEntries =
                redisTemplate.opsForZSet().reverseRangeWithScores(LeaderboardKeys.STREAMER_LEADERBOARD, 0, safeLimit - 1);

        return topEntries.stream()
                .map(tuple -> {
                    Long streamerId = Long.valueOf(tuple.getValue());
                    AppUser streamer = findUser(streamerId);
                    long followerCount = tuple.getScore().longValue();
                    return new LeaderboardEntry(streamerId, streamer.getUsername(), followerCount);
                })
                .toList();
    }

    private AppUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在: " + id));
    }
}
