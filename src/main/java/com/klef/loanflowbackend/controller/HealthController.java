package com.klef.loanflowbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.klef.loanflowbackend.dto.ApiResponse;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "https://loanflow-frontend.netlify.app"})
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Backend is healthy", "LoanFlow Backend Running")
        );
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> status() {
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Status OK", "Production")
        );
    }
}

