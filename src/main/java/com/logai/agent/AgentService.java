package com.logai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logai.service.OllamaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final ToolRouter toolRouter;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentService(ToolRouter toolRouter, OllamaClient ollamaClient) {
        this.toolRouter = toolRouter;
        this.ollamaClient = ollamaClient;
    }

    public String handle(String logMessage) {
        String decision = decide(logMessage);
        log.info("LLM-beslutning: {}", decision);
        return toolRouter.execute(decision, logMessage);
    }

    private String decide(String logMessage) {
        String prompt = """
                Du er en AI-agent som velger riktig analyseverktøy for en loggmelding.
                
                Tilgjengelige verktøy:
                - DB_ANALYZE     : Bruk når loggen er relatert til databasefeil, SQL, tilkoblinger, spørringer eller tidsavbrudd mot DB
                - KAFKA_ANALYZE  : Bruk når loggen er relatert til Kafka, meldingskø, topic, consumer, producer eller broker
                - OLLAMA_ANALYZE : Bruk for alle andre logger som krever generell AI-resonnering
                
                Svar KUN med et gyldig JSON-objekt, ingen forklaring, ingen markdown:
                {"tool": "DB_ANALYZE"}
                
                Logg som skal analyseres:
                %s
                """.formatted(logMessage);

        try {
            String raw = ollamaClient.generate(prompt);
            String cleaned = extractJson(raw);
            Map<?, ?> parsed = objectMapper.readValue(cleaned, Map.class);
            String tool = parsed.get("tool").toString().trim().toUpperCase();

            return switch (tool) {
                case "DB_ANALYZE", "KAFKA_ANALYZE", "OLLAMA_ANALYZE" -> tool;
                default -> {
                    log.warn("Ukjent verktøy fra LLM: {} → bruker OLLAMA_ANALYZE", tool);
                    yield "OLLAMA_ANALYZE";
                }
            };

        } catch (Exception e) {
            log.error("Kunne ikke tolke LLM-beslutning: {} → bruker OLLAMA_ANALYZE", e.getMessage());
            return "OLLAMA_ANALYZE";
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            throw new RuntimeException("Ingen JSON funnet i LLM-respons: " + raw);
        }
        return raw.substring(start, end + 1);
    }
}