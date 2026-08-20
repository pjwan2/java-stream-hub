package com.example.streamhub.controller;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import com.redis.testcontainers.RedisContainer;
import com.example.streamhub.dto.UserDtos.CreateUserRequest;
import com.example.streamhub.dto.UserDtos.UserResponse;
import com.example.streamhub.entity.UserRole;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class UserControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldCreateUserAndReturn201() throws Exception {
        var email = "alice-" + UUID.randomUUID() + "@example.com";
        var request = new CreateUserRequest("Alice", email, UserRole.VIEWER);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Alice"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        var email = "dup-" + UUID.randomUUID() + "@example.com";
        var request = new CreateUserRequest("Alice", email, UserRole.VIEWER);
        var json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenEmailInvalid() throws Exception {
        var request = new CreateUserRequest("Alice", "not-an-email", UserRole.VIEWER);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetUserById() throws Exception {
        var email = "get-" + UUID.randomUUID() + "@example.com";
        var request = new CreateUserRequest("Alice", email, UserRole.VIEWER);

        var createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
        var createdUser = objectMapper.readValue(createResult.getResponse().getContentAsString(), UserResponse.class);

        mockMvc.perform(get("/api/users/{id}", createdUser.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }
}
