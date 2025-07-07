package com.bezkoder.spring.security.postgresql.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewId;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    private LocalDateTime scheduledDate;

    private String location;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status = InterviewStatus.SCHEDULED;
}
