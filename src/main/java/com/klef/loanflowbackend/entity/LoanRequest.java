package com.klef.loanflowbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "loan_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", unique = true)
    private String requestCode;

    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Tenure is required")
    private Integer tenure;

    @Column(length = 100)
    @NotNull(message = "Purpose is required")
    private String purpose;

    @NotNull(message = "Interest rate is required")
    private Double interestRate;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private RequestStatus status;

    @ManyToOne
    @JoinColumn(name = "approved_by_lender_id")
    private Lender approvedByLender;

    @OneToOne
    @JoinColumn(name = "sanctioned_loan_id")
    private Loan sanctionedLoan;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        if (status == null) {
            status = RequestStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}

