package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.dto.RiskReportDTO;
import com.klef.loanflowbackend.service.RiskReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/risk-reports")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class RiskReportController {

    private final RiskReportService riskReportService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RiskReportDTO>>> getAllRiskReports() {
        try {
            List<RiskReportDTO> reports = riskReportService.getAllRiskReports();
            return ResponseEntity.ok(new ApiResponse<>(true, "Risk reports retrieved", reports));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

