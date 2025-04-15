package com.springBoot.saveMe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servizio per la gestione dello storage dei file.
 * Si occupa di salvare, recuperare ed eliminare file dal file system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    /**
     * Salva un file nel sistema di storage.
     *
     * @param file il file da salvare
     * @return il nome generato del file salvato
     * @throws IOException se si verifica un errore durante il salvataggio
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Normalizza il nome del file
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Genera un nome univoco per il file
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        
        // Crea la directory se non esiste
        Path uploadPath = getUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Copia il file nella directory di destinazione
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File salvato: {}", fileName);
        return fileName;
    }

    /**
     * Carica un file come risorsa.
     *
     * @param fileName il nome del file da caricare
     * @return la risorsa del file
     * @throws IOException se si verifica un errore durante il caricamento
     */
    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = getUploadPath().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("File non trovato: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new IOException("File non trovato: " + fileName, ex);
        }
    }

    /**
     * Elimina un file dal sistema di storage.
     *
     * @param fileName il nome del file da eliminare
     * @return true se l'eliminazione ha avuto successo, false altrimenti
     */
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = getUploadPath().resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Errore durante l'eliminazione del file: {}", fileName, ex);
            return false;
        }
    }

    /**
     * Ottiene il percorso completo della directory di upload.
     *
     * @return il percorso della directory di upload
     */
    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}