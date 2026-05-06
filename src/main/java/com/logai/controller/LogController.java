package com.logai.controller;

import com.logai.model.LogEntry;
import com.logai.service.LogStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final LogStorageService storageService;

    @Value("${kafka.topic.log}")
    private String logTopic;

    public LogController(KafkaTemplate<String, String> kafkaTemplate,
                         LogStorageService storageService) {
        this.kafkaTemplate = kafkaTemplate;
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> sendLog(@RequestBody String logMessage,
                                                       Authentication authentication) {
        String correlationId = UUID.randomUUID().toString();

        storageService.savePending(correlationId, logMessage);
        kafkaTemplate.send(logTopic, correlationId, logMessage);

        log.info("Logg mottatt | correlationId={} | user={}", correlationId, authentication.getName());

        return ResponseEntity.accepted().body(Map.of(
                "correlationId", correlationId,
                "status", "PENDING"
        ));
    }

    @GetMapping("/{correlationId}")
    public ResponseEntity<?> getResult(@PathVariable String correlationId) {
        try {
            LogEntry entry = storageService.findByCorrelationId(correlationId);
            return ResponseEntity.ok(Map.of(
                    "correlationId", entry.getCorrelationId(),
                    "status", entry.getStatus(),
                    "message", entry.getMessage(),
                    "result", entry.getResult() != null ? entry.getResult() : "",
                    "createdAt", entry.getCreatedAt().toString(),
                    "completedAt", entry.getCompletedAt() != null
                            ? entry.getCompletedAt().toString() : ""
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}