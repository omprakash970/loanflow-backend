package com.klef.loanflowbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "otp_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        if (expiresAt == null) {
            // OTP expires in 10 minutes
            expiresAt = System.currentTimeMillis() + (10 * 60 * 1000);
        }
    }
}

