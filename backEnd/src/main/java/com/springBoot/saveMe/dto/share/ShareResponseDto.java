package com.springBoot.saveMe.dto.share;

import com.springBoot.saveMe.model.enums.PermissionType;
import com.springBoot.saveMe.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO per la risposta contenente i dati di una condivisione.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareResponseDto {
    
    private Long id;
    private Long resourceId;
    private ResourceType resourceType;
    private String resourceName;
    private Long sharedWithUserId;
    private String sharedWithUsername;
    private PermissionType permissionType;
    private boolean isPublicLink;
    private String publicLinkToken;
    private String publicLinkUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}