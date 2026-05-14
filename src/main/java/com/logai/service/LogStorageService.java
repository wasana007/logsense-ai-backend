package com.logai.service;

import com.logai.model.LogEntry;
import com.logai.model.LogStatus;
import com.logai.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogStorageService {

    private final LogRepository repository;

    public LogStorageService(LogRepository repository) {
        this.repository = repository;
    }

    public void savePending(String correlationId, String message) {
        LogEntry entry = new LogEntry();
        entry.setCorrelationId(correlationId);
        entry.setMessage(message);
        entry.setStatus(LogStatus.PENDING);
        entry.setCreatedAt(LocalDateTime.now());
        repository.save(entry);
    }

    public boolean savePendingIfAbsent(String correlationId, String message) {
        if (repository.existsByCorrelationId(correlationId)) {
            return false;
        }
        savePending(correlationId, message);
        return true;
    }

    public LogEntry saveResult(String correlationId, String result) {
        LogEntry entry = findByCorrelationId(correlationId);
        entry.setStatus(LogStatus.COMPLETED);
        entry.setResult(result);
        entry.setCompletedAt(LocalDateTime.now());
        return repository.save(entry);
    }

    public LogEntry saveFailed(String correlationId, String errorMessage) {
        LogEntry entry = findByCorrelationId(correlationId);
        entry.setResult(errorMessage);
        entry.setStatus(LogStatus.FAILED);
        entry.setCompletedAt(LocalDateTime.now());
        return repository.save(entry);
    }

    public LogEntry findByCorrelationId(String correlationId) {
        return repository.findByCorrelationId(correlationId)
                .orElseThrow(() -> new RuntimeException("Ikke funnet: " + correlationId));
    }
}