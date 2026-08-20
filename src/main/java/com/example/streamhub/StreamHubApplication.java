package com.example.streamhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
        title = "Stream Hub API",
        version = "v1",
        description = "直播关注/推荐服务 REST API"
))

@EnableScheduling
@SpringBootApplication
public class StreamHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamHubApplication.class, args);
    }
}
