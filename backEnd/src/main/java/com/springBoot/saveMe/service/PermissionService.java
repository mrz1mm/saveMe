package com.springBoot.saveMe.service;

import com.springBoot.saveMe.model.entity.SharePermission;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.model.enums.PermissionType;
import com.springBoot.saveMe.model.enums.ResourceType;
import com.springBoot.saveMe.repository.SharePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servizio per la gestione dei permessi di condivisione.
 * Gestisce la creazione, la lettura e la rimozione dei permessi di condivisione per file e cartelle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final SharePermissionRepository sharePermissionRepository;

    /**
     * Crea un permesso di condivisione per una risorsa.
     *
     * @param resourceId l'ID della risorsa
     * @param resourceType il tipo di risorsa (FILE o FOLDER)
     * @param sharedWithUser l'utente con cui condividere (null per link pubblici)
     * @param permissionType il tipo di permesso (READ o EDIT)
     * @param isPublicLink indica se è un link pubblico
     * @param expiresAt data di scadenza (null per nessuna scadenza)
     * @return il permesso di condivisione creato
     */
    @Transactional
    public SharePermission createSharePermission(
            Long resourceId,
            ResourceType resourceType,
            User sharedWithUser,
            PermissionType permissionType,
            boolean isPublicLink,
            LocalDateTime expiresAt) {

        SharePermission permission = SharePermission.builder()
                .resourceId(resourceId)
                .resourceType(resourceType)
                .sharedWithUser(sharedWithUser)
                .permissionType(permissionType)
                .isPublicLink(isPublicLink)
                .expiresAt(expiresAt)
                .build();

        if (isPublicLink) {
            permission.setPublicLinkToken(generatePublicLinkToken());
        }

        return sharePermissionRepository.save(permission);
    }

    /**
     * Verifica se un utente ha accesso a una risorsa.
     *
     * @param resourceId l'ID della risorsa
     * @param resourceType il tipo di risorsa
     * @param userId l'ID dell'utente
     * @return true se l'utente ha accesso, false altrimenti
     */
    @Transactional(readOnly = true)
    public boolean hasUserAccess(Long resourceId, ResourceType resourceType, Long userId) {
        return sharePermissionRepository.hasUserAccess(resourceId, resourceType, userId);
    }

    /**
     * Ottiene un permesso di condivisione tramite token pubblico.
     *
     * @param token il token del link pubblico
     * @return il permesso di condivisione se valido
     */
    @Transactional(readOnly = true)
    public Optional<SharePermission> getValidPublicPermission(String token) {
        return sharePermissionRepository.findValidPublicLink(token, LocalDateTime.now());
    }

    /**
     * Trova tutti i permessi di condivisione per una risorsa.
     *
     * @param resourceId l'ID della risorsa
     * @param resourceType il tipo di risorsa
     * @return la lista dei permessi di condivisione
     */
    @Transactional(readOnly = true)
    public List<SharePermission> findSharePermissions(Long resourceId, ResourceType resourceType) {
        return sharePermissionRepository.findByResourceIdAndResourceType(resourceId, resourceType);
    }

    /**
     * Elimina un permesso di condivisione.
     *
     * @param permissionId l'ID del permesso
     */
    @Transactional
    public void deleteSharePermission(Long permissionId) {
        sharePermissionRepository.deleteById(permissionId);
    }

    /**
     * Elimina tutti i permessi di condivisione per una risorsa.
     *
     * @param resourceId l'ID della risorsa
     * @param resourceType il tipo di risorsa
     */
    @Transactional
    public void deleteAllSharePermissions(Long resourceId, ResourceType resourceType) {
        sharePermissionRepository.deleteByResourceIdAndResourceType(resourceId, resourceType);
    }

    /**
     * Genera un token univoco per un link pubblico.
     *
     * @return il token generato
     */
    private String generatePublicLinkToken() {
        return UUID.randomUUID().toString();
    }
}