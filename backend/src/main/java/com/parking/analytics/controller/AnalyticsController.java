package com.parking.analytics.controller;

import com.parking.analytics.dto.AnalyticsResponse;
import com.parking.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Returns dashboard statistics for the manager:
     * - Occupancy rate (today's reserved spots / total spots)
     * - No-show rate (no_shows / total reservations)
     * - Electric charger usage rate
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsResponse> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }
}
