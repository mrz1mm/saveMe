package com.springBoot.saveMe.controller;

import com.springBoot.saveMe.dto.folder.FolderRequestDto;
import com.springBoot.saveMe.dto.folder.FolderResponseDto;
import com.springBoot.saveMe.model.entity.Folder;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.service.FolderService;
import com.springBoot.saveMe.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per gestire le operazioni sulle cartelle.
 * Gestisce la creazione, l'aggiornamento, l'elenco e l'eliminazione delle cartelle.
 */
@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@Slf4j
public class FolderController {

    private final FolderService folderService;
    private final UserService userService;

    /**
     * Endpoint per creare una nuova cartella.
     * 
     * @param folderRequest i dati della cartella
     * @param authentication l'oggetto di autenticazione
     * @return i dati della cartella creata
     */
    @PostMapping
    public ResponseEntity<FolderResponseDto> createFolder(
            @Valid @RequestBody FolderRequestDto folderRequest,
            Authentication authentication) {
        
        log.info("Richiesta di creazione cartella: {}, cartella genitore: {}", 
                folderRequest.getName(), folderRequest.getParentFolderId());
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            Folder folder = folderService.createFolder(
                    folderRequest.getName(), 
                    folderRequest.getParentFolderId(), 
                    user);
            
            return ResponseEntity.ok(folderService.convertToDto(folder));
        } catch (RuntimeException e) {
            log.error("Errore durante la creazione della cartella: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per aggiornare una cartella esistente.
     * 
     * @param folderId l'ID della cartella
     * @param folderRequest i nuovi dati della cartella
     * @param authentication l'oggetto di autenticazione
     * @return i dati della cartella aggiornata
     */
    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponseDto> updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody FolderRequestDto folderRequest,
            Authentication authentication) {
        
        log.info("Richiesta di aggiornamento cartella: {}, nome: {}, cartella genitore: {}", 
                folderId, folderRequest.getName(), folderRequest.getParentFolderId());
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            Folder folder = folderService.updateFolder(
                    folderId,
                    folderRequest.getName(), 
                    folderRequest.getParentFolderId(), 
                    user);
            
            return ResponseEntity.ok(folderService.convertToDto(folder));
        } catch (RuntimeException e) {
            log.error("Errore durante l'aggiornamento della cartella: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per ottenere i dati di una cartella.
     * 
     * @param folderId l'ID della cartella
     * @param authentication l'oggetto di autenticazione
     * @return i dati della cartella
     */
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponseDto> getFolder(
            @PathVariable Long folderId,
            Authentication authentication) {
        
        log.info("Richiesta di visualizzazione cartella: {}", folderId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            Folder folder = folderService.getFolder(folderId, user);
            return ResponseEntity.ok(folderService.convertToDto(folder));
        } catch (RuntimeException e) {
            log.error("Errore durante la visualizzazione della cartella: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per ottenere l'elenco delle cartelle di un utente, opzionalmente filtrate per cartella genitore.
     * 
     * @param parentFolderId l'ID della cartella genitore (opzionale)
     * @param authentication l'oggetto di autenticazione
     * @return l'elenco delle cartelle
     */
    @GetMapping
    public ResponseEntity<List<FolderResponseDto>> getFolders(
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId,
            Authentication authentication) {
        
        log.info("Richiesta di elenco cartelle, cartella genitore: {}", parentFolderId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            List<Folder> folders = folderService.getUserFolders(user, parentFolderId);
            return ResponseEntity.ok(folderService.convertToDtoList(folders));
        } catch (RuntimeException e) {
            log.error("Errore durante l'elenco delle cartelle: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per eliminare una cartella.
     * 
     * @param folderId l'ID della cartella
     * @param authentication l'oggetto di autenticazione
     * @return conferma dell'eliminazione
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(
            @PathVariable Long folderId,
            Authentication authentication) {
        
        log.info("Richiesta di eliminazione cartella: {}", folderId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            folderService.deleteFolder(folderId, user);
            return ResponseEntity.ok("Cartella eliminata con successo");
        } catch (RuntimeException e) {
            log.error("Errore durante l'eliminazione della cartella: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}