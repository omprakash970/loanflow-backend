package com.klef.loanflowbackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserStatusRequest {

    private Long userId;
    private String status; // ACTIVE, DISABLED
}
