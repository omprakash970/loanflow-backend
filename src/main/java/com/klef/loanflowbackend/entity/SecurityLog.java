package com.klef.loanflowbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "security_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_id", unique = true)
    private String logId;

    @NotBlank(message = "Action is required")
    private String action;

    @Column(length = 100)
    private String performedBy;

    @Enumerated(EnumType.STRING)
    private RiskLevel severity;

    @Column(name = "timestamp")
    private Long timestamp;

    @Column(name = "created_at")
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
}
