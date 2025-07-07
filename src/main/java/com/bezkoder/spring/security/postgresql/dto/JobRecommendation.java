package com.bezkoder.spring.security.postgresql.dto;


import lombok.Data;

import java.util.List;

@Data
public class JobRecommendation {
    private String title;
    private String description;
    private int matchPercentage;
    private String experienceLevel;
    //private String salaryRange;
    private List<String> requiredSkills;
    private String justification; // optional reasoning from the LLM

}
