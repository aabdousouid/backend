package com.bezkoder.spring.security.postgresql.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewId;

    @ManyToOne
    @JoinColumn(name = "application_id")
    @JsonBackReference
    private Application application;

    @Column
    private Date scheduledDate;

    @Column
    private Date scheduledHour;



    @Column
    private String location;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status = InterviewStatus.SCHEDULED;


    private int duration;

    @Enumerated(EnumType.STRING)
    private InterviewType interviewType = InterviewType.ONSITE;


    @Enumerated(EnumType.STRING)
    private InterviewTest interviewTest = InterviewTest.TECHNIQUE;

    private String interviewerEmail;

    private String interviewerName;

    @Column(nullable = true)
    private String meetingLink;



}
