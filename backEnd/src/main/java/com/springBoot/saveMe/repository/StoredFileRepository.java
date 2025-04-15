package com.springBoot.saveMe.repository;

import com.springBoot.saveMe.model.entity.Folder;
import com.springBoot.saveMe.model.entity.StoredFile;
import com.springBoot.saveMe.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'accesso e la manipolazione dei file.
 */
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    /**
     * Trova tutti i file di un utente.
     * 
     * @param owner l'utente proprietario
     * @return una lista di file
     */
    List<StoredFile> findByOwner(User owner);
    
    /**
     * Trova tutti i file di un utente in una cartella specifica.
     * 
     * @param owner l'utente proprietario
     * @param folder la cartella
     * @return una lista di file
     */
    List<StoredFile> findByOwnerAndFolder(User owner, Folder folder);
    
    /**
     * Trova tutti i file di primo livello di un utente (senza cartella).
     * 
     * @param owner l'utente proprietario
     * @return una lista di file
     */
    List<StoredFile> findByOwnerAndFolderIsNull(User owner);
    
    /**
     * Verifica se un file appartiene a un utente specifico.
     * 
     * @param id l'id del file
     * @param ownerId l'id dell'utente
     * @return true se il file appartiene all'utente, false altrimenti
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM StoredFile f WHERE f.id = :id AND f.owner.id = :ownerId")
    boolean isOwnedByUser(@Param("id") Long id, @Param("ownerId") Long ownerId);
    
    /**
     * Trova un file per id e proprietario.
     * 
     * @param id l'id del file
     * @param owner l'utente proprietario
     * @return un Optional contenente il file se trovato
     */
    Optional<StoredFile> findByIdAndOwner(Long id, User owner);
}