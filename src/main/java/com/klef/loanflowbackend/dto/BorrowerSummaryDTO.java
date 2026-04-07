package com.klef.loanflowbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerSummaryDTO {
    private Long borrowerId;
    private Long userId;
    private String idCode;
    private String name;
    private Integer activeLoans;
    private String riskLevel;
}

