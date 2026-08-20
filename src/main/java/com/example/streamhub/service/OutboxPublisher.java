package com.example.streamhub.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.streamhub.entity.OutboxEvent;
import com.example.streamhub.repository.OutboxEventRepository;

@Component
public class OutboxPublisher {

    private static final Logger log = LogManager.getLogger(OutboxPublisher.class);
    private static final String TOPIC = "follow-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByProcessedFalse();
        for (OutboxEvent event : pending) {
            kafkaTemplate.send(TOPIC, event.getPayload());
            event.markProcessed();
            outboxEventRepository.save(event);
            log.info("Published outbox event id={} type={}", event.getId(), event.getEventType());
        }
    }
}
