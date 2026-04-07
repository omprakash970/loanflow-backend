package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.dto.BorrowerSummaryDTO;
import com.klef.loanflowbackend.service.BorrowerSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerSummaryService borrowerSummaryService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('LENDER') or hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<ApiResponse<List<BorrowerSummaryDTO>>> getBorrowersSummary() {
        try {
            List<BorrowerSummaryDTO> list = borrowerSummaryService.getAllBorrowersSummary();
            return ResponseEntity.ok(new ApiResponse<>(true, "Borrowers retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

