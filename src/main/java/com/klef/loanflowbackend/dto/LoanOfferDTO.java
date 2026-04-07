package com.klef.loanflowbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanOfferDTO {
    private Long id;
    private String offerCode;
    private Long lenderId;
    private String lenderName;
    private Double minAmount;
    private Double maxAmount;
    private Double interestRate;
    private Integer tenure;
    private String status;
    private Long acceptedByBorrowerId;
    private String acceptedByBorrowerName;
    private Long sanctionedLoanId;
    private String sanctionedLoanCode;
    private Long createdAt;
}

