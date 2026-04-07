package com.klef.loanflowbackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.dto.LoanDTO;
import com.klef.loanflowbackend.entity.User;
import com.klef.loanflowbackend.repository.UserRepository;
import com.klef.loanflowbackend.service.LoanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    /**
     * Borrower applies for a new loan
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<LoanDTO>> applyForLoan(
            Authentication authentication,
            @RequestBody LoanDTO loanDTO) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            LoanDTO savedLoan = loanService.applyForLoan(user.getId().toString(), loanDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Loan application submitted", savedLoan));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get borrower's own loans
     */
    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<List<LoanDTO>>> getMyLoans(
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<LoanDTO> loans = loanService.getBorrowerLoans(user.getId().toString());
            return ResponseEntity.ok(new ApiResponse<>(true, "Loans retrieved", loans));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get all pending loans (Admin only)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanDTO>>> getPendingLoans() {
        try {
            List<LoanDTO> loans = loanService.getPendingLoans();
            return ResponseEntity.ok(new ApiResponse<>(true, "Pending loans retrieved", loans));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get loan by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanDTO>> getLoanById(@PathVariable Long id) {
        try {
            LoanDTO loan = loanService.getLoanById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Loan retrieved", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Approve a loan (Admin only)
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanDTO>> approveLoan(@PathVariable Long id) {
        try {
            LoanDTO loan = loanService.approveLoan(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Loan approved", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Reject a loan (Admin only)
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanDTO>> rejectLoan(@PathVariable Long id) {
        try {
            LoanDTO loan = loanService.rejectLoan(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Loan rejected", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get all loans (Admin/Analyst)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<ApiResponse<List<LoanDTO>>> getAllLoans() {
        try {
            List<LoanDTO> loans = loanService.getAllLoans();
            return ResponseEntity.ok(new ApiResponse<>(true, "All loans retrieved", loans));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get lender's loans
     */
    @GetMapping("/lender/my-loans")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<List<LoanDTO>>> getLenderLoans(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<LoanDTO> loans = loanService.getLenderLoans(user.getId().toString());
            return ResponseEntity.ok(new ApiResponse<>(true, "Lender loans retrieved", loans));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
