package com.bezkoder.spring.security.postgresql.dto;

import com.bezkoder.spring.security.postgresql.models.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopAppliedJobsDTO {
    private String title;
    private JobType type;
    private Integer applications;
    private Double matchingScore;
}
