package com.springBoot.saveMe.service;

import com.springBoot.saveMe.dto.auth.JwtResponseDto;
import com.springBoot.saveMe.dto.auth.LoginRequestDto;
import com.springBoot.saveMe.dto.auth.RegisterRequestDto;
import com.springBoot.saveMe.model.entity.Role;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.repository.RoleRepository;
import com.springBoot.saveMe.repository.UserRepository;
import com.springBoot.saveMe.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Servizio per la gestione degli utenti e l'autenticazione.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Registra un nuovo utente nel sistema.
     *
     * @param registerRequest i dati di registrazione
     * @return l'utente creato
     */
    @Transactional
    public User registerUser(RegisterRequestDto registerRequest) {
        // Verifica che username ed email non siano già in uso
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username già in uso");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email già in uso");
        }

        // Crea un nuovo utente
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(new HashSet<>())
                .build();

        // Assegna il ruolo ROLE_USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Ruolo ROLE_USER non trovato"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Salva l'utente
        return userRepository.save(user);
    }

    /**
     * Autentica un utente e genera un token JWT.
     *
     * @param loginRequest i dati di login
     * @return la risposta con il token JWT
     */
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        // Autentica l'utente
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        // Imposta l'autenticazione nel contesto di sicurezza
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Genera il token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Carica i dettagli dell'utente
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        // Crea e restituisce la risposta
        return JwtResponseDto.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    /**
     * Carica un utente dal database tramite ID.
     *
     * @param id l'ID dell'utente
     * @return l'utente trovato
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + id));
    }

    /**
     * Carica un utente dal database tramite username.
     *
     * @param username il nome utente
     * @return l'utente trovato
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con username: " + username));
    }
}