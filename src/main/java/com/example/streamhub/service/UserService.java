package com.example.streamhub.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.streamhub.dto.UserDtos.CreateUserRequest;
import com.example.streamhub.dto.UserDtos.UpdateUserRequest;
import com.example.streamhub.dto.UserDtos.UserResponse;
import com.example.streamhub.entity.AppUser;
import com.example.streamhub.exception.ConflictException;
import com.example.streamhub.exception.NotFoundException;
import com.example.streamhub.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
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
        log.debug("Fetching user id={}", id);
        return UserResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        AppUser user = findEntity(id);
        user.updateProfile(request.username(), request.role());
        log.info("Updated user id={}, role={}", id, request.role());
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = findEntity(id);
        userRepository.delete(user);
        log.info("Deleted user id={}", id);
    }

    AppUser findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在: " + id));
    }
}
