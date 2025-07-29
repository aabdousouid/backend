package com.bezkoder.spring.security.postgresql.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
   //@JsonBackReference
    private Job job;

    @Column(nullable = false)
    private LocalDateTime appliedDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column
    private List<String> adminComments;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Interview> interviews;


    @Column
    private String cvFileName; // or cvUrl if you store a full URL

    @Column(columnDefinition = "TEXT")
    private String extractedSkills; // JSON array as string

    @Column(columnDefinition = "TEXT")
    private String cvSummary;


    @Column
    private Double matchingScore;

    @Column(length = 1000)
    private String matchingComment;
}
