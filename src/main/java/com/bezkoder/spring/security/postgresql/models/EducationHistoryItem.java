package com.bezkoder.spring.security.postgresql.models;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationHistoryItem {
    private String degree;
    private String school;
    private String duration;
}
