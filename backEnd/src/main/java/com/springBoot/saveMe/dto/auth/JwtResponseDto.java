package com.springBoot.saveMe.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di autenticazione contenente il token JWT.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDto {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
}