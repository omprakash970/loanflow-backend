package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.dto.PaymentDTO;
import com.klef.loanflowbackend.entity.User;
import com.klef.loanflowbackend.repository.UserRepository;
import com.klef.loanflowbackend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @GetMapping("/lender/my")
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getMyLenderPayments(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<PaymentDTO> payments = paymentService.getPaymentsForLenderUser(user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Payments retrieved", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getAllPayments() {
        try {
            List<PaymentDTO> payments = paymentService.getAllPayments();
            return ResponseEntity.ok(new ApiResponse<>(true, "All payments retrieved", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/borrower/pay-emi")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<PaymentDTO>> payEmi(
            @RequestBody PaymentDTO paymentDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            PaymentDTO payment = paymentService.createPaymentFromBorrower(paymentDTO, user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment created successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/borrower/my")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getMyBorrowerPayments(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<PaymentDTO> payments = paymentService.getPaymentsForBorrowerUser(user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Payments retrieved", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

