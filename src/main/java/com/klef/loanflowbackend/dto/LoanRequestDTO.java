package com.klef.loanflowbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequestDTO {
    private Long id;
    private String requestCode;
    private Long borrowerId;
    private String borrowerName;
    private Double amount;
    private Integer tenure;
    private String purpose;
    private Double interestRate;
    private String status;
    private Long approvedByLenderId;
    private String approvedByLenderName;
    private Long sanctionedLoanId;
    private String sanctionedLoanCode;
    private Long createdAt;
}

