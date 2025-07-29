package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.models.ApplicationStatus;
import com.bezkoder.spring.security.postgresql.models.Job;
import com.bezkoder.spring.security.postgresql.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepositroy extends JpaRepository<Application, Long>
{
    List<Application> findByUserId(Long userId);

    @Override
    void deleteById(Long applicationId);

    Optional<Application> findByUserAndJob(User user, Job job);


}
