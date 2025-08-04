package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.dto.UpcomingInterviewDTO;
import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import com.bezkoder.spring.security.postgresql.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService{
    private final InterviewRepository interviewRepository;
    private final ApplicationRepositroy applicationRepositroy;
    private final EmailServiceImpl emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

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
        Interview savedInterview= interviewRepository.save(interview);

        Application app = interview.getApplication();
        User user = app.getUser();

// Create & save notification
        Notifications notif = new Notifications();
        notif.setRecipient(user);
        notif.setMessage("A new interview (" + interview.getInterviewTest() + ") was scheduled for your application to: " + app.getJob().getTitle());
        notif.setType(NotificationType.INTERVIEW_SCHEDULED); // Update your enum as needed
        notif.setCreatedAt(java.time.LocalDateTime.now());
        notif.setIsRead(false);
        notificationService.save(notif);


        return savedInterview;

    }


    public List<UpcomingInterviewDTO> getUpcomingInterviews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        List<Interview> interviews = interviewRepository
                .findByScheduledDateAfterOrderByScheduledDateAsc(new Date(), pageable);
        //System.out.println(interviews);
        return interviews.stream().map(i -> {
            Application app = i.getApplication();
            User user = app.getUser();
            Job job = app.getJob();

            String candidateName = user.getFirstname() + " " + user.getLastname();
            String jobTitle = job.getTitle();
            Date scheduledDate = i.getScheduledDate();
            String status = i.getStatus() != null ? i.getStatus().toString() : "";

            return new UpcomingInterviewDTO(candidateName, jobTitle, scheduledDate, status);
        }).collect(Collectors.toList());
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
            /*Notifications notif = new Notifications();
            notif.setRecipient(application.getUser());
            notif.setMessage("Interview (" + updatedInterview.getInterviewTest() + ") status updated to: " + updatedInterview.getStatus());
            notif.setType(NotificationType.APPLICATION_STATUS_CHANGED); // Use enum value
            notif.setCreatedAt(java.time.LocalDateTime.now());
            notif.setIsRead(false);
            notificationService.save(notif);*/

            // Example notification message
            String notifMsg = "Your interview has been updated!";

            // Build notification payload (could be a DTO or Map or String)
            Map<String, Object> notif = new HashMap<>();
            notif.put("message", notifMsg);
            notif.put("timestamp", System.currentTimeMillis());
            //System.out.println(interview.getApplication().getUser().getUsername());
            // Send notification to user (assume interview.getApplication().getUser().getUsername())
            String payload = "{\"message\": \"Your interview has been updated!\", \"timestamp\": " + System.currentTimeMillis() + "}";
            messagingTemplate.convertAndSendToUser(application.getUser().getUsername(), "/queue/notifications", payload);



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
