package com.springBoot.saveMe.controller;

import com.springBoot.saveMe.dto.file.FileResponseDto;
import com.springBoot.saveMe.model.entity.StoredFile;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.service.FileService;
import com.springBoot.saveMe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controller per gestire le operazioni sui file.
 * Gestisce l'upload, il download, l'elenco e l'eliminazione dei file.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;
    private final UserService userService;

    /**
     * Endpoint per caricare un file.
     * 
     * @param file il file da caricare
     * @param folderId l'ID della cartella (opzionale)
     * @param authentication l'oggetto di autenticazione
     * @return i metadati del file caricato
     */
    @PostMapping("/upload")
    public ResponseEntity<FileResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId,
            Authentication authentication) {
        
        log.info("Richiesta di upload file: {}, cartella: {}", file.getOriginalFilename(), folderId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            StoredFile storedFile = fileService.uploadFile(file, folderId, user);
            return ResponseEntity.ok(fileService.convertToDto(storedFile));
        } catch (IOException e) {
            log.error("Errore durante l'upload del file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            log.error("Errore durante l'upload del file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per scaricare un file.
     * 
     * @param fileId l'ID del file
     * @param authentication l'oggetto di autenticazione
     * @return il file richiesto
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        log.info("Richiesta di download file: {}", fileId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            StoredFile file = fileService.getFile(fileId, user);
            Resource resource = fileService.downloadFile(fileId, user);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Errore durante il download del file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            log.error("Errore durante il download del file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per ottenere i metadati di un file.
     * 
     * @param fileId l'ID del file
     * @param authentication l'oggetto di autenticazione
     * @return i metadati del file
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        log.info("Richiesta di visualizzazione file: {}", fileId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            StoredFile file = fileService.getFile(fileId, user);
            return ResponseEntity.ok(fileService.convertToDto(file));
        } catch (RuntimeException e) {
            log.error("Errore durante la visualizzazione del file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per ottenere l'elenco dei file in una cartella o al livello principale.
     * 
     * @param folderId l'ID della cartella (opzionale)
     * @param authentication l'oggetto di autenticazione
     * @return l'elenco dei file
     */
    @GetMapping
    public ResponseEntity<List<FileResponseDto>> getFiles(
            @RequestParam(value = "folderId", required = false) Long folderId,
            Authentication authentication) {
        
        log.info("Richiesta di elenco file, cartella: {}", folderId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            List<StoredFile> files = fileService.getUserFiles(user, folderId);
            return ResponseEntity.ok(fileService.convertToDtoList(files));
        } catch (RuntimeException e) {
            log.error("Errore durante l'elenco dei file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint per eliminare un file.
     * 
     * @param fileId l'ID del file
     * @param authentication l'oggetto di autenticazione
     * @return conferma dell'eliminazione
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        log.info("Richiesta di eliminazione file: {}", fileId);
        
        try {
            User user = userService.getUserByUsername(authentication.getName());
            boolean deleted = fileService.deleteFile(fileId, user);
            
            if (deleted) {
                return ResponseEntity.ok("File eliminato con successo");
            } else {
                return ResponseEntity.internalServerError().body("Errore durante l'eliminazione del file dal file system");
            }
        } catch (RuntimeException e) {
            log.error("Errore durante l'eliminazione del file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}