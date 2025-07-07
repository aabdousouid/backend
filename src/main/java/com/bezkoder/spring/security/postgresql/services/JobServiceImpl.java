package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.JobType;
import com.bezkoder.spring.security.postgresql.repository.JobRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class JobServiceImpl implements JobService{
    private final JobRepository jobRepository;

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
    public Job updateJob(Long id, Job newJob) {
        Job job = jobRepository.findById(id).orElseThrow();
        job.setTitle(newJob.getTitle());
        job.setDescription(newJob.getDescription());
        job.setCompany(newJob.getCompany());
        job.setRequirements(newJob.getRequirements());
        job.setLocation(newJob.getLocation());
        job.setJobType(newJob.getJobType());
        job.setIsActive(newJob.getIsActive());
        return jobRepository.save(newJob);
    }
}
