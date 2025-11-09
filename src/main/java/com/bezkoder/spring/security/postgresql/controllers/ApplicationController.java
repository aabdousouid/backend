package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.services.ApplicationServiceImpl;
import com.bezkoder.spring.security.postgresql.services.JobServiceImpl;
import com.bezkoder.spring.security.postgresql.services.OllamaService;
import com.bezkoder.spring.security.postgresql.services.ProfileServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;
    private final JobServiceImpl jobService;
    private final ProfileServiceImpl profileService;
    private final OllamaService ollamaService;


    private final ObjectMapper mapper = new ObjectMapper();

    private final Tika tika ;
    @PostMapping("/apply/{jobId}/{userId}")
    public ResponseEntity<Application> applyJob(@PathVariable Long jobId,
                                                @PathVariable Long userId,
                                                @RequestParam(value = "cv", required = false) MultipartFile cvFile) throws IOException, TikaException {

        User user = profileService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Job job = jobService.getByIdJob(jobId).orElseThrow(() -> new RuntimeException("Job not found"));

        Application application = new Application();

        if (cvFile != null && !cvFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(cvFile.getOriginalFilename());
            String uploadDir = "uploads/cvs/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(cvFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            application.setCvFileName(fileName);

            // Extract text from CV
            String cvText = tika.parseToString(filePath);

            // Call Ollama
            String ollamaResponse = ollamaService.extractCvData(cvText);
            System.out.println("OLLAMA RAW RESPONSE: " + ollamaResponse);

            // Extract JSON safely
            int start = ollamaResponse.indexOf("{");
            int end = ollamaResponse.lastIndexOf("}");
            if (start != -1 && end != -1 && start < end) {
                String jsonPart = ollamaResponse.substring(start, end + 1);
                System.out.println("Extracted JSON Part: " + jsonPart);

                JsonNode jsonNode = mapper.readTree(jsonPart);

                JsonNode skillsNode = jsonNode.get("skills");
                JsonNode summaryNode = jsonNode.get("summary");

                if (skillsNode == null || summaryNode == null) {
                    throw new RuntimeException("Missing 'skills' or 'summary' field in Ollama JSON response.");
                }

                String skillsJson = skillsNode.toString();
                String summary = summaryNode.asText();

                application.setExtractedSkills(skillsJson);
                application.setCvSummary(summary);

            } else {
                throw new RuntimeException("No valid JSON block found in Ollama response.");
            }
        }

        // Save application and calculate score
        Application savedApplication = applicationService.applyJob(job, user, application);
        return ResponseEntity.ok(savedApplication);
    }

    @GetMapping("/getApplications")
    public ResponseEntity<?> getApplications() {
        return ResponseEntity.ok(this.applicationService.findAllApplications());
    }

    @GetMapping("/getUserApplications/{userId}")
    public ResponseEntity<?> getUserApplications(@PathVariable Long userId) {
        return ResponseEntity.ok(this.applicationService.findUserApplications(userId));
    }

    @DeleteMapping("/deleteApplication/{applicationId}")
    public ResponseEntity<?> deleteById(@PathVariable Long applicationId) {
        this.applicationService.deleteById(applicationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findById/{applicationId}")
    public ResponseEntity<Application> findById(@PathVariable Long applicationId) {
        return ResponseEntity.ok(this.applicationService.findById(applicationId));
    }

    @PutMapping("/updateStatus/{applicationId}")
    public ResponseEntity<Application> updateStatus(@PathVariable Long applicationId, @RequestBody String status){
        return ResponseEntity.ok(this.applicationService.updateStatus(applicationId,status));
    }

    @PutMapping("/addComments/{applicationId}")
    public ResponseEntity<Application> addComments(@PathVariable Long applicationId, @RequestBody String comment){
        return ResponseEntity.ok(this.applicationService.addComment(applicationId,comment));
    }


    @GetMapping("/findByUserAndJob/{userId}/{jobId}")
    public Optional<Boolean> findByUserAndJob (@PathVariable Long userId,@PathVariable Long jobId){
        return this.applicationService.findByUserAndJob(userId,jobId);

    }

    @GetMapping("/findByApplicationId/{applicationId}")
    public Optional<QuizResults> findByApplicationId(@PathVariable Long applicationId){
        return this.applicationService.findQuizByApplicationId(applicationId);
    }


}
