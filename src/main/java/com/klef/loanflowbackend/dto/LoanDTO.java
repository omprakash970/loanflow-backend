package com.klef.loanflowbackend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDTO {

    private Long id;
    private String loanId;
    private String borrowerName;
    private String borrowerEmail;
    private Long borrowerId;
    private String lenderName;
    private Long lenderId;
    private Double amount;
    private Double interestRate;
    private Integer tenure;
    private String purpose;
    private String status;
    private Long startDate;
    private Long nextPaymentDate;
    private List<EmiScheduleDTO> emiSchedules;
    private Long createdAt;
}
