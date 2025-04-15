package com.springBoot.saveMe.controller;

import com.springBoot.saveMe.dto.auth.JwtResponseDto;
import com.springBoot.saveMe.dto.auth.LoginRequestDto;
import com.springBoot.saveMe.dto.auth.RegisterRequestDto;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller per gestire le richieste di autenticazione.
 * Gestisce la registrazione degli utenti e il login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /**
     * Endpoint per la registrazione di un nuovo utente.
     * 
     * @param registerRequest dati per la registrazione
     * @return risposta con i dati dell'utente registrato
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto registerRequest) {
        log.info("Richiesta di registrazione per l'utente: {}", registerRequest.getUsername());
        
        try {
            User user = userService.registerUser(registerRequest);
            return ResponseEntity.ok("Utente registrato con successo!");
        } catch (IllegalArgumentException e) {
            log.error("Errore durante la registrazione: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint per il login di un utente.
     * 
     * @param loginRequest dati per il login
     * @return risposta con il token JWT e i dati dell'utente
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.info("Richiesta di login per l'utente: {}", loginRequest.getUsername());
        
        try {
            JwtResponseDto response = userService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Errore durante il login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Credenziali non valide");
        }
    }
}