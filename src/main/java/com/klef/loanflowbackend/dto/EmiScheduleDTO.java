package com.klef.loanflowbackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiScheduleDTO {

    private Long id;
    private Long loanId;
    private Integer month;
    private Double emiAmount;
    private Double principal;
    private Double interest;
    private Double balance;
    private String status;
    private Long createdAt;
}
