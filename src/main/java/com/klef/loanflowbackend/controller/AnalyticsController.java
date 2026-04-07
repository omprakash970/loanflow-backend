package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.dto.AnalyticsDTO;
import com.klef.loanflowbackend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get risk score distribution by bands
     */
    @GetMapping("/risk-distribution")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO.RiskBand>>> getRiskDistribution() {
        List<AnalyticsDTO.RiskBand> data = analyticsService.getRiskScoreDistribution();
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Risk distribution fetched successfully",
            data
        ));
    }

    /**
     * Get portfolio exposure breakdown
     */
    @GetMapping("/portfolio-exposure")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO.PortfolioExposure>>> getPortfolioExposure() {
        List<AnalyticsDTO.PortfolioExposure> data = analyticsService.getPortfolioExposure();
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Portfolio exposure fetched successfully",
            data
        ));
    }

    /**
     * Get quarterly loan trends
     */
    @GetMapping("/quarterly-trends")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO.QuarterlyTrend>>> getQuarterlyTrends() {
        List<AnalyticsDTO.QuarterlyTrend> data = analyticsService.getQuarterlyTrends();
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Quarterly trends fetched successfully",
            data
        ));
    }

    /**
     * Get payment completion metrics
     */
    @GetMapping("/payment-metrics")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentMetrics() {
        Map<String, Object> data = analyticsService.getPaymentMetrics();
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Payment metrics fetched successfully",
            data
        ));
    }

    /**
     * Get complete analytics summary (all data)
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalyticsSummary() {
        Map<String, Object> data = analyticsService.getAnalyticsSummary();
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Analytics summary fetched successfully",
            data
        ));
    }
}
