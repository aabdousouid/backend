package com.bezkoder.spring.security.postgresql.models;

import java.util.List;

public class RecommendationRequest {
    private String cvText;
    private List<Job> jobs;



    public String getCvText() {
        return cvText;
    }
    public void setCvText(String cvText) {
        this.cvText = cvText;
    }
    public List<Job> getJobs() {
        return jobs;
    }
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
