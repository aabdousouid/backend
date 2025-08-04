package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.dto.DashboardStatsDTO;
import com.bezkoder.spring.security.postgresql.services.DashboardStatsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboardStats")
@AllArgsConstructor
public class DashboardStatsController {
    private final DashboardStatsServiceImpl dashboardStatsService;

    @GetMapping("/")
    public DashboardStatsDTO getStats() {
        return dashboardStatsService.getStats();
    }
}
