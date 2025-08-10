package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.dto.ScoreResponseDto;
import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import com.bezkoder.spring.security.postgresql.repository.JobRepository;
import com.bezkoder.spring.security.postgresql.repository.QuizResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {


    private final EmailServiceImpl emailService;
    private final ProfileServiceImpl profileService;

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationServiceImpl notificationService;

    private final ApplicationRepositroy applicationRepositroy;
    private final JobRepository jobRepository;
    private final QuizResultRepository quizResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";


    private ScoreResponseDto requestMatchingScore(String jobDescription, String skills, String summary) {
        String prompt = String.format("""
            Given the following job description and the applicant's CV details, provide a matching score out of 100 indicating how suitable this applicant is for this job. Also add a short comment.
            
            Job description:
            %s
            
            Applicant CV details:
            Skills: %s
            Summary: %s
            
            Respond ONLY in JSON format as follows:
            {
              "matchingScore": <score as integer>,
              "comment": "<short comment>"
            }
            """, jobDescription, skills, summary);

        String requestBody = """
        {
            "model": "llama3",
            "prompt": "%s",
            "stream": false
        }
        """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(OLLAMA_URL, HttpMethod.POST, entity, String.class);

        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            String rawResponse = node.get("response").asText();

            // Remove potential text before JSON
            int jsonStart = rawResponse.indexOf("{");
            if (jsonStart > 0) {
                rawResponse = rawResponse.substring(jsonStart);
            }

            ScoreResponseDto scoreResponse = objectMapper.readValue(rawResponse, ScoreResponseDto.class);
            return scoreResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse matching score JSON from Ollama", e);
        }
    }

    @Override
    public Application applyJob(Job job, User user, Application application) {

        //check if application already exists
        Optional<Application> existtingApplication = this.applicationRepositroy.findByUserAndJob(user,job);
        if(existtingApplication.isPresent()){
            throw new RuntimeException("User has already applied for this Job");
        }

        application.setUser(user);
        application.setJob(job);
        application.setAppliedDate(LocalDateTime.now());

        // Save initially as PENDING
        application.setStatus(ApplicationStatus.PENDING);
        application = this.applicationRepositroy.save(application);

        String jobDescription = job.getDescription();
        String summary = application.getCvSummary();

        // Parse skills JSON string
        List<String> skillList;
        try {
            skillList = objectMapper.readValue(application.getExtractedSkills(), List.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse extracted skills JSON", e);
        }
        String skills = String.join(", ", skillList);

        // Get matching score
        ScoreResponseDto scoreDto = requestMatchingScore(jobDescription, skills, summary);
        double score = (double) scoreDto.getMatchingScore();

        application.setMatchingScore(score);
        application.setMatchingComment(scoreDto.getComment());

        // Apply auto status transition
        if (score == 0.0) {
            application.setStatus(ApplicationStatus.REJECTED);
            emailService.sendRejectionEmail(user.getEmail(), application);
        } else if (score == 100.0) {
            application.setStatus(ApplicationStatus.APPROVED);
            emailService.sendApprovalEmail(user.getEmail(), application);
        }

        emailService.sendApplicationEmail(user.getEmail());

        return applicationRepositroy.save(application);
    }






    @Override
    public Application updateStatus(Long applicationId, String newStatusStr) {
        Application application = applicationRepositroy.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        ApplicationStatus currentStatus = application.getStatus();
        ApplicationStatus newStatus = ApplicationStatus.valueOf(newStatusStr.toUpperCase());

        // Block changes from terminal states
        if (currentStatus == ApplicationStatus.REJECTED || currentStatus == ApplicationStatus.HIRED) {
            throw new IllegalStateException("Cannot update status from " + currentStatus);
        }

        // Enforce allowed transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != ApplicationStatus.APPROVED && newStatus != ApplicationStatus.REJECTED) {
                    throw new IllegalStateException("From PENDING, status can only be changed to APPROVED or REJECTED");
                }
                break;

            case APPROVED:
                if (newStatus != ApplicationStatus.INTERVIEW && newStatus != ApplicationStatus.REJECTED) {
                    throw new IllegalStateException("From APPROVED, status can only be changed to INTERVIEW or REJECTED");
                }
                break;

            case INTERVIEW:
                if (newStatus == ApplicationStatus.HIRED) {
                    // Must have RH and TECHNIQUE interviews
                    long rhCount = application.getInterviews().stream()
                            .filter(i -> i.getInterviewTest().equals(InterviewTest.valueOf("RH")))
                            .count();
                    long techCount = application.getInterviews().stream()
                            .filter(i -> i.getInterviewTest().equals(InterviewTest.valueOf("TECHNIQUE")))
                            .count();
                    if (rhCount == 0 || techCount == 0) {
                        throw new IllegalStateException("Cannot mark as HIRED without both RH and TECHNIQUE interviews");
                    }
                } else if (newStatus != ApplicationStatus.REJECTED) {
                    throw new IllegalStateException("From INTERVIEW, status can only be changed to HIRED or REJECTED");
                }
                break;
        }

        // All good: update status
        application.setStatus(newStatus);
        application.setLastUpdated(LocalDateTime.now());

        // Email notifications
        String email = application.getUser().getEmail();
        if (newStatus == ApplicationStatus.APPROVED) {
            emailService.sendApprovalEmail(email, application);
        } else if (newStatus == ApplicationStatus.REJECTED) {
            emailService.sendRejectionEmail(email, application);
        } else if (newStatus == ApplicationStatus.HIRED) {
            emailService.sendHiredEmail(email,application);
        }

        // === NOTIFICATION LOGIC ===
        User targetUser = application.getUser();
        String notifMsg = "Your application status for " + application.getJob().getTitle()
                + " has changed to: " + newStatus.toString();

        Notifications notif = new Notifications();
        notif.setRecipient(targetUser);
        notif.setMessage(notifMsg);
        notif.setType(NotificationType.APPLICATION_STATUS_CHANGED); // Update your enum as needed
        notif.setCreatedAt(new Date());
        notif.setIsRead(false);
        notificationService.sendNotification(
                targetUser.getUsername(),
                notif
        );
        // ==========================
        return applicationRepositroy.save(application);
    }


    @Override
    public Application addComment(Long applicationId, String comments) {
        Application application = applicationRepositroy.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("application not found"));

        List<String> oldComments = application.getAdminComments();

        if (oldComments == null) {
            oldComments = new ArrayList<>();
        }

        oldComments.add(comments);
        application.setAdminComments(oldComments);

        return applicationRepositroy.save(application);
    }

    @Override
    public Optional<Boolean> findByUserAndJob(Long userId, Long jobId) {
        User user = this.profileService.getUserById(userId).orElseThrow(()->new RuntimeException("User not found"));
        Job job = this.jobRepository.findById(jobId).orElseThrow(()->new RuntimeException("Job not found"));
        boolean returnedBool = false; // false the user did not apply for the job before, true already applied
        List<Application> applications = this.applicationRepositroy.findAll();

        for (Application application: applications
             ) {
            if (application.getJob().getJobId().equals(jobId) && application.getUser().getId().equals(userId)) {

                returnedBool = true;
                break;
            }


        }


         return Optional.of(returnedBool);
    }

    @Override
    public Optional<QuizResults> findQuizByApplicationId(Long applicationId) {

         QuizResults quizResults = this.quizResultRepository.findByApplicationApplicationId(applicationId).orElseThrow(()->new RuntimeException("Application Do not Have any Quiz"));


        return Optional.of(quizResults);

    }


    @Override
    public Application findById(Long applicationId) {
        return applicationRepositroy.findById(applicationId).orElseThrow(()-> new RuntimeException("Application not found"));
    }

    @Override
    public List<Application> findAllApplications() {

        List<Application> applications = applicationRepositroy.findAll();

        if(applications.isEmpty()){
            throw new RuntimeException("No Applications found");
        }
        return applications;
    }

    @Override
    public List<Application> findUserApplications(Long userId) {

        List<Application> applications = this.applicationRepositroy.findByUserId(userId);

        if(applications.isEmpty()){
            throw new RuntimeException("No Applications found");
        }
        return applications;
    }




    @Override
    public void deleteById(Long applicationId){
        this.applicationRepositroy.deleteById(applicationId);
    }


}
