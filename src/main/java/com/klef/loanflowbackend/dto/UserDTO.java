package com.klef.loanflowbackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}
