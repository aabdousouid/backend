package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.ApplicationStatus;
import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.User;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ApplicationService {

    Application applyJob(Job job, User user,Application application);

    Application updateStatus(Long applicationId,String status);

    Application findById(Long applicationId);

    List<Application> findAllApplications();

    List<Application> findUserApplications(Long userId);

    void deleteById (Long applicationId);

    Application addComment(Long applicationId, String comments);

    //Application updateStatus(Long applicationId, ApplicationStatus status, Date lastUpdated);

    Optional<Boolean> findByUserAndJob(Long userId,Long jobId);



}
