package com.logai.tools;

import com.logai.service.OllamaClient;
import org.springframework.stereotype.Component;

@Component
public class OllamaTool implements Tool {

    private final OllamaClient ollamaClient;

    public OllamaTool(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    @Override
    public String run(String input) {
        String prompt = """
                Du er et AI-system som analyserer logger.
                Forklar årsaken kort:
                LOGG:
                %s
                """.formatted(input);

        return ollamaClient.generate(prompt);
    }
}