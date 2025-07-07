package com.bezkoder.spring.security.postgresql.controllers;
import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.services.ApplicationService;
import com.bezkoder.spring.security.postgresql.services.JobService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/job")
@AllArgsConstructor
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    @PostMapping("/addJob")
    public ResponseEntity<Job> addJob(@Valid @RequestBody Job job) throws IOException {
        return ResponseEntity.ok(jobService.addJob(job));
    }

    @GetMapping("/testing")
    public String Testing(){
        return "testing";
    }
    @GetMapping("/getJobs")
    public List<Job> getAllJobs () {

        return this.jobService.getAllJobs();
    }

    @GetMapping("/getJob/{id}")
    public Optional<Job> getJobById(@PathVariable Long id) {
        return this.jobService.getByIdJob(id);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    /*@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }*/


    @PostMapping("/apply/{jobId}")
    public ResponseEntity<Application> apply(@PathVariable Long jobId, @AuthenticationPrincipal User user,@RequestBody Application application) {
        Job job = new Job();
        job.setJobId(jobId); // Assuming a lookup is made here
        return ResponseEntity.ok(applicationService.applyJob(job, user,application));
    }


    @PatchMapping("/applications/{appId}/status")
    public ResponseEntity<Application> updateApplicationStatus(@PathVariable Long appId, @RequestParam String status) {
        return ResponseEntity.ok(applicationService.updateStatus(appId, status));
    }
}
