package com.springBoot.saveMe.repository;

import com.springBoot.saveMe.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository per l'accesso e la manipolazione dei dati degli utenti.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Cerca un utente per username.
     * 
     * @param username il nome utente da cercare
     * @return un Optional contenente l'utente se trovato
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Cerca un utente per email.
     * 
     * @param email l'email da cercare
     * @return un Optional contenente l'utente se trovato
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se esiste un utente con un dato username.
     * 
     * @param username il nome utente da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica se esiste un utente con una data email.
     * 
     * @param email l'email da verificare
     * @return true se esiste, false altrimenti
     */
    boolean existsByEmail(String email);
}