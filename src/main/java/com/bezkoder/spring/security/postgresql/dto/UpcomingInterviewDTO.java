package com.bezkoder.spring.security.postgresql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


public class UpcomingInterviewDTO {
    private String candidateName;
    private String jobTitle;
    private Date scheduledDate;
    private String status;


    public UpcomingInterviewDTO(String candidateName, String jobTitle, Date scheduledDate, String status) {
        this.candidateName = candidateName;
        this.jobTitle = jobTitle;
        this.scheduledDate = scheduledDate;
        this.status = status;
    }

    // Getters
    public String getCandidateName() { return candidateName; }
    public String getJobTitle() { return jobTitle; }
    public Date getScheduledDate() { return scheduledDate; }
    public String getStatus() { return status; }


    // Getters
    public void setCandidateName(String candidateName) { this.candidateName=candidateName;}
    public void setJobTitle(String jobTitle) { this.jobTitle=jobTitle; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate=scheduledDate; }
    public void setStatus(String status) { this.status=status; }
}
