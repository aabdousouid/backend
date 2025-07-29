package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.Interview;
import com.bezkoder.spring.security.postgresql.models.InterviewStatus;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import com.bezkoder.spring.security.postgresql.repository.InterviewRepository;
import com.bezkoder.spring.security.postgresql.services.InterviewServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewServiceImpl interviewService;
    private final InterviewRepository interviewRepository;
    private final ApplicationRepositroy applicationRepositroy;

   /* @PostMapping("/{applicationId}/interviews")
    public ResponseEntity<Interview> addInterview(@PathVariable Long applicationId, @RequestBody Interview interview) {
        Application application = applicationRepositroy.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        interview.setApplication(application);
        Interview savedInterview = interviewRepository.save(interview);
        return ResponseEntity.ok(savedInterview);
    }*/

    @PostMapping("/{applicationId}/interviews")
    public ResponseEntity<Interview> addInterview(@PathVariable Long applicationId, @RequestBody Interview interview) {
        return ResponseEntity.ok(interviewService.addInterview(interview,applicationId));
    }

    @GetMapping("/getApplicationInterviews/{applicationId}")
    public ResponseEntity<?> getUserApplications(@PathVariable Long applicationId) {
        return ResponseEntity.ok(this.interviewService.findApplicationInterview(applicationId));
    }

    @DeleteMapping("/deleteInterview/{interviewId}")
    public void deleteById(@PathVariable Long interviewId){
        this.interviewService.deleteById(interviewId);
    }


    @PutMapping("/updateInterview/{interviewId}")
    public ResponseEntity<Interview> updateInterview(@PathVariable Long interviewId,@RequestBody Interview interview){

        return ResponseEntity.ok(this.interviewService.updateInterview(interviewId,interview));
    }

    @GetMapping("/getApplicationByInterviewId/{interviewId}")
    public ResponseEntity<Application> getApplicationByInterviewId(@PathVariable Long interviewId){
        return ResponseEntity.ok(this.interviewService.findApplicationByInterviewId(interviewId));
    }


    @PutMapping("/updateInterviewStatus/{interviewId}")
    public ResponseEntity<Interview> updateInterviewStatus(@PathVariable Long interviewId, @RequestBody String status){
        return ResponseEntity.ok(this.interviewService.updateStatus(interviewId,status));
    }

}
