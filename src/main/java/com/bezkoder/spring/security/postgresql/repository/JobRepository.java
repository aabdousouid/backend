package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    long countByPostedDateAfter(LocalDateTime date);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.isActive = :active WHERE j.jobId = :id")
    int updateActive(@Param("id") Long id, @Param("active") boolean active);


}
