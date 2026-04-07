package com.klef.loanflowbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", unique = true)
    private String loanId;

    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @ManyToOne
    @JoinColumn(name = "lender_id")
    private Lender lender;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Interest rate is required")
    private Double interestRate;

    @NotNull(message = "Tenure is required")
    private Integer tenure;

    @Column(length = 100)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private LoanStatus status;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "next_payment_date")
    private Long nextPaymentDate;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}
