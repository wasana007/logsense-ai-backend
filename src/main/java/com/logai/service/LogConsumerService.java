package com.logai.service;

import com.logai.agent.AgentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LogConsumerService {

    private static final Logger log = LoggerFactory.getLogger(LogConsumerService.class);

    private final AgentService agentService;
    private final LogStorageService storageService;

    public LogConsumerService(AgentService agentService,
                              LogStorageService storageService) {
        this.agentService = agentService;
        this.storageService = storageService;
    }

    @KafkaListener(
            topics = "${kafka.topic.log}",
            groupId = "${kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record) {

        String correlationId = record.key();
        String logMessage = record.value();

        log.info("Mottatt | correlationId={}", correlationId);

        try {
            String result = agentService.handle(logMessage);
            storageService.saveResult(correlationId, result);
            log.info("Fullført | correlationId={}", correlationId);

        } catch (Exception e) {
            storageService.saveFailed(correlationId, "FEIL: " + e.getMessage());
            log.error("Mislyktes | correlationId={}", correlationId, e);
        }
    }
}