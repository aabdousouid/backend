package com.bezkoder.spring.security.postgresql.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class PythonBridgeService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${python.api.url:http://localhost:5001}")
    private String pythonApiUrl;

    public Map<String, Object> parseCv(MultipartFile file) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileAsResource);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonApiUrl + "/parse-cv", request, Map.class);

        return response.getBody();
    }

    public List<Map<String, Object>> matchJobs(Map<String, Object> parsedCv, List<Map<String, Object>> jobs) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("parsed_cv", parsedCv);
        payload.put("jobs", jobs);

        ResponseEntity<List> response = restTemplate.postForEntity(
                pythonApiUrl + "/match-jobs",
                payload,
                List.class
        );
        return response.getBody();
    }

    public Map<String, Object> startQuiz(Map<String, Object> job, String candidateName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("job", job);
        payload.put("candidate_name", candidateName);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonApiUrl + "/generate-quiz", payload, Map.class);
        return response.getBody();
    }

    public Map<String, Object> submitQuiz(List<Integer> answers, String candidateName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("answers", answers);
        payload.put("candidate_name", candidateName);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonApiUrl + "/submit-quiz", payload, Map.class);
        return response.getBody();
    }






}