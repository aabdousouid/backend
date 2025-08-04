package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.dto.DashboardStatsDTO;
import com.bezkoder.spring.security.postgresql.models.ApplicationStatus;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import com.bezkoder.spring.security.postgresql.repository.InterviewRepository;
import com.bezkoder.spring.security.postgresql.repository.JobRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@AllArgsConstructor
public class DashboardStatsServiceImpl {
    private ApplicationRepositroy applicationRepository;
    private InterviewRepository interviewRepository;
    private JobRepository jobRepository;

    public DashboardStatsDTO getStats() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime today = LocalDate.now().atStartOfDay();
        Date todayDate = java.sql.Date.valueOf(LocalDate.now());

        DashboardStatsDTO dto = new DashboardStatsDTO();

        // Applications this month and new today
        dto.setApplications(applicationRepository.countByAppliedDateAfter(monthStart));
        dto.setNewApplications(applicationRepository.countByAppliedDateAfter(today));

        // Interviews conducted (scheduled today or after)
        dto.setInterviewsConducted(interviewRepository.countByScheduledDateAfter(java.sql.Date.valueOf(monthStart.toLocalDate())));
        dto.setInterviewsThisWeek(interviewRepository.countByScheduledDateAfter(todayDate)); // or set proper week logic

        // New job postings
        dto.setNewJobPostings(jobRepository.countByPostedDateAfter(monthStart));
        dto.setJobPostingsToday(jobRepository.countByPostedDateAfter(today));

        // Offer acceptance rate (Assume HIRED means offer accepted, OFFERED means offer made)
        long offersThisMonth = applicationRepository.countByStatusAndAppliedDateAfter(ApplicationStatus.APPROVED, monthStart);
        long acceptedThisMonth = applicationRepository.countByStatusAndAppliedDateAfter(ApplicationStatus.HIRED, monthStart);
        dto.setOfferAcceptanceRate(offersThisMonth > 0 ? (100.0 * acceptedThisMonth / offersThisMonth) : 0.0);

        // You can add more KPIs as needed

        return dto;
    }
}
