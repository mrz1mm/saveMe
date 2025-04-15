package com.springBoot.saveMe.model.entity;

import com.springBoot.saveMe.model.enums.PermissionType;
import com.springBoot.saveMe.model.enums.ResourceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entità che rappresenta un permesso di condivisione per una risorsa (file o cartella).
 * Permette di definire condivisioni con utenti specifici o tramite link pubblici.
 */
@Entity
@Table(name = "share_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long resourceId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id")
    private User sharedWithUser;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType permissionType;
    
    @Column(nullable = false)
    private boolean isPublicLink;
    
    @Column(unique = true)
    private String publicLinkToken;
    
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}