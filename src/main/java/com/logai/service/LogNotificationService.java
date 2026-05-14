package com.logai.service;

import com.logai.model.LogEntry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public LogNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
 
    public void notify(LogEntry entry) {
        messagingTemplate.convertAndSend("/topic/logs", toDto(entry));
    }

    private LogEntryDto toDto(LogEntry entry) {
        return new LogEntryDto(
                entry.getCorrelationId(),
                entry.getStatus() != null ? entry.getStatus().name() : null,
                entry.getMessage(),
                entry.getResult(),
                entry.getCreatedAt(),
                entry.getCompletedAt()
        );
    }

    public record LogEntryDto(
            String correlationId,
            String status,
            String message,
            String result,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
    }
}