package com.logai.repository;

import com.logai.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogRepository extends JpaRepository<LogEntry, Long> {
    Optional<LogEntry> findByCorrelationId(String correlationId);
}