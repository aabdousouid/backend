package com.bezkoder.spring.security.postgresql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long applications;
    private long newApplications;
    private long interviewsConducted;
    private long interviewsThisWeek;
    private long newJobPostings;
    private long jobPostingsToday;
    private double offerAcceptanceRate;
    private double acceptanceRateDelta; // +3% since last month
}
