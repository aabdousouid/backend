package com.bezkoder.spring.security.postgresql.controllers;


import com.bezkoder.spring.security.postgresql.dto.QuizResultRequest;
import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.repository.*;
import com.bezkoder.spring.security.postgresql.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat-bot")
@AllArgsConstructor
public class ChatBotController {

    @Autowired
    private PythonBridgeService pythonBridge;
    private QuizResultRepository quizResultRepository;
    private ApplicationRepositroy applicationRepositroy;
    private UserProfileRepository userProfileRepository;
    private UserRepository userRepository;
    private JobRepository jobRepository;
    private OllamaService ollamaService;
    private ApplicationServiceImpl applicationService;
    private TikaService tika ;
    private final ObjectMapper mapper = new ObjectMapper();
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
        Map<String, Object> parsedCv = (Map<String, Object>) payload.get("parsed_cv");
        Map<String, Object> job = (Map<String, Object>) payload.get("job");
        String candidateName = (String) payload.getOrDefault("candidate_name", "Candidate");
        return pythonBridge.startQuiz(parsedCv, job, candidateName);
    }


    @PostMapping("/quiz/submit")
    public Map<String, Object> submitQuiz(@RequestBody Map<String, Object> payload) {
        List<Integer> answers = (List<Integer>) payload.get("answers");
        String candidateName = (String) payload.getOrDefault("candidate_name", "Candidate");
        return pythonBridge.submitQuiz(answers, candidateName);
    }



    @PostMapping("/quiz/save-result")
    public ResponseEntity<?> saveQuizResult(@RequestBody QuizResultRequest request) {

        Optional<User> userOpt = this.userRepository.findById(request.getUserId());
        Optional<Job> jobOpt = jobRepository.findById(request.getJobId());

        if (userOpt.isEmpty() || jobOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Job not found");
        }

        User user = userOpt.get();
        Job job = jobOpt.get();

        // Find existing application or create one
        Application application = applicationRepositroy
                .findByUserAndJob(user, job)
                .orElseGet(() -> {
                    Application newApp = new Application();
                    newApp.setUser(user);
                    newApp.setJob(job);
                    newApp.setStatus(ApplicationStatus.valueOf("APPROVED"));
                    newApp.setCvFileName(user.getProfile().getCvFilePath());// default status
                    //return applicationRepositroy.save(newApp);
                    return applicationService.applyJob(job,user,newApp);
                });

        // Save quiz result
        QuizResults quizResult = new QuizResults();
        quizResult.setScore(request.getQuizScore());
        quizResult.setMatchScore(request.getMatchScore());
        quizResult.setStatus(request.getStatus());
        quizResult.setSubmittedAt(LocalDateTime.now());
        quizResult.setApplication(application);

        quizResultRepository.save(quizResult);

        return ResponseEntity.ok("Quiz result saved successfully.");
    }



    @PostMapping("/apply-with-quiz/{jobId}/{userId}")
    public ResponseEntity<QuizResults> applyJobWithQuiz(@PathVariable Long jobId,
                                                       @PathVariable Long userId,
                                                       @RequestParam("quizScore") double quizScore,
                                                       @RequestParam("matchingScore") double matchingScore) throws IOException, TikaException {

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Job job = this.jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        UserProfile userProfile = userProfileRepository.findById(user.getProfile().getProfileId())
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        String cvFileName = userProfile.getCvFilePath();
        if (cvFileName == null || cvFileName.isEmpty()) {
            throw new RuntimeException("No CV uploaded in user profile.");
        }

        Path filePath = Paths.get("uploads/" + cvFileName);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("CV file not found on disk.");
        }

        Application application = new Application();
        application.setCvFileName(cvFileName);

        // Extract text from CV using Tika
        String cvText = tika.parseToString(filePath);
        String ollamaResponse = ollamaService.extractCvData(cvText);

        int start = ollamaResponse.indexOf("{");
        int end = ollamaResponse.lastIndexOf("}");
        if (start == -1 || end == -1 || start >= end) {
            throw new RuntimeException("No valid JSON block found in Ollama response.");
        }

        String jsonPart = ollamaResponse.substring(start, end + 1);
        JsonNode jsonNode = mapper.readTree(jsonPart);

        JsonNode skillsNode = jsonNode.get("skills");
        JsonNode summaryNode = jsonNode.get("summary");

        if (skillsNode == null || summaryNode == null) {
            throw new RuntimeException("Missing 'skills' or 'summary' field in Ollama JSON response.");
        }

        application.setExtractedSkills(skillsNode.toString());
        application.setCvSummary(summaryNode.asText());

        // Save Application
        Application savedApplication = applicationService.applyJob(job, user, application);

        // Create and Save QuizResult
        QuizResults result = new QuizResults();
        result.setSubmittedAt(LocalDateTime.now());
        result.setStatus("PASS");
        result.setScore(quizScore);
        result.setMatchScore(matchingScore);
        result.setApplication(savedApplication);

        QuizResults savedResult = quizResultRepository.save(result);

        return ResponseEntity.ok(savedResult);
    }

    @PostMapping("/parse-cv-of-user/{userId}")
    public ResponseEntity<?> parseCvOfUser(@PathVariable Long userId) {
        try {
            Optional<UserProfile> upOpt = userProfileRepository.findById(
                    userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"))
                            .getProfile().getProfileId()
            );
            if (upOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User profile not found");

            UserProfile up = upOpt.get();
            String storedPath = up.getCvFilePath();
            if (storedPath == null || storedPath.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No CV uploaded in user profile");
            }

            // Support either absolute or relative paths
            Path path = Paths.get(storedPath);
            if (!path.isAbsolute()) {
                // Your upload controller defaults to "uploads/cvs/..."
                path = Paths.get("uploads").resolve(storedPath).normalize();
            }

            if (!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CV file not found on disk");
            }

            byte[] bytes = Files.readAllBytes(path);
            String filename = path.getFileName() != null ? path.getFileName().toString() : "cv.pdf";
            String contentType = Files.probeContentType(path); // may be null

            Map<String, Object> parsed = pythonBridge.parseCvBytes(bytes, filename, contentType);
            return ResponseEntity.ok(parsed);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to parse CV: " + e.getMessage());
        }
    }

}