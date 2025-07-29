package com.bezkoder.spring.security.postgresql.models;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "complaints")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complaintId;

    private String Title;

    private String Description;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus complaintStatus;

    @Enumerated(EnumType.STRING)
    private ComplaintType complaintType;


    @ManyToOne
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    private Date createdAt;

}
