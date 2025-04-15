package com.springBoot.saveMe.repository;

import com.springBoot.saveMe.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository per l'accesso e la manipolazione dei ruoli.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Trova un ruolo in base al suo nome.
     * 
     * @param name il nome del ruolo da cercare
     * @return un Optional contenente il ruolo se trovato
     */
    Optional<Role> findByName(String name);
}