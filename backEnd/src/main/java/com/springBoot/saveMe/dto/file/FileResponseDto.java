package com.springBoot.saveMe.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO per la risposta contenente i metadati di un file.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDto {
    
    private Long id;
    private String originalFileName;
    private String contentType;
    private Long size;
    private Long ownerId;
    private String ownerUsername;
    private Long folderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}