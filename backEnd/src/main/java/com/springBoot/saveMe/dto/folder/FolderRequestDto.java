package com.springBoot.saveMe.dto.folder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la creazione o l'aggiornamento di una cartella.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderRequestDto {

    @NotBlank(message = "Il nome della cartella è obbligatorio")
    @Size(min = 1, max = 255, message = "Il nome della cartella deve essere compreso tra 1 e 255 caratteri")
    private String name;
    
    private Long parentFolderId;
}