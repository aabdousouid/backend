package com.bezkoder.spring.security.postgresql.dto;
import com.bezkoder.spring.security.postgresql.models.EducationHistoryItem;
import com.bezkoder.spring.security.postgresql.models.WorkHistoryItem;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
@Data
public class UserProfileRequest {
    private Long userId;
    private String title;
    private String phoneNumber;
    private String address;
    private List<String> links;
    private String summary;
    private List<String> skills;
    private Integer experienceYears;
    private List<String> languages;
    private List<String> certifications;
    private List<EducationHistoryItem> education;
    private String cvFilePath;
    private List<WorkHistoryItem> workHistory;
}
