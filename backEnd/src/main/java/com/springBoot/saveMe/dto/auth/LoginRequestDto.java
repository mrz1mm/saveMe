package com.springBoot.saveMe.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la richiesta di login di un utente.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Il nome utente è obbligatorio")
    private String username;
    
    @NotBlank(message = "La password è obbligatoria")
    private String password;
}