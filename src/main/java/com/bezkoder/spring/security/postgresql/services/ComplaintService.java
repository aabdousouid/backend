package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Complaint;
import com.bezkoder.spring.security.postgresql.models.ComplaintType;

import java.util.List;

public interface ComplaintService {
    Complaint addComplaint(Complaint complaint);

    Complaint findComplaintById(Long complaintId);

    void DeleteComplaintById(Long complaintId);

    List<Complaint>findAllComplaintsByUser(Long userId);

    Complaint UpdateStatus(Long complaintId, String status);

    List<Complaint> findAllComplaints();


}
