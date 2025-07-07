package com.bezkoder.spring.security.postgresql.dto;

import lombok.Data;

import java.util.List;

@Data
public class CVAnalysis {
    private List<String> skills;
    private List<String> experience;
    private List<String> education;
    private List<String> certifications;
    private List<String> languages;
    private String summary;
}
