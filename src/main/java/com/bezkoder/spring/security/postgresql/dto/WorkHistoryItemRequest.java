package com.bezkoder.spring.security.postgresql.dto;

import lombok.Data;

@Data
public class WorkHistoryItemRequest {
    private String company;
    private String title;
    private String duration;
    private String description;

}
