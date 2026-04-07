package com.klef.loanflowbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private String paymentId;
    private Long loanId;
    private Long emiScheduleId;
    private String loanCode;
    private String borrowerName;
    private String lenderName;
    private Double amount;
    private Long paymentDate;
    private String method;
    private String status;
}

