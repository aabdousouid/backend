package com.bezkoder.spring.security.postgresql.dto;

import lombok.Data;

@Data
public class QuizResultRequest {
    private Long jobId;
    private Long userId;
    private double quizScore;
    private double matchScore;
    private String status;

    // Getters and setters

}