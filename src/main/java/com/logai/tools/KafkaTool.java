package com.logai.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTool {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.log}")
    private String logTopic;

    public KafkaTool(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String run(String logMessage) {
        kafkaTemplate.send(logTopic, logMessage);
        return "Sendt til Kafka-analysepipeline";
    }
}