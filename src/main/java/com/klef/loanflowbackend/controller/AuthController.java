package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.*;
import com.klef.loanflowbackend.service.AuthService;
import com.klef.loanflowbackend.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    /**
     * Register a new user
     * POST /api/auth/register
     * Supported roles: ADMIN, LENDER, BORROWER, ANALYST
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "User registered successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Registration failed", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Registration failed", e.getMessage()));
        }
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(true, "Login successful", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Login failed", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Login failed", "Invalid credentials"));
        }
    }

    /**
     * Send OTP to email for registration
     * POST /api/auth/send-otp
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            otpService.sendOtp(request.getEmail());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(true, "OTP sent successfully to " + request.getEmail(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to send OTP", e.getMessage()));
        }
    }

    /**
     * Verify OTP
     * POST /api/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OtpVerificationResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            otpService.verifyOtp(request.getEmail(), request.getOtp());
            OtpVerificationResponse response = OtpVerificationResponse.builder()
                    .verified(true)
                    .message("OTP verified successfully")
                    .build();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(true, "OTP verified", response));
        } catch (IllegalArgumentException e) {
            OtpVerificationResponse response = OtpVerificationResponse.builder()
                    .verified(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "OTP verification failed", response));
        } catch (Exception e) {
            OtpVerificationResponse response = OtpVerificationResponse.builder()
                    .verified(false)
                    .message("An error occurred during OTP verification")
                    .build();


            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "OTP verification failed", response));
        }
    }
}

