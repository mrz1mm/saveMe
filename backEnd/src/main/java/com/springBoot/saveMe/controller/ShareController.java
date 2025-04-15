package com.springBoot.saveMe.controller;

import com.springBoot.saveMe.dto.share.ShareRequestDto;
import com.springBoot.saveMe.dto.share.ShareResponseDto;
import com.springBoot.saveMe.model.entity.SharePermission;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.service.ShareService;
import com.springBoot.saveMe.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per gestire le operazioni di condivisione.
 * Gestisce la creazione, la visualizzazione e l'eliminazione delle condivisioni.
 */
@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
@Slf4j
public class ShareController {

    private final ShareService shareService;
    private final UserService userService;

    /**
     * Endpoint per condividere un file.
     * 
     * @param fileId l'ID del file
     * @param shareRequest i dettagli della condivisione
     * @param authentication l'oggetto di autenticazione
     * @param request la richiesta HTTP
     * @return i dettagli della condivisione creata
     */
    @PostMapping("/file/{fileId}")
    public ResponseEntity<ShareResponseDto> shareFile(
            @PathVariable Long fileId,
            @Valid @RequestBody ShareRequestDto shareRequest,
            Authentication authentication,
            HttpServletRequest request) {
        
        log.info("Richiesta di condivisione file: {}", fileId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            SharePermission permission = shareService.shareFile(fileId, shareRequest, user);
            
            String baseUrl = getBaseUrl(request);
            return ResponseEntity.ok(shareService.convertToDto(permission, baseUrl));
        } catch (RuntimeException e) {
            log.error("Errore durante la condivisione del file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per condividere una cartella.
     * 
     * @param folderId l'ID della cartella
     * @param shareRequest i dettagli della condivisione
     * @param authentication l'oggetto di autenticazione
     * @param request la richiesta HTTP
     * @return i dettagli della condivisione creata
     */
    @PostMapping("/folder/{folderId}")
    public ResponseEntity<ShareResponseDto> shareFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody ShareRequestDto shareRequest,
            Authentication authentication,
            HttpServletRequest request) {
        
        log.info("Richiesta di condivisione cartella: {}", folderId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            SharePermission permission = shareService.shareFolder(folderId, shareRequest, user);
            
            String baseUrl = getBaseUrl(request);
            return ResponseEntity.ok(shareService.convertToDto(permission, baseUrl));
        } catch (RuntimeException e) {
            log.error("Errore durante la condivisione della cartella: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per ottenere le condivisioni di un file.
     * 
     * @param fileId l'ID del file
     * @param authentication l'oggetto di autenticazione
     * @param request la richiesta HTTP
     * @return la lista delle condivisioni
     */
    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<ShareResponseDto>> getFileShares(
            @PathVariable Long fileId,
            Authentication authentication,
            HttpServletRequest request) {
        
        log.info("Richiesta di elenco condivisioni del file: {}", fileId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            List<SharePermission> permissions = shareService.getFileShares(fileId, user);
            
            String baseUrl = getBaseUrl(request);
            return ResponseEntity.ok(shareService.convertToDtoList(permissions, baseUrl));
        } catch (RuntimeException e) {
            log.error("Errore durante l'elenco delle condivisioni del file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per ottenere le condivisioni di una cartella.
     * 
     * @param folderId l'ID della cartella
     * @param authentication l'oggetto di autenticazione
     * @param request la richiesta HTTP
     * @return la lista delle condivisioni
     */
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<ShareResponseDto>> getFolderShares(
            @PathVariable Long folderId,
            Authentication authentication,
            HttpServletRequest request) {
        
        log.info("Richiesta di elenco condivisioni della cartella: {}", folderId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            List<SharePermission> permissions = shareService.getFolderShares(folderId, user);
            
            String baseUrl = getBaseUrl(request);
            return ResponseEntity.ok(shareService.convertToDtoList(permissions, baseUrl));
        } catch (RuntimeException e) {
            log.error("Errore durante l'elenco delle condivisioni della cartella: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per eliminare una condivisione.
     * 
     * @param shareId l'ID della condivisione
     * @param authentication l'oggetto di autenticazione
     * @return conferma dell'eliminazione
     */
    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> deleteShare(
            @PathVariable Long shareId,
            Authentication authentication) {
        
        log.info("Richiesta di eliminazione condivisione: {}", shareId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            shareService.deleteShare(shareId, user);
            return ResponseEntity.ok("Condivisione eliminata con successo");
        } catch (RuntimeException e) {
            log.error("Errore durante l'eliminazione della condivisione: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Ottiene l'URL base per la generazione dei link pubblici.
     * 
     * @param request la richiesta HTTP
     * @return l'URL base
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        // Costruzione dell'URL base
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        if (serverPort != 80 && serverPort != 443) {
            url.append(":").append(serverPort);
        }
        
        url.append(contextPath);
        
        return url.toString();
    }
}