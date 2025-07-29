package com.bezkoder.spring.security.postgresql.dto;

import lombok.Data;

@Data
public class ScoreResponseDto {
    private Integer matchingScore;
    private String comment;
}
