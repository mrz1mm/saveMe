package com.springBoot.saveMe.dto.share;

import com.springBoot.saveMe.model.enums.PermissionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO per la richiesta di creazione di una condivisione.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareRequestDto {
    
    private Long sharedWithUserId;
    
    @NotNull(message = "Il tipo di permesso è obbligatorio")
    private PermissionType permissionType;
    
    private boolean isPublicLink;
    
    private LocalDateTime expiresAt;
}