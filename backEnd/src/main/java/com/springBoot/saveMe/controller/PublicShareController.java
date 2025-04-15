package com.springBoot.saveMe.controller;

import com.springBoot.saveMe.dto.file.FileResponseDto;
import com.springBoot.saveMe.dto.folder.FolderResponseDto;
import com.springBoot.saveMe.model.entity.Folder;
import com.springBoot.saveMe.model.entity.SharePermission;
import com.springBoot.saveMe.model.entity.StoredFile;
import com.springBoot.saveMe.model.enums.ResourceType;
import com.springBoot.saveMe.repository.FolderRepository;
import com.springBoot.saveMe.repository.StoredFileRepository;
import com.springBoot.saveMe.service.FileService;
import com.springBoot.saveMe.service.FileStorageService;
import com.springBoot.saveMe.service.FolderService;
import com.springBoot.saveMe.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Controller per gestire l'accesso alle risorse condivise pubblicamente.
 * Gestisce l'accesso ai file e alle cartelle tramite link pubblici.
 */
@RestController
@RequestMapping("/public/share")
@RequiredArgsConstructor
@Slf4j
public class PublicShareController {

    private final PermissionService permissionService;
    private final FileStorageService fileStorageService;
    private final StoredFileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileService fileService;
    private final FolderService folderService;

    /**
     * Endpoint per accedere a una risorsa condivisa tramite token pubblico.
     * 
     * @param token il token del link pubblico
     * @return i dati della risorsa condivisa o la risorsa stessa
     */
    @GetMapping("/{token}")
    public ResponseEntity<?> accessSharedResource(@PathVariable String token) {
        log.info("Richiesta di accesso a risorsa condivisa con token: {}", token);
        
        try {
            // Verifica che il token sia valido e non scaduto
            Optional<SharePermission> permissionOpt = permissionService.getValidPublicPermission(token);
            
            if (permissionOpt.isEmpty()) {
                log.error("Token non valido o scaduto: {}", token);
                return ResponseEntity.badRequest().body("Link non valido o scaduto");
            }
            
            SharePermission permission = permissionOpt.get();
            
            // Gestisce in modo diverso in base al tipo di risorsa
            if (permission.getResourceType() == ResourceType.FILE) {
                return handleFileAccess(permission);
            } else if (permission.getResourceType() == ResourceType.FOLDER) {
                return handleFolderAccess(permission);
            } else {
                log.error("Tipo di risorsa non supportato: {}", permission.getResourceType());
                return ResponseEntity.badRequest().body("Tipo di risorsa non supportato");
            }
        } catch (Exception e) {
            log.error("Errore durante l'accesso alla risorsa condivisa: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Errore durante l'accesso alla risorsa condivisa");
        }
    }
    
    /**
     * Gestisce l'accesso a un file condiviso.
     * 
     * @param permission il permesso di condivisione
     * @return il file
     * @throws IOException se si verifica un errore durante il recupero del file
     */
    private ResponseEntity<?> handleFileAccess(SharePermission permission) throws IOException {
        Optional<StoredFile> fileOpt = fileRepository.findById(permission.getResourceId());
        
        if (fileOpt.isEmpty()) {
            log.error("File non trovato: {}", permission.getResourceId());
            return ResponseEntity.badRequest().body("File non trovato");
        }
        
        StoredFile file = fileOpt.get();
        Resource resource = fileStorageService.loadFileAsResource(file.getFileName());
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                .body(resource);
    }
    
    /**
     * Gestisce l'accesso a una cartella condivisa.
     * 
     * @param permission il permesso di condivisione
     * @return i metadati della cartella
     */
    private ResponseEntity<?> handleFolderAccess(SharePermission permission) {
        Optional<Folder> folderOpt = folderRepository.findById(permission.getResourceId());
        
        if (folderOpt.isEmpty()) {
            log.error("Cartella non trovata: {}", permission.getResourceId());
            return ResponseEntity.badRequest().body("Cartella non trovata");
        }
        
        Folder folder = folderOpt.get();
        FolderResponseDto folderDto = folderService.convertToDto(folder);
        
        return ResponseEntity.ok(folderDto);
    }
}