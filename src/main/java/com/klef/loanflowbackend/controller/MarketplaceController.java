package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.*;
import com.klef.loanflowbackend.entity.User;
import com.klef.loanflowbackend.repository.UserRepository;
import com.klef.loanflowbackend.service.MarketplaceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final UserRepository userRepository;

    // --- Offers ---

    @PostMapping("/offers")
    // @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<LoanOfferDTO>> createOffer(Authentication authentication, @RequestBody LoanOfferDTO dto) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            LoanOfferDTO created = marketplaceService.createOffer(user.getId(), dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Offer created", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/offers/open")
    // @PreAuthorize("hasRole('BORROWER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanOfferDTO>>> listOpenOffers() {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Open offers", marketplaceService.listOpenOffers()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/offers/my")
    // @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<List<LoanOfferDTO>>> listMyOffers(Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(new ApiResponse<>(true, "My offers", marketplaceService.listMyOffers(user.getId())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/offers/{offerId}/accept")
    // @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<LoanDTO>> acceptOffer(Authentication authentication, @PathVariable Long offerId, @RequestBody AcceptOfferRequest req) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            LoanDTO loan = marketplaceService.acceptOffer(user.getId(), offerId, req.getAmount(), req.getPurpose());
            return ResponseEntity.ok(new ApiResponse<>(true, "Offer accepted and loan sanctioned", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // --- Requests ---

    @PostMapping("/requests")
    // @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<LoanRequestDTO>> createRequest(Authentication authentication, @RequestBody LoanRequestDTO dto) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            LoanRequestDTO created = marketplaceService.createRequest(user.getId(), dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Request created", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/requests/open")
    // @PreAuthorize("hasRole('LENDER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanRequestDTO>>> listOpenRequests() {
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Open requests", marketplaceService.listOpenRequests()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/requests/my")
    // @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<List<LoanRequestDTO>>> listMyRequests(Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(new ApiResponse<>(true, "My requests", marketplaceService.listMyRequests(user.getId())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/requests/{requestId}/approve")
    // @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<LoanDTO>> approveRequest(Authentication authentication, @PathVariable Long requestId) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            LoanDTO loan = marketplaceService.approveRequest(user.getId(), requestId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Request approved and loan sanctioned", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @Data
    public static class AcceptOfferRequest {
        private Double amount;
        private String purpose;
    }
}
