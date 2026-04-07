package com.klef.loanflowbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "loan_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "offer_code", unique = true)
    private String offerCode;

    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private Lender lender;

    @NotNull
    @Column(name = "min_amount")
    private Double minAmount;

    @NotNull
    @Column(name = "max_amount")
    private Double maxAmount;

    @NotNull
    private Double interestRate;

    @NotNull
    private Integer tenure;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OfferStatus status;

    @ManyToOne
    @JoinColumn(name = "accepted_by_borrower_id")
    private Borrower acceptedByBorrower;

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
        if (status == null) status = OfferStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}

