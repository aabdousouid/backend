package com.bezkoder.spring.security.postgresql.services;


import com.bezkoder.spring.security.postgresql.models.Complaint;
import com.bezkoder.spring.security.postgresql.models.ComplaintStatus;
import com.bezkoder.spring.security.postgresql.models.ComplaintType;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.repository.ComplaintRepository;
import com.bezkoder.spring.security.postgresql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService{

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    @Override
    public Complaint addComplaint(Complaint complaint) {

        complaint.setComplaintStatus(ComplaintStatus.OPEN);
        complaint.setCreatedAt(new Date());
        return this.complaintRepository.save(complaint);
    }

    @Override
    public Complaint findComplaintById(Long complaintId) {


        return this.complaintRepository.findById(complaintId).orElseThrow(()->new RuntimeException("Complaint do not exist"));
    }

    @Override
    public void DeleteComplaintById(Long complaintId) {
        this.complaintRepository.deleteById(complaintId);
    }

    @Override
    public List<Complaint> findAllComplaintsByUser(Long userId) {
        return this.complaintRepository.findByUserId(userId);
    }

    @Override
    public Complaint UpdateStatus(Long complaintId, String status) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(()-> new RuntimeException("Complaint Not found"));

        ComplaintStatus currentStatus = complaint.getComplaintStatus();
        ComplaintStatus newStatus = ComplaintStatus.valueOf(status.toUpperCase());

        if(currentStatus == ComplaintStatus.RESOLVED || currentStatus == ComplaintStatus.REJECTED || currentStatus == ComplaintStatus.ABANDONED){
            throw new IllegalStateException("Cannot update Status from "+currentStatus+" to "+newStatus);
        }

        complaint.setComplaintStatus(newStatus);
        return this.complaintRepository.save(complaint);
    }

    @Override
    public List<Complaint> findAllComplaints(){
        return this.complaintRepository.findAll();
    }
}
