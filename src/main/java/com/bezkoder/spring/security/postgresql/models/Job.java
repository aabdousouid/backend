package com.bezkoder.spring.security.postgresql.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(length = 2000)
    private String description;

    private String location;

    @Enumerated(EnumType.STRING)
    private JobType jobType;


    private String experience;

   // private String salary;

    @Column(length = 2000)
    private String requirements;

    private ArrayList<String> skills;

    @Column(nullable = false)
    private LocalDateTime postedDate = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean isActive = true;



    @Column(nullable = false)
    private Boolean isUrgent = false;



    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonManagedReference
    @JsonIgnore
    private List<Application> applications;
}


