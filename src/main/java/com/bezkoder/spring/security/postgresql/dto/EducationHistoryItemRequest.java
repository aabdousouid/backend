package com.bezkoder.spring.security.postgresql.dto;

import lombok.Data;

@Data
public class EducationHistoryItemRequest {

    private String degree;
    private String school;
    private String duration;
}
