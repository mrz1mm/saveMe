package com.springBoot.saveMe.security.services;

import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione di UserDetailsService per caricare i dettagli utente dal database.
 * Utilizzata da Spring Security per l'autenticazione e l'autorizzazione.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carica un utente dal database in base al nome utente.
     *
     * @param username il nome utente da cercare
     * @return un oggetto UserDetails costruito dai dati dell'utente trovato
     * @throws UsernameNotFoundException se l'utente non viene trovato
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con username: " + username));
        
        // Conversione dei ruoli in autorità Spring Security
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
        
        // Creazione dell'oggetto UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}