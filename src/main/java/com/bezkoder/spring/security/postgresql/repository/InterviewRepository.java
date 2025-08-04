package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.dto.UpcomingInterviewDTO;
import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.Interview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview,Long> {

    long countByScheduledDateAfter(Date date);


    // In InterviewRepository.java
    List<Interview> findByScheduledDateAfterOrderByScheduledDateAsc(Date date, Pageable pageable);


}
