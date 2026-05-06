package com.logai.agent;

import com.logai.tools.DbTool;
import com.logai.tools.KafkaTool;
import com.logai.tools.OllamaTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ToolRouter {

    private static final Logger log = LoggerFactory.getLogger(ToolRouter.class);

    private final DbTool dbTool;
    private final KafkaTool kafkaTool;
    private final OllamaTool ollamaTool;

    public ToolRouter(DbTool dbTool, KafkaTool kafkaTool, OllamaTool ollamaTool) {
        this.dbTool = dbTool;
        this.kafkaTool = kafkaTool;
        this.ollamaTool = ollamaTool;
    }

    public String execute(String decision, String logMessage) {

        log.info("Rå beslutning fra LLM: {}", decision);

        if (decision == null) {
            return ollamaTool.run(logMessage);
        }

        String normalized = decision
                .trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace(".", "");

        return switch (normalized) {

            case "DB_ANALYZE" -> {
                log.info("→ Bruker DB-verktøy");
                yield dbTool.run(logMessage);
            }

            case "KAFKA_ANALYZE" -> {
                log.info("→ Bruker Kafka-verktøy");
                yield kafkaTool.run(logMessage);
            }

            case "OLLAMA_ANALYZE" -> {
                log.info("→ Bruker Ollama-verktøy");
                yield ollamaTool.run(logMessage);
            }

            default -> {
                log.warn("⚠ Ukjent beslutning: {} → bruker LLM som reserve", normalized);
                yield ollamaTool.run(logMessage);
            }
        };
    }
}