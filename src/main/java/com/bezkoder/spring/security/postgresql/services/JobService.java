package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.dto.TopAppliedJobsDTO;
import com.bezkoder.spring.security.postgresql.models.Job;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface JobService {

    public Job addJob(Job job) throws IOException;

    public List<Job> getAllJobs();
    public Optional<Job> getByIdJob(Long id);


    public void DeleteByIdJob(Long id);

    public  Job updateJob(Long id, Job newJob);

    public List<TopAppliedJobsDTO> getTopAppliedJobs();


    Job disableJob(Long id);
    Job enableJob(Long id);
    Job setActive(Long id, boolean active);

}

