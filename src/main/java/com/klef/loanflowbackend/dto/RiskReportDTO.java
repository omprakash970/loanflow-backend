package com.klef.loanflowbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskReportDTO {
    private Long id;
    private Long loanId;
    private String loanCode;
    private Integer riskScore;
    private Double defaultProbability;
}

