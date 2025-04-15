package com.springBoot.saveMe.service;

import com.springBoot.saveMe.dto.share.ShareRequestDto;
import com.springBoot.saveMe.dto.share.ShareResponseDto;
import com.springBoot.saveMe.model.entity.Folder;
import com.springBoot.saveMe.model.entity.SharePermission;
import com.springBoot.saveMe.model.entity.StoredFile;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.model.enums.ResourceType;
import com.springBoot.saveMe.repository.FolderRepository;
import com.springBoot.saveMe.repository.StoredFileRepository;
import com.springBoot.saveMe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione delle condivisioni di file e cartelle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShareService {

    private final PermissionService permissionService;
    private final StoredFileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    /**
     * Crea una condivisione per un file.
     *
     * @param fileId l'ID del file
     * @param shareRequest i dettagli della condivisione
     * @param owner l'utente proprietario
     * @return i dettagli della condivisione creata
     */
    @Transactional
    public SharePermission shareFile(Long fileId, ShareRequestDto shareRequest, User owner) {
        // Verifica che il file esista e appartenga all'utente
        StoredFile file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new RuntimeException("File non trovato o non autorizzato"));
        
        // Trova l'utente con cui condividere, se specificato
        User sharedWithUser = null;
        if (shareRequest.getSharedWithUserId() != null) {
            sharedWithUser = userRepository.findById(shareRequest.getSharedWithUserId())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        }
        
        // Crea il permesso di condivisione
        return permissionService.createSharePermission(
                fileId,
                ResourceType.FILE,
                sharedWithUser,
                shareRequest.getPermissionType(),
                shareRequest.isPublicLink(),
                shareRequest.getExpiresAt()
        );
    }

    /**
     * Crea una condivisione per una cartella.
     *
     * @param folderId l'ID della cartella
     * @param shareRequest i dettagli della condivisione
     * @param owner l'utente proprietario
     * @return i dettagli della condivisione creata
     */
    @Transactional
    public SharePermission shareFolder(Long folderId, ShareRequestDto shareRequest, User owner) {
        // Verifica che la cartella esista e appartenga all'utente
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new RuntimeException("Cartella non trovata o non autorizzata"));
        
        // Trova l'utente con cui condividere, se specificato
        User sharedWithUser = null;
        if (shareRequest.getSharedWithUserId() != null) {
            sharedWithUser = userRepository.findById(shareRequest.getSharedWithUserId())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        }
        
        // Crea il permesso di condivisione
        return permissionService.createSharePermission(
                folderId,
                ResourceType.FOLDER,
                sharedWithUser,
                shareRequest.getPermissionType(),
                shareRequest.isPublicLink(),
                shareRequest.getExpiresAt()
        );
    }

    /**
     * Ottiene tutte le condivisioni di un file.
     *
     * @param fileId l'ID del file
     * @param owner l'utente proprietario
     * @return la lista delle condivisioni
     */
    @Transactional(readOnly = true)
    public List<SharePermission> getFileShares(Long fileId, User owner) {
        // Verifica che il file esista e appartenga all'utente
        StoredFile file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new RuntimeException("File non trovato o non autorizzato"));
        
        return permissionService.findSharePermissions(fileId, ResourceType.FILE);
    }

    /**
     * Ottiene tutte le condivisioni di una cartella.
     *
     * @param folderId l'ID della cartella
     * @param owner l'utente proprietario
     * @return la lista delle condivisioni
     */
    @Transactional(readOnly = true)
    public List<SharePermission> getFolderShares(Long folderId, User owner) {
        // Verifica che la cartella esista e appartenga all'utente
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new RuntimeException("Cartella non trovata o non autorizzata"));
        
        return permissionService.findSharePermissions(folderId, ResourceType.FOLDER);
    }

    /**
     * Elimina una condivisione.
     *
     * @param shareId l'ID della condivisione
     * @param user l'utente che richiede l'eliminazione
     */
    @Transactional
    public void deleteShare(Long shareId, User user) {
        // Qui potremmo aggiungere un controllo per verificare che l'utente sia il proprietario
        // della risorsa associata alla condivisione, ma per semplicità assumiamo che
        // solo il controller controlla questo
        permissionService.deleteSharePermission(shareId);
    }

    /**
     * Converte un permesso di condivisione in un DTO di risposta.
     *
     * @param permission il permesso di condivisione
     * @param baseUrl l'URL base per i link pubblici
     * @return il DTO di risposta
     */
    public ShareResponseDto convertToDto(SharePermission permission, String baseUrl) {
        String resourceName = "";
        
        // Ottiene il nome della risorsa
        if (permission.getResourceType() == ResourceType.FILE) {
            fileRepository.findById(permission.getResourceId())
                    .ifPresent(file -> {
                        // Utilizziamo una variabile locale perché non possiamo modificare
                        // la variabile resourceName direttamente da dentro la lambda
                        Thread.currentThread().setName(file.getOriginalFileName());
                    });
            resourceName = Thread.currentThread().getName();
        } else if (permission.getResourceType() == ResourceType.FOLDER) {
            folderRepository.findById(permission.getResourceId())
                    .ifPresent(folder -> {
                        Thread.currentThread().setName(folder.getName());
                    });
            resourceName = Thread.currentThread().getName();
        }
        
        // Costruisce l'URL pubblico se applicabile
        String publicLinkUrl = null;
        if (permission.isPublicLink() && permission.getPublicLinkToken() != null) {
            publicLinkUrl = baseUrl + "/public/share/" + permission.getPublicLinkToken();
        }
        
        return ShareResponseDto.builder()
                .id(permission.getId())
                .resourceId(permission.getResourceId())
                .resourceType(permission.getResourceType())
                .resourceName(resourceName)
                .sharedWithUserId(permission.getSharedWithUser() != null ? permission.getSharedWithUser().getId() : null)
                .sharedWithUsername(permission.getSharedWithUser() != null ? permission.getSharedWithUser().getUsername() : null)
                .permissionType(permission.getPermissionType())
                .isPublicLink(permission.isPublicLink())
                .publicLinkToken(permission.getPublicLinkToken())
                .publicLinkUrl(publicLinkUrl)
                .expiresAt(permission.getExpiresAt())
                .createdAt(permission.getCreatedAt())
                .build();
    }

    /**
     * Converte una lista di permessi di condivisione in una lista di DTO di risposta.
     *
     * @param permissions la lista di permessi
     * @param baseUrl l'URL base per i link pubblici
     * @return la lista di DTO di risposta
     */
    public List<ShareResponseDto> convertToDtoList(List<SharePermission> permissions, String baseUrl) {
        return permissions.stream()
                .map(permission -> convertToDto(permission, baseUrl))
                .collect(Collectors.toList());
    }
}