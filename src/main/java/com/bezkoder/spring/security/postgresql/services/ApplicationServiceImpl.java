package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.ApplicationStatus;
import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {


    private final EmailServiceImpl emailService;

    private final ApplicationRepositroy applicationRepositroy;
    @Override
    public Application applyJob(Job job, User user,Application application) {
        //Application application = new Application();

        application.setUser(user);
        application.setJob(job);
        application.setStatus(ApplicationStatus.valueOf("PENDING"));
        application.setAppliedDate(LocalDateTime.now());

        emailService.sendApplicationEmail(user.getEmail());
        //notification to the admin

        return applicationRepositroy.save(application);
    }

    @Override
    public Application updateStatus(Long applicationId, String status) {
        Application application = applicationRepositroy.findById(applicationId).orElseThrow();
        application.setStatus(ApplicationStatus.valueOf(status));


        Application updatedApp =  applicationRepositroy.save(application);

        return updatedApp;


    }
}
