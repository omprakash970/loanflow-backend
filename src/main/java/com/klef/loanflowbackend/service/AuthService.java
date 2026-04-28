package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.AuthRequest;
import com.klef.loanflowbackend.dto.AuthResponse;
import com.klef.loanflowbackend.dto.RegisterRequest;
import com.klef.loanflowbackend.entity.*;
import com.klef.loanflowbackend.repository.BorrowerRepository;
import com.klef.loanflowbackend.repository.LenderRepository;
import com.klef.loanflowbackend.repository.UserRepository;
import com.klef.loanflowbackend.security.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BorrowerRepository borrowerRepository;
    private final LenderRepository lenderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    /**
     * Register a new user - requires verified OTP
     */
    public AuthResponse register(RegisterRequest request) {
        // Verify OTP before registration
        if (!otpService.isOtpVerified(request.getEmail())) {
            throw new IllegalArgumentException("Email not verified. Please verify your OTP first.");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Validate role
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        // Create new user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);

        // Auto-create role profile row
        if (role == Role.BORROWER) {
            borrowerRepository.findByUserId(savedUser.getId()).orElseGet(() -> borrowerRepository.save(
                    Borrower.builder()
                            .user(savedUser)
                            .activeLoans(0)
                            .riskLevel(RiskLevel.LOW)
                            .creditScore(700)
                            .kycVerified(Boolean.FALSE)
                            .build()
            ));
        } else if (role == Role.LENDER) {
            lenderRepository.findByUserId(savedUser.getId()).orElseGet(() -> lenderRepository.save(
                    Lender.builder()
                            .user(savedUser)
                            .companyName(null)
                            .activeLoans(0)
                            .totalDisbursed(0.0)
                            .build()
            ));
        }

        // Clean up OTP record after successful registration
        otpService.cleanupOtp(request.getEmail());

        // Generate JWT token
        String token = jwtService.generateToken(savedUser.getEmail());

        // Return auth response
        return AuthResponse.builder()
                .token(token)
                .role(savedUser.getRole().name())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .userId(savedUser.getId())
                .build();
    }

    /**
     * Login user
     */
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userId(user.getId())
                .build();
    }
}