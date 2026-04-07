package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.entity.SecurityLog;
import com.klef.loanflowbackend.service.SecurityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security-logs")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class SecurityLogController {

    private final SecurityLogService securityLogService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SecurityLog>>> getAll() {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Security logs retrieved", securityLogService.getAll()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

