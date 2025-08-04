package com.bezkoder.spring.security.postgresql.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResults {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double score;

    private String status;

    private double matchScore;

    @OneToOne
    @JoinColumn(name = "application_id", referencedColumnName = "applicationId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    private LocalDateTime submittedAt;
}
