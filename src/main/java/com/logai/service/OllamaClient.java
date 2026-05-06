package com.logai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    public String generate(String prompt) {

        String url = baseUrl + "/api/generate";

        Map<String, Object> request = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response == null || !response.containsKey("response")) {
            log.error("Tomt svar fra Ollama | url={}", url);
            throw new IllegalStateException("Tomt svar fra Ollama");
        }

        return response.get("response").toString();
    }
}