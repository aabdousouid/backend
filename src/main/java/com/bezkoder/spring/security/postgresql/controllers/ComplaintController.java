package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.Complaint;
import com.bezkoder.spring.security.postgresql.services.ComplaintServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintController {
    private final ComplaintServiceImpl complaintService;

    @GetMapping("/getComplaintsByUser/{userId}")
    public ResponseEntity<?> getComplaintsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(this.complaintService.findAllComplaintsByUser(userId));
    }

    @GetMapping("/getComplaints")
    public ResponseEntity<?> getComplaints(){
        return ResponseEntity.ok(this.complaintService.findAllComplaints());
    }

    @PostMapping("/addComplaints")
    public ResponseEntity<Complaint> addComplaints(@RequestBody Complaint complaint){
        return ResponseEntity.ok(this.complaintService.addComplaint(complaint));
    }

    @PutMapping("/updateComplaintStatus/{complaintId}")
    public ResponseEntity<Complaint> updateComplaintStatus(@PathVariable Long complaintId, @RequestBody String status){
        return ResponseEntity.ok(this.complaintService.UpdateStatus(complaintId,status));
    }

    @DeleteMapping("/deleteComplaint/{complaintId}")
    public ResponseEntity<?> deleteComplaint(@PathVariable Long complaintId){
        this.complaintService.DeleteComplaintById(complaintId);
        return ResponseEntity.ok().build();
    }

}
