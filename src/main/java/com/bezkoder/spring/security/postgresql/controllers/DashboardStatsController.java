package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.dto.DashboardStatsDTO;
import com.bezkoder.spring.security.postgresql.dto.PlatformActivityResponse;
import com.bezkoder.spring.security.postgresql.models.Application;
import com.bezkoder.spring.security.postgresql.repository.ApplicationRepositroy;
import com.bezkoder.spring.security.postgresql.repository.JobRepository;
import com.bezkoder.spring.security.postgresql.services.DashboardStatsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboardStats")
@AllArgsConstructor
public class DashboardStatsController {
    private final DashboardStatsServiceImpl dashboardStatsService;
    private final JobRepository jobRepository;
    private final ApplicationRepositroy applicationRepositroy;

    @GetMapping("/user/{userId}")
    public DashboardStatsDTO getUserStats(@PathVariable Long userId) {
        return dashboardStatsService.getUserStats(userId);
    }
    @GetMapping("/")
    public DashboardStatsDTO getStats() {
        return dashboardStatsService.getStats();
    }


    /*@GetMapping("/activity")
    public PlatformActivityResponse getActivity(@RequestParam(defaultValue = "quarter") String period) {
        return dashboardStatsService.getPlatformActivity(period);
    }*/
    @GetMapping("/activity-chart")
    public Map<String, Object> getActivityChart(
            @RequestParam(required = false) Integer year) {

        int y = (year != null) ? year : java.time.LocalDate.now().getYear();
        var labels = java.util.List.of("Q1","Q2","Q3","Q4");

        java.util.List<Long> jobPostings = new java.util.ArrayList<>(4);
        java.util.List<Long> applications = new java.util.ArrayList<>(4);
        java.util.List<Long> hires = new java.util.ArrayList<>(4);

        for (int q = 1; q <= 4; q++) {
            var start = java.time.LocalDate.of(y, (q - 1) * 3 + 1, 1).atStartOfDay();
            var end   = start.plusMonths(3);

            // If you only have "...After(...)" methods, use diffAfter(); else replace with Between() directly.
            long jp  = diffAfter(jobRepository::countByPostedDateAfter, start, end);
            long app = diffAfter(applicationRepositroy::countByAppliedDateAfter, start, end);
            long hr  = diffAfter(dt -> applicationRepositroy.countByStatusAndAppliedDateAfter(
                    com.bezkoder.spring.security.postgresql.models.ApplicationStatus.HIRED, dt), start, end);

            jobPostings.add(jp);
            applications.add(app);
            hires.add(hr);
        }

        return java.util.Map.of(
                "labels", labels,
                "datasets", java.util.List.of(
                        java.util.Map.of("label", "Job Postings", "data", jobPostings),
                        java.util.Map.of("label", "Candidate Applications", "data", applications),
                        java.util.Map.of("label", "Hires", "data", hires)
                )
        );
    }

    private long diffAfter(java.util.function.Function<java.time.LocalDateTime, Long> afterCounter,
                           java.time.LocalDateTime start, java.time.LocalDateTime end) {
        long a = afterCounter.apply(start);
        long b = afterCounter.apply(end);
        return Math.max(0, a - b);
    }
}
