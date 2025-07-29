package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.Interview;
import com.bezkoder.spring.security.postgresql.models.InterviewStatus;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import com.bezkoder.spring.security.postgresql.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService{
    private final InterviewRepository interviewRepository;
    private final ApplicationRepositroy applicationRepositroy;
    private final EmailServiceImpl emailService;

    @Override
    public Interview addInterview(Interview interview, Long applicationId) {
        Application application = applicationRepositroy.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        List<Interview> interviews = application.getInterviews();

        // Check if this type already exists
        boolean alreadyExists = interviews.stream()
                .anyMatch(existing -> existing.getInterviewTest().equals(interview.getInterviewTest()));

        if (alreadyExists) {
            throw new RuntimeException("An interview of type '" + interview.getInterviewTest() + "' has already been scheduled.");
        }

        // Check if more than 2 interviews already exist (extra safety) better safe than sorry biaacth
        if (interviews.size() >= 2) {
            throw new RuntimeException("Only two interviews are allowed per application.");
        }

        interview.setApplication(application);
        this.emailService.sendInterviewEmail(application.getUser().getEmail(),interview);
        return interviewRepository.save(interview);

    }



    @Override
    public Interview findById(Long interviewId) {
        return null;
    }

    @Override
    public void  deleteById(Long interviewId) {
        this.interviewRepository.deleteById(interviewId);
    }


    @Override
    public List<Interview> findApplicationInterview(Long applicationId) {

        List<Interview> interviews = this.applicationRepositroy.findById(applicationId).get().getInterviews();

        if(interviews.isEmpty()){
            throw new RuntimeException("No Interviews found");
        }
        return interviews;
    }

    public Application findApplicationByInterviewId(Long interviewId) {
        List<Application> applications = applicationRepositroy.findAll();

        for (Application application : applications) {
            for (Interview interview : application.getInterviews()) {
                if (interview.getInterviewId().equals(interviewId)) {
                    return application;
                }
            }
        }

        throw new RuntimeException("No Application found for Interview ID: " + interviewId);
    }

    @Override
    public Interview updateInterview(Long interviewId, Interview interview) {
        Interview oldInterview = this.interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        Application application = this.findApplicationByInterviewId(interviewId);
       // Application application = this.applicationRepositroy.findById(interview.getApplication().getApplicationId())
          //      .orElseThrow(() -> new RuntimeException("Application not found"));

        // Save previous status to compare later
        InterviewStatus oldStatus = oldInterview.getStatus();

        // Update fields
        oldInterview.setInterviewerName(interview.getInterviewerName());
        oldInterview.setInterviewerEmail(interview.getInterviewerEmail());
        oldInterview.setScheduledDate(interview.getScheduledDate());
        oldInterview.setScheduledHour(interview.getScheduledHour());
        oldInterview.setInterviewTest(interview.getInterviewTest());
        oldInterview.setInterviewType(interview.getInterviewType());
        oldInterview.setLocation(interview.getLocation());
        oldInterview.setNotes(interview.getNotes());
        oldInterview.setMeetingLink(interview.getMeetingLink());
        oldInterview.setStatus(interview.getStatus());
        oldInterview.setDuration(interview.getDuration());

        Interview updatedInterview = this.interviewRepository.save(oldInterview);

        if (updatedInterview != null) {
            InterviewStatus newStatus = interview.getStatus(); // Use interview (the incoming object) for new status
            String toEmail = application.getUser().getEmail();

            if (!newStatus.equals(oldStatus)) {
                switch (newStatus) {
                    case COMPLETED -> this.emailService.sendCompletedInterview(toEmail, updatedInterview);
                    case CANCELLED -> this.emailService.sendCanceledInterview(toEmail, updatedInterview);
                    case RESCHEDULED -> this.emailService.sendRescheduledInterview(toEmail, updatedInterview);
                    case CONFIRMED -> this.emailService.sendConfirmedInterview(toEmail, updatedInterview);
                    default -> this.emailService.sendUpdateInterview(toEmail, updatedInterview);
                }
            } else {
                // Status unchanged, but other details may have changed
                this.emailService.sendUpdateInterview(toEmail, updatedInterview);
            }
        }

        return updatedInterview;
    }

    @Override
    public Interview updateStatus(Long interviewId, String status) {
        Interview interview = this.interviewRepository.findById(interviewId).orElseThrow(()-> new RuntimeException("Interview not found"));
        interview.setStatus(InterviewStatus.valueOf(status));
        return this.interviewRepository.save(interview);
    }

}
