package com.example.carnet_de_sante.application.dto;

import com.example.carnet_de_sante.domain.model.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String email;
    private Role role;
    private Long userId;
}
