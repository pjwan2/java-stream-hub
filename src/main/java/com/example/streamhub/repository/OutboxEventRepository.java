package com.example.streamhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.streamhub.entity.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByProcessedFalse();
}
