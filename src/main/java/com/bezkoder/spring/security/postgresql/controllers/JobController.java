package com.bezkoder.spring.security.postgresql.controllers;
import com.bezkoder.spring.security.postgresql.dto.TopAppliedJobsDTO;
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
import java.util.ArrayList;
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


    @PutMapping("/updateJob/{jobId}")
    public ResponseEntity<Job> updateJob(@Valid @RequestBody Job job,@PathVariable Long jobId){
        return ResponseEntity.ok(this.jobService.updateJob(jobId,job));
    }

    @GetMapping("/testing")
    public String Testing(){
        return "testing";
    }
    @GetMapping("/getJobs")
    public List<Job> getAllJobs () {
        if(this.jobService.getAllJobs().isEmpty()){
            return new ArrayList<Job>();
        }

        else return this.jobService.getAllJobs();
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

    @DeleteMapping("/deleteJob/{jobId}")
    public void deleteJob(@PathVariable Long jobId){
        this.jobService.DeleteByIdJob(jobId);
    }

    @GetMapping("/getTopAppliedJobs")
    public List<TopAppliedJobsDTO> getTopAppliedJobs(@RequestParam(defaultValue = "5") int limit){
        List<TopAppliedJobsDTO> all = jobService.getTopAppliedJobs();
        return all.stream().limit(limit).toList();
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Job> disableJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.disableJob(id));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Job> enableJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.enableJob(id));
    }
}
