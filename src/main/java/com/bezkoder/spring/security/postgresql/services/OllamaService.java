package com.bezkoder.spring.security.postgresql.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractCvData(String cvText) {
        String url = "http://localhost:11434/api/generate";  // Adjust if needed

        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama3"); // or whichever model you're using
        request.put("prompt", buildPrompt(cvText));
        request.put("stream", false);

        Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
        return response != null ? response.get("response") : null;
    }

    private String buildPrompt(String cvText) {
        return """
        Analyze the following CV text and provide two things in JSON format ONLY (no explanation, no extra text):
        {
          "skills": ["Skill1", "Skill2", "..."],
          "summary": "Concise professional summary (max 5 sentences)"
        }
        CV text:
        """ + cvText;
    }
}
