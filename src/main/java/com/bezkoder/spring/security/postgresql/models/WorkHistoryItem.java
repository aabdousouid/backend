package com.bezkoder.spring.security.postgresql.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkHistoryItem {
    private String company;
    private String title;
    private String duration;
    private String description;
}

