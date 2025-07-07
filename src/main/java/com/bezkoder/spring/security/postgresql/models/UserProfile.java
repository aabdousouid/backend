package com.bezkoder.spring.security.postgresql.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    private String title;
    private String phoneNumber;

    @Column(length = 500)
    private String address;

    @ElementCollection
    private List<String> links;

    @Column(length = 1000)
    private String summary;

    @ElementCollection
    private List<String> skills;

    private Integer experienceYears;

    @ElementCollection
    private List<String> languages;

    @ElementCollection
    private List<String> certifications;

    private String cvFilePath;

    @ElementCollection
    @CollectionTable(name = "user_education_history", joinColumns = @JoinColumn(name = "profile_id"))
    private List<EducationHistoryItem> education;

    @ElementCollection
    @CollectionTable(name = "user_work_history", joinColumns = @JoinColumn(name = "profile_id"))
    private List<WorkHistoryItem> workHistory;
}
