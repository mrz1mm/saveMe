package com.springBoot.saveMe.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO per la risposta contenente i dati di una cartella.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderResponseDto {
    
    private Long id;
    private String name;
    private Long ownerId;
    private String ownerUsername;
    private Long parentFolderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}