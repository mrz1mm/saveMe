package com.springBoot.saveMe.service;

import com.springBoot.saveMe.dto.folder.FolderResponseDto;
import com.springBoot.saveMe.model.entity.Folder;
import com.springBoot.saveMe.model.entity.StoredFile;
import com.springBoot.saveMe.model.entity.User;
import com.springBoot.saveMe.model.enums.ResourceType;
import com.springBoot.saveMe.repository.FolderRepository;
import com.springBoot.saveMe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione delle cartelle.
 * Gestisce la creazione, l'aggiornamento, la visualizzazione e l'eliminazione delle cartelle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final FolderRepository folderRepository;
    private final StoredFileRepository fileRepository;
    private final PermissionService permissionService;

    /**
     * Crea una nuova cartella.
     *
     * @param name il nome della cartella
     * @param parentFolderId l'ID della cartella genitore (può essere null)
     * @param owner l'utente proprietario
     * @return la cartella creata
     */
    @Transactional
    public Folder createFolder(String name, Long parentFolderId, User owner) {
        // Verifica la cartella genitore se specificata
        Folder parentFolder = null;
        if (parentFolderId != null) {
            parentFolder = folderRepository.findByIdAndOwner(parentFolderId, owner)
                    .orElseThrow(() -> new RuntimeException("Cartella genitore non trovata o non autorizzata"));
        }
        
        // Crea la nuova cartella
        Folder folder = Folder.builder()
                .name(name)
                .owner(owner)
                .parentFolder(parentFolder)
                .build();
        
        return folderRepository.save(folder);
    }

    /**
     * Aggiorna una cartella esistente.
     *
     * @param folderId l'ID della cartella
     * @param name il nuovo nome
     * @param parentFolderId l'ID della nuova cartella genitore (può essere null)
     * @param user l'utente che richiede l'aggiornamento
     * @return la cartella aggiornata
     */
    @Transactional
    public Folder updateFolder(Long folderId, String name, Long parentFolderId, User user) {
        // Trova la cartella da aggiornare
        Folder folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new RuntimeException("Cartella non trovata o non autorizzata"));
        
        // Verifica che la nuova cartella genitore non sia la cartella stessa o una sua discendente
        if (parentFolderId != null && (parentFolderId.equals(folderId) || isFolderDescendant(parentFolderId, folderId, user))) {
            throw new RuntimeException("Operazione non valida: la cartella genitore non può essere la cartella stessa o una sua discendente");
        }
        
        // Verifica la nuova cartella genitore se specificata
        Folder parentFolder = null;
        if (parentFolderId != null) {
            parentFolder = folderRepository.findByIdAndOwner(parentFolderId, user)
                    .orElseThrow(() -> new RuntimeException("Cartella genitore non trovata o non autorizzata"));
        }
        
        // Aggiorna la cartella
        folder.setName(name);
        folder.setParentFolder(parentFolder);
        
        return folderRepository.save(folder);
    }

    /**
     * Ottiene una cartella.
     *
     * @param folderId l'ID della cartella
     * @param user l'utente che richiede la cartella
     * @return la cartella
     */
    @Transactional(readOnly = true)
    public Folder getFolder(Long folderId, User user) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Cartella non trovata"));
        
        // Verifica se l'utente è il proprietario o ha accesso condiviso
        if (folder.getOwner().getId().equals(user.getId()) || 
                permissionService.hasUserAccess(folderId, ResourceType.FOLDER, user.getId())) {
            return folder;
        }
        
        throw new RuntimeException("Accesso non autorizzato alla cartella");
    }

    /**
     * Ottiene tutte le cartelle di un utente, opzionalmente filtrate per cartella genitore.
     *
     * @param user l'utente proprietario
     * @param parentFolderId l'ID della cartella genitore (può essere null)
     * @return la lista delle cartelle
     */
    @Transactional(readOnly = true)
    public List<Folder> getUserFolders(User user, Long parentFolderId) {
        if (parentFolderId != null) {
            Folder parentFolder = folderRepository.findByIdAndOwner(parentFolderId, user)
                    .orElseThrow(() -> new RuntimeException("Cartella genitore non trovata o non autorizzata"));
            
            return folderRepository.findByOwnerAndParentFolder(user, parentFolder);
        } else {
            return folderRepository.findByOwnerAndParentFolderIsNull(user);
        }
    }

    /**
     * Elimina una cartella e tutto il suo contenuto.
     *
     * @param folderId l'ID della cartella
     * @param user l'utente che richiede l'eliminazione
     */
    @Transactional
    public void deleteFolder(Long folderId, User user) {
        Folder folder = folderRepository.findByIdAndOwner(folderId, user)
                .orElseThrow(() -> new RuntimeException("Cartella non trovata o non autorizzata"));
        
        // Elimina ricorsivamente tutte le sottocartelle
        List<Folder> subfolders = folderRepository.findByOwnerAndParentFolder(user, folder);
        for (Folder subfolder : subfolders) {
            deleteFolder(subfolder.getId(), user);
        }
        
        // Elimina tutti i file nella cartella
        List<StoredFile> files = fileRepository.findByOwnerAndFolder(user, folder);
        for (StoredFile file : files) {
            // Elimina le condivisioni del file
            permissionService.deleteAllSharePermissions(file.getId(), ResourceType.FILE);
            // Elimina il file dal file system
            // Non controlliamo il risultato qui perché vogliamo continuare l'eliminazione
            // anche se un file non può essere eliminato dal file system
            fileRepository.delete(file);
        }
        
        // Elimina le condivisioni della cartella
        permissionService.deleteAllSharePermissions(folderId, ResourceType.FOLDER);
        
        // Elimina la cartella
        folderRepository.delete(folder);
    }
    
    /**
     * Verifica se una cartella è discendente di un'altra.
     *
     * @param possibleDescendantId l'ID della possibile cartella discendente
     * @param ancestorId l'ID della cartella antenata
     * @param user l'utente proprietario
     * @return true se la prima cartella è discendente della seconda
     */
    private boolean isFolderDescendant(Long possibleDescendantId, Long ancestorId, User user) {
        Folder folder = folderRepository.findByIdAndOwner(possibleDescendantId, user)
                .orElseThrow(() -> new RuntimeException("Cartella non trovata o non autorizzata"));
        
        if (folder.getParentFolder() == null) {
            return false;
        }
        
        if (folder.getParentFolder().getId().equals(ancestorId)) {
            return true;
        }
        
        return isFolderDescendant(folder.getParentFolder().getId(), ancestorId, user);
    }

    /**
     * Converte un'entità Folder in un DTO FolderResponseDto.
     *
     * @param folder l'entità Folder
     * @return il DTO FolderResponseDto
     */
    public FolderResponseDto convertToDto(Folder folder) {
        return FolderResponseDto.builder()
                .id(folder.getId())
                .name(folder.getName())
                .ownerId(folder.getOwner().getId())
                .ownerUsername(folder.getOwner().getUsername())
                .parentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }

    /**
     * Converte una lista di entità Folder in una lista di DTO FolderResponseDto.
     *
     * @param folders la lista di entità Folder
     * @return la lista di DTO FolderResponseDto
     */
    public List<FolderResponseDto> convertToDtoList(List<Folder> folders) {
        return folders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}