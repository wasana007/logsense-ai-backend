package com.logai.service;

import com.logai.model.LogEntry;
import com.logai.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogStorageService {

    private final LogRepository repo;

    public LogStorageService(LogRepository repo) {
        this.repo = repo;
    }

    public void savePending(String correlationId, String message) {
        LogEntry entry = new LogEntry();
        entry.setCorrelationId(correlationId);
        entry.setMessage(message);
        entry.setStatus(LogEntry.Status.PENDING);
        repo.save(entry);
    }

    public void saveResult(String correlationId, String result) {
        repo.findByCorrelationId(correlationId).ifPresent(entry -> {
            entry.setResult(result);
            entry.setStatus(LogEntry.Status.COMPLETED);
            entry.setCompletedAt(LocalDateTime.now());
            repo.save(entry);
        });
    }

    public void saveFailed(String correlationId, String errorMessage) {
        repo.findByCorrelationId(correlationId).ifPresent(entry -> {
            entry.setResult(errorMessage);
            entry.setStatus(LogEntry.Status.FAILED);
            entry.setCompletedAt(LocalDateTime.now());
            repo.save(entry);
        });
    }

    public LogEntry findByCorrelationId(String correlationId) {
        return repo.findByCorrelationId(correlationId)
                .orElseThrow(() -> new RuntimeException("Ikke funnet: " + correlationId));
    }
}