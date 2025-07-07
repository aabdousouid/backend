package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.User;

public interface ApplicationService {

    public Application applyJob(Job job, User user,Application application);

    public Application updateStatus(Long applicationId,String status);

}
