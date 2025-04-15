package com.springBoot.saveMe.repository;

import com.springBoot.saveMe.model.entity.Folder;
import com.springBoot.saveMe.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository per l'accesso e la manipolazione delle cartelle.
 */
public interface FolderRepository extends JpaRepository<Folder, Long> {

    /**
     * Trova tutte le cartelle di un utente specifico.
     * 
     * @param owner l'utente proprietario
     * @return una lista di cartelle
     */
    List<Folder> findByOwner(User owner);
    
    /**
     * Trova tutte le cartelle di un utente in una cartella specifica.
     * 
     * @param owner l'utente proprietario
     * @param parentFolder la cartella genitore
     * @return una lista di cartelle
     */
    List<Folder> findByOwnerAndParentFolder(User owner, Folder parentFolder);
    
    /**
     * Trova tutte le cartelle di primo livello di un utente (senza cartella genitore).
     * 
     * @param owner l'utente proprietario
     * @return una lista di cartelle
     */
    List<Folder> findByOwnerAndParentFolderIsNull(User owner);
    
    /**
     * Verifica se una cartella appartiene a un utente specifico.
     * 
     * @param id l'id della cartella
     * @param ownerId l'id dell'utente
     * @return true se la cartella appartiene all'utente, false altrimenti
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Folder f WHERE f.id = :id AND f.owner.id = :ownerId")
    boolean isOwnedByUser(@Param("id") Long id, @Param("ownerId") Long ownerId);
    
    /**
     * Trova una cartella per id e proprietario.
     * 
     * @param id l'id della cartella
     * @param owner l'utente proprietario
     * @return un Optional contenente la cartella se trovata
     */
    Optional<Folder> findByIdAndOwner(Long id, User owner);
}