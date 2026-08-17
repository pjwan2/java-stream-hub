package com.example.streamhub.service;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.streamhub.dto.PageResponse;
import com.example.streamhub.dto.UserDtos.CreateUserRequest;
import com.example.streamhub.dto.UserDtos.UpdateUserRequest;
import com.example.streamhub.dto.UserDtos.UserResponse;
import com.example.streamhub.entity.AppUser;
import com.example.streamhub.exception.ConflictException;
import com.example.streamhub.exception.NotFoundException;
import com.example.streamhub.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class UserService {

    private static final Logger log = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    

    public UserService(UserRepository userRepository, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.userRepository = userRepository;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
}

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("邮箱已注册: " + request.email());
        }

        AppUser saved = userRepository.save(
                new AppUser(request.username(), request.email(), request.role())
        );
        log.info("Created user id={}, role={}", saved.getId(), saved.getRole());
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        String key = cacheKey(id);
        String cached = readCache(key);
        if (cached != null) {
            UserResponse fromCache = deserialize(cached);
            if (fromCache != null) {
                log.debug("Cache hit for user id={}", id);
                return fromCache;
    }
}
        log.debug("Cache miss for user id={}, querying database", id);
        UserResponse response = UserResponse.from(findEntity(id));
        writeCache(key, response);
        return response;
    }


    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable).map(UserResponse::from);
        return PageResponse.from(page);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        AppUser user = findEntity(id);
        user.updateProfile(request.username(), request.role());
        evictCache(id);
        log.info("Updated user id={}, role={}", id, request.role());
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = findEntity(id);
        userRepository.delete(user);
                evictCache(id);
        log.info("Deleted user id={}", id);
    }

    AppUser findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在: " + id));
    }

    private String cacheKey(Long id) {
        return "user:" + id; }

    private String readCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
    }   catch (Exception e) {
            log.warn("Redis read failed for key={}, falling back to database", key, e);
            return null;
    }
}

    private void writeCache(String key, UserResponse response) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(response), Duration.ofMinutes(5));
    }   catch (Exception e) {
            log.warn("Redis write failed for key={}", key, e);
    }
}

private UserResponse deserialize(String json) {
    try {
        return objectMapper.readValue(json, UserResponse.class);
    } catch (Exception e) {
        log.warn("Failed to deserialize cached user, treating as cache miss", e);
        return null;
    }
}
private void evictCache(Long id) {
    try {
        redisTemplate.delete(cacheKey(id));
    } catch (Exception e) {
        log.warn("Redis evict failed for id={}", id, e);
    }
}

}

