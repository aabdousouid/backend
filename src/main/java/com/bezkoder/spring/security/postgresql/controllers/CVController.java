package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.MultipartInputStreamFileResource;
import com.bezkoder.spring.security.postgresql.dto.JobRecommendation;
import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.RecommendationRequest;
import com.bezkoder.spring.security.postgresql.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

    @CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cv")
@AllArgsConstructor
public class CVController {

    private final Path root = Paths.get("uploads");
    private final RestTemplate restTemplate;
    private final JobRepository jobRepository;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }



    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama3");
        body.put("prompt", "Tu es un assistant virtuel de recrutement. Réponds clairement à : " + message);
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> ollamaResponse = restTemplate.postForEntity(
                "http://localhost:11434/api/generate", request, Map.class);

        String reply = (String) ollamaResponse.getBody().get("response");
        return ResponseEntity.ok(Map.of("reply", reply));
    }



     @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            Path filePath = root.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String cvText = extractTextFromCV(filePath);

            // Call LLM to parse structured JSON from raw text
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "llama3");
            payload.put("prompt", """
                Extract the following fields from the CV text below and return as JSON:
                {
                  "skills": [],
                  "experience": [],
                  "education": [],
                  "certifications": [],
                  "languages": [],
                  "experienceLevel": "",
                  "industries": []
                }

                CV:
                """ + cvText);
            payload.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:11434/api/generate", requestEntity, Map.class);
            String jsonResult = (String) response.getBody().get("response");

            return ResponseEntity.ok(Map.of(
                    "message", "CV uploaded and parsed successfully",
                    "json", jsonResult
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


        @GetMapping("/cv/{filename:.+}")
        public ResponseEntity<Resource> downloadCv(@PathVariable String filename) throws IOException {
            Path filePath = Paths.get("uploads/cvs/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new FileNotFoundException("CV file not found: " + filename);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }



        private String extractTextFromCV(Path filePath) throws Exception {
        try (InputStream stream = Files.newInputStream(filePath)) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            parser.parse(stream, handler, metadata, new ParseContext());
            return handler.toString();
        }
    }








        @PostMapping("/recommend")
    public ResponseEntity<?> recommendJobs(@RequestBody Map<String, Object> requestBody) {
        try {
            String cvJson = (String) requestBody.get("cvJson");
            List<Job> jobs = jobRepository.findAll();

            StringBuilder prompt = new StringBuilder();
            prompt.append("You are a career advisor. Based on the structured CV JSON and the list of jobs below, recommend the top 3 matching jobs in the following JSON format:\n\n");
            prompt.append("[\n");
            prompt.append("  {\n");
            prompt.append("    \"title\": \"...\",\n");

            prompt.append("    \"description\": \"...\",\n");
            prompt.append("    \"matchPercentage\": 95,\n");
            prompt.append("    \"experienceLevel\": \"Mid-level\",\n");
            //prompt.append("    \"salaryRange\": \"$60,000 - $80,000\",\n");
            prompt.append("    \"requiredSkills\": [\"Java\", \"Spring Boot\", \"SQL\"],\n");
            prompt.append("    \"justification\": \"Matches backend and SQL skills\"\n");
            prompt.append("  }\n");
            prompt.append("]\n\n");

            prompt.append("CV:\n").append(cvJson).append("\n\nJobs:\n");
            int i = 1;
            for (Job job : jobs) {
                prompt.append(i++).append(". ").append(job.getTitle()).append(" - ").append(job.getDescription()).append("\n");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "llama3");
            payload.put("prompt", prompt.toString());
            payload.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:11434/api/generate", entity, Map.class);
            String rawResponse = (String) response.getBody().get("response");

            // Try to extract only the JSON part from LLM response
            String jsonList = extractJsonArrayFromText(rawResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            List<JobRecommendation> recommendations = Arrays.asList(
                    objectMapper.readValue(jsonList, JobRecommendation[].class)
            );

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String extractJsonArrayFromText(String text) {
        int startIndex = text.indexOf("[");
        int endIndex = text.lastIndexOf("]") + 1;
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex);
        }
        throw new RuntimeException("Failed to extract JSON array from LLM response");
    }


}
