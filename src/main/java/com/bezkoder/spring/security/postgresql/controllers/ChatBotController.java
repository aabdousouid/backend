package com.bezkoder.spring.security.postgresql.controllers;


import com.bezkoder.spring.security.postgresql.services.PythonBridgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat-bot")
public class ChatBotController {

    @Autowired
    private PythonBridgeService pythonBridge;

    @PostMapping("/parse-cv")
    public Map<String, Object> parseCV(@RequestParam("file") MultipartFile file) throws Exception {
        return pythonBridge.parseCv(file);
    }

    @PostMapping("/match")
    public List<Map<String, Object>> matchJobs(@RequestBody Map<String, Object> payload) {
        Map<String, Object> parsedCv = (Map<String, Object>) payload.get("parsed_cv");
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) payload.get("jobs");
        return pythonBridge.matchJobs(parsedCv, jobs);
    }

    @PostMapping("/quiz/start")
    public Map<String, Object> startQuiz(@RequestBody Map<String, Object> payload) {
        Map<String, Object> job = (Map<String, Object>) payload.get("job");
        String candidateName = (String) payload.getOrDefault("candidate_name", "Candidate");
        return pythonBridge.startQuiz(job, candidateName);
    }

    @PostMapping("/quiz/submit")
    public Map<String, Object> submitQuiz(@RequestBody Map<String, Object> payload) {
        List<Integer> answers = (List<Integer>) payload.get("answers");
        String candidateName = (String) payload.getOrDefault("candidate_name", "Candidate");
        return pythonBridge.submitQuiz(answers, candidateName);
    }





}