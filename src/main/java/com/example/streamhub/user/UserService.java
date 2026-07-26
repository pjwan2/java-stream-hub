package com.example.streamhub.user;

import com.example.streamhub.common.ConflictException;
import com.example.streamhub.common.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.streamhub.user.UserDtos.CreateUserRequest;
import static com.example.streamhub.user.UserDtos.UpdateUserRequest;
import static com.example.streamhub.user.UserDtos.UserResponse;

@Service
public class UserService {

    private static final Logger log = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
