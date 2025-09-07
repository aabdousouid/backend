package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.dto.TopAppliedJobsDTO;
import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.JobType;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import com.bezkoder.spring.security.postgresql.repository.JobRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class JobServiceImpl implements JobService{
    private final JobRepository jobRepository;
    private final ApplicationRepositroy applicationRepositroy;

    @Override
    public Job addJob(Job job) throws IOException {

        return jobRepository.save(job);

    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public Optional<Job> getByIdJob(Long id) {
        return this.jobRepository.findById(id);

    }

    @Override
    public void DeleteByIdJob(Long id) {
        jobRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Job updateJob(Long id, Job newJob) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found: " + id));

        job.setTitle(newJob.getTitle());
        job.setDescription(newJob.getDescription());
        job.setCompany(newJob.getCompany());
        job.setRequirements(newJob.getRequirements());
        job.setLocation(newJob.getLocation());
        job.setJobType(newJob.getJobType());
        job.setIsActive(newJob.getIsActive());
        job.setExperience(newJob.getExperience());
        job.setIsUrgent(newJob.getIsUrgent());
        job.setSkills(newJob.getSkills());

        // IMPORTANT FIX: save the managed entity, not newJob
        return jobRepository.save(job);
    }

    @Override
    public List<TopAppliedJobsDTO> getTopAppliedJobs() {

        int limit = 5;

        // If you can, prefer a lightweight fetch (e.g., only ids) or use a repo that fetches just what you need.
        List<Application> apps = applicationRepositroy.findAll(); // might be big!

        Map<Job, Long> counts = apps.stream()
                .filter(a -> a.getJob() != null)
                .collect(Collectors.groupingBy(Application::getJob, Collectors.counting()));

        List<TopAppliedJobsDTO> dtos = counts.entrySet().stream()
                .sorted(Map.Entry.<Job, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new TopAppliedJobsDTO(
                        e.getKey().getTitle(),
                        e.getKey().getJobType(),
                        e.getValue().intValue(),
                        0.0
                ))
                .toList();

        int max = dtos.stream().mapToInt(TopAppliedJobsDTO::getApplications).max().orElse(1);
        dtos.forEach(d -> d.setMatchingScore(
                Math.round((d.getApplications() * 100.0 / max) * 10.0) / 10.0
        ));

        return dtos;
    }

    @Override
    @Transactional
    public Job setActive(Long id, boolean active) {
        // Option A: JPQL update (fast) then reload
        int updated = jobRepository.updateActive(id, active);
        if (updated == 0) {
            throw new EntityNotFoundException("Job not found: " + id);
        }
        return jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found after update: " + id));

        // Option B (alternate): load entity, set flag, save.
        // Job job = jobRepository.findById(id)
        //        .orElseThrow(() -> new EntityNotFoundException("Job not found: " + id));
        // job.setIsActive(active);
        // return jobRepository.save(job);
    }

    @Override
    public Job disableJob(Long id) {
        return setActive(id, false);
    }

    @Override
    public Job enableJob(Long id) {
        return setActive(id, true);
    }
}
