package com.springBoot.saveMe.service;

import com.springBoot.saveMe.dto.file.FileResponseDto;
import com.springBoot.saveMe.model.entity.Folder;
import com.springBoot.saveMe.model.entity.StoredFile;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.model.enums.ResourceType;
import com.springBoot.saveMe.repository.FolderRepository;
import com.springBoot.saveMe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione dei file.
 * Gestisce l'upload, il download, la visualizzazione e l'eliminazione dei file.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final StoredFileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileStorageService fileStorageService;
    private final PermissionService permissionService;

    /**
     * Carica un nuovo file nel sistema.
     *
     * @param file il file da caricare
     * @param folderId l'ID della cartella (può essere null)
     * @param owner l'utente proprietario
     * @return i metadati del file caricato
     * @throws IOException se si verifica un errore durante il caricamento
     */
    @Transactional
    public StoredFile uploadFile(MultipartFile file, Long folderId, User owner) throws IOException {
        // Salva il file nel file system
        String storedFileName = fileStorageService.storeFile(file);
        
        // Trova la cartella se specificata
        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findByIdAndOwner(folderId, owner)
                    .orElseThrow(() -> new RuntimeException("Cartella non trovata o non autorizzata"));
        }
        
        // Crea il record del file nel database
        StoredFile storedFile = StoredFile.builder()
                .fileName(storedFileName)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .storagePath(storedFileName)
                .owner(owner)
                .folder(folder)
                .build();
        
        return fileRepository.save(storedFile);
    }

    /**
     * Ottiene i metadati di un file.
     *
     * @param fileId l'ID del file
     * @param user l'utente che richiede i metadati
     * @return i metadati del file
     */
    @Transactional(readOnly = true)
    public StoredFile getFile(Long fileId, User user) {
        StoredFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File non trovato"));
        
        // Verifica se l'utente è il proprietario o ha accesso condiviso
        if (file.getOwner().getId().equals(user.getId()) || 
                permissionService.hasUserAccess(fileId, ResourceType.FILE, user.getId())) {
            return file;
        }
        
        throw new RuntimeException("Accesso non autorizzato al file");
    }

    /**
     * Scarica un file.
     *
     * @param fileId l'ID del file
     * @param user l'utente che richiede il download
     * @return la risorsa del file
     * @throws IOException se si verifica un errore durante il download
     */
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId, User user) throws IOException {
        StoredFile file = getFile(fileId, user);
        return fileStorageService.loadFileAsResource(file.getFileName());
    }

    /**
     * Ottiene tutti i file di un utente, opzionalmente filtrati per cartella.
     *
     * @param user l'utente proprietario
     * @param folderId l'ID della cartella (può essere null)
     * @return la lista dei file
     */
    @Transactional(readOnly = true)
    public List<StoredFile> getUserFiles(User user, Long folderId) {
        if (folderId != null) {
            Folder folder = folderRepository.findByIdAndOwner(folderId, user)
                    .orElseThrow(() -> new RuntimeException("Cartella non trovata o non autorizzata"));
            
            return fileRepository.findByOwnerAndFolder(user, folder);
        } else {
            return fileRepository.findByOwnerAndFolderIsNull(user);
        }
    }

    /**
     * Elimina un file.
     *
     * @param fileId l'ID del file
     * @param user l'utente che richiede l'eliminazione
     * @return true se l'eliminazione ha avuto successo
     */
    @Transactional
    public boolean deleteFile(Long fileId, User user) {
        StoredFile file = fileRepository.findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new RuntimeException("File non trovato o non autorizzato"));
        
        // Elimina tutte le condivisioni associate
        permissionService.deleteAllSharePermissions(fileId, ResourceType.FILE);
        
        // Elimina il file dal database
        fileRepository.delete(file);
        
        // Elimina il file dal file system
        return fileStorageService.deleteFile(file.getFileName());
    }

    /**
     * Converte un'entità StoredFile in un DTO FileResponseDto.
     *
     * @param file l'entità StoredFile
     * @return il DTO FileResponseDto
     */
    public FileResponseDto convertToDto(StoredFile file) {
        return FileResponseDto.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .contentType(file.getContentType())
                .size(file.getSize())
                .ownerId(file.getOwner().getId())
                .ownerUsername(file.getOwner().getUsername())
                .folderId(file.getFolder() != null ? file.getFolder().getId() : null)
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    /**
     * Converte una lista di entità StoredFile in una lista di DTO FileResponseDto.
     *
     * @param files la lista di entità StoredFile
     * @return la lista di DTO FileResponseDto
     */
    public List<FileResponseDto> convertToDtoList(List<StoredFile> files) {
        return files.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}