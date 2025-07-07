package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Job;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface JobService {

    public Job addJob(Job job) throws IOException;

    public List<Job> getAllJobs();
    public Optional<Job> getByIdJob(Long id);


    public void DeleteByIdJob(Long id);

    public  Job updateJob(Long id, Job newJob);



}
