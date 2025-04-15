package com.springBoot.saveMe.repository;

import com.springBoot.saveMe.model.entity.SharePermission;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.model.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository per l'accesso e la manipolazione dei permessi di condivisione.
 */
public interface SharePermissionRepository extends JpaRepository<SharePermission, Long> {

    /**
     * Trova tutti i permessi di condivisione per un dato file.
     * 
     * @param resourceId l'id del file
     * @return una lista di permessi
     */
    List<SharePermission> findByResourceIdAndResourceType(Long resourceId, ResourceType resourceType);
    
    /**
     * Trova tutti i permessi di condivisione di un utente.
     * 
     * @param sharedWithUser l'utente con cui è condiviso
     * @return una lista di permessi
     */
    List<SharePermission> findBySharedWithUser(User sharedWithUser);
    
    /**
     * Trova un permesso di condivisione tramite token pubblico.
     * 
     * @param publicLinkToken il token del link pubblico
     * @return un Optional contenente il permesso se trovato
     */
    Optional<SharePermission> findByPublicLinkToken(String publicLinkToken);
    
    /**
     * Trova un permesso di condivisione valido tramite token pubblico (non scaduto).
     * 
     * @param publicLinkToken il token del link pubblico
     * @param now la data/ora attuale
     * @return un Optional contenente il permesso se trovato e valido
     */
    @Query("SELECT sp FROM SharePermission sp WHERE sp.publicLinkToken = :token AND (sp.expiresAt IS NULL OR sp.expiresAt > :now)")
    Optional<SharePermission> findValidPublicLink(@Param("token") String publicLinkToken, @Param("now") LocalDateTime now);
    
    /**
     * Verifica se un utente ha accesso a una risorsa.
     * 
     * @param resourceId l'id della risorsa
     * @param resourceType il tipo della risorsa
     * @param userId l'id dell'utente
     * @return true se l'utente ha accesso, false altrimenti
     */
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM SharePermission sp " +
           "WHERE sp.resourceId = :resourceId AND sp.resourceType = :resourceType AND sp.sharedWithUser.id = :userId")
    boolean hasUserAccess(@Param("resourceId") Long resourceId, 
                           @Param("resourceType") ResourceType resourceType, 
                           @Param("userId") Long userId);
    
    /**
     * Elimina tutti i permessi di condivisione per una risorsa.
     * 
     * @param resourceId l'id della risorsa
     * @param resourceType il tipo della risorsa
     */
    void deleteByResourceIdAndResourceType(Long resourceId, ResourceType resourceType);
}