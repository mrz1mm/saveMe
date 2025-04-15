import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FileService } from '../../services/file.service';
import { FolderService } from '../../services/folder.service';
import { FileItem, FolderItem, FolderRequest } from '../../models/file.model';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ResourceType } from '../../models/share.model';

/**
 * Componente principale per la visualizzazione e la navigazione tra file e cartelle.
 * Permette di visualizzare, creare, eliminare e navigare tra le risorse.
 */
@Component({
  selector: 'app-file-browser',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './file-browser.component.html',
  styleUrls: ['./file-browser.component.scss'],
})
export class FileBrowserComponent implements OnInit {
  // Dati del browser
  currentFolderId: number | null = null;
  currentFolder: FolderItem | null = null;
  breadcrumbs: FolderItem[] = [];
  folders: FolderItem[] = [];
  files: FileItem[] = [];

  // Flag di stato
  isLoading = true;
  isCreatingFolder = false;
  isUploading = false;
  uploadProgress = 0;

  // Form per la creazione di nuove cartelle
  newFolderForm: FormGroup;

  // Elementi selezionati
  selectedItems: {
    [key: number]: { type: ResourceType; item: FileItem | FolderItem };
  } = {};

  constructor(
    private fileService: FileService,
    private folderService: FolderService,
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder
  ) {
    // Inizializza il form per la creazione di nuove cartelle
    this.newFolderForm = this.formBuilder.group({
      folderName: [
        '',
        [
          Validators.required,
          Validators.minLength(1),
          Validators.maxLength(255),
        ],
      ],
    });
  }

  ngOnInit(): void {
    // Ascolta i cambiamenti nei parametri dell'URL
    this.route.queryParams.subscribe((params) => {
      const folderId = params['folderId'] ? +params['folderId'] : null;
      this.loadContent(folderId);
    });
  }

  /**
   * Carica il contenuto della cartella specificata o della root.
   * @param folderId - ID della cartella da visualizzare (null per la root)
   */
  loadContent(folderId: number | null): void {
    this.isLoading = true;
    this.currentFolderId = folderId;
    this.selectedItems = {}; // Reset della selezione

    // Carica le cartelle
    this.folderService.getFolders(folderId).subscribe({
      next: (data) => {
        this.folders = data;

        // Carica i file nella stessa cartella
        this.fileService.getFiles(folderId).subscribe({
          next: (fileData) => {
            this.files = fileData;
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Errore nel caricamento dei file:', error);
            this.isLoading = false;
          },
        });

        // Se siamo in una sottocartella, carica i dettagli della cartella corrente
        if (folderId) {
          this.folderService.getFolder(folderId).subscribe({
            next: (folder) => {
              this.currentFolder = folder;
              this.loadBreadcrumbs(folder);
            },
            error: (error) => {
              console.error(
                'Errore nel caricamento dei dettagli della cartella:',
                error
              );
            },
          });
        } else {
          this.currentFolder = null;
          this.breadcrumbs = [];
        }
      },
      error: (error) => {
        console.error('Errore nel caricamento delle cartelle:', error);
        this.isLoading = false;
      },
    });
  }

  /**
   * Gestisce la navigazione in una cartella.
   * @param folderId - ID della cartella in cui navigare
   */
  navigateToFolder(folderId: number | null): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: folderId ? { folderId } : {},
      queryParamsHandling: 'merge',
    });
  }

  /**
   * Carica le breadcrumbs per la navigazione gerarchica.
   * @param folder - Cartella corrente
   */
  loadBreadcrumbs(folder: FolderItem): void {
    this.breadcrumbs = [folder]; // Inizia con la cartella corrente

    // Se la cartella ha un parent, carica ricorsivamente la gerarchia
    if (folder.parentFolderId) {
      this.loadParentFolder(folder.parentFolderId);
    }
  }

  /**
   * Carica ricorsivamente le cartelle parent per le breadcrumbs.
   * @param parentId - ID della cartella parent
   */
  loadParentFolder(parentId: number): void {
    this.folderService.getFolder(parentId).subscribe({
      next: (parentFolder) => {
        // Inserisce all'inizio dell'array per avere l'ordine corretto
        this.breadcrumbs.unshift(parentFolder);

        // Continua ricorsivamente se c'è un altro parent
        if (parentFolder.parentFolderId) {
          this.loadParentFolder(parentFolder.parentFolderId);
        }
      },
      error: (error) => {
        console.error('Errore nel caricamento della cartella parent:', error);
      },
    });
  }

  /**
   * Mostra il form per la creazione di una nuova cartella.
   */
  showNewFolderForm(): void {
    this.isCreatingFolder = true;
    this.newFolderForm.reset();
  }

  /**
   * Crea una nuova cartella.
   */
  createNewFolder(): void {
    if (this.newFolderForm.invalid) {
      return;
    }

    const folderData: FolderRequest = {
      name: this.newFolderForm.value.folderName,
      parentFolderId: this.currentFolderId,
    };

    this.folderService.createFolder(folderData).subscribe({
      next: () => {
        // Ricarica il contenuto per mostrare la nuova cartella
        this.loadContent(this.currentFolderId);
        this.isCreatingFolder = false;
      },
      error: (error) => {
        console.error('Errore nella crezione della cartella:', error);
      },
    });
  }

  /**
   * Gestisce la selezione di un file o cartella.
   * @param item - L'elemento selezionato
   * @param type - Il tipo di risorsa
   */
  toggleSelect(item: FileItem | FolderItem, type: ResourceType): void {
    if (
      this.selectedItems[item.id] &&
      this.selectedItems[item.id].type === type
    ) {
      // Se già selezionato, rimuovi la selezione
      delete this.selectedItems[item.id];
    } else {
      // Altrimenti, seleziona
      this.selectedItems[item.id] = { type, item };
    }
  }

  /**
   * Verifica se un elemento è selezionato.
   * @param id - ID dell'elemento
   * @param type - Tipo di risorsa
   * @returns true se l'elemento è selezionato
   */
  isSelected(id: number, type: ResourceType): boolean {
    return !!(this.selectedItems[id] && this.selectedItems[id].type === type);
  }

  /**
   * Elimina gli elementi selezionati (file o cartelle).
   */
  deleteSelected(): void {
    if (Object.keys(this.selectedItems).length === 0) {
      return;
    }

    // Chiedi conferma all'utente
    if (!confirm('Sei sicuro di voler eliminare gli elementi selezionati?')) {
      return;
    }

    // Conta quanti elementi devono essere eliminati per sapere quando ricaricare
    const totalToDelete = Object.keys(this.selectedItems).length;
    let deletedCount = 0;

    // Funzione per verificare se abbiamo completato tutte le eliminazioni
    const checkAllDeleted = () => {
      deletedCount++;
      if (deletedCount === totalToDelete) {
        this.loadContent(this.currentFolderId);
      }
    };

    // Elimina ciascun elemento selezionato
    for (const id in this.selectedItems) {
      const item = this.selectedItems[id];

      if (item.type === ResourceType.FILE) {
        this.fileService.deleteFile(+id).subscribe({
          next: checkAllDeleted,
          error: (error) => {
            console.error("Errore nell'eliminazione del file:", error);
            checkAllDeleted();
          },
        });
      } else if (item.type === ResourceType.FOLDER) {
        this.folderService.deleteFolder(+id).subscribe({
          next: checkAllDeleted,
          error: (error) => {
            console.error("Errore nell'eliminazione della cartella:", error);
            checkAllDeleted();
          },
        });
      }
    }
  }

  /**
   * Gestisce l'upload di file.
   * @param event - L'evento di input file
   */
  uploadFiles(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;

    // Conta quanti file devono essere caricati per gestire il progresso
    const totalFiles = input.files.length;
    let uploadedCount = 0;

    // Carica ciascun file selezionato
    Array.from(input.files).forEach((file) => {
      this.fileService.uploadFile(file, this.currentFolderId).subscribe({
        next: () => {
          uploadedCount++;
          this.uploadProgress = Math.round((uploadedCount / totalFiles) * 100);

          // Se tutti i file sono stati caricati, ricarica il contenuto
          if (uploadedCount === totalFiles) {
            setTimeout(() => {
              this.isUploading = false;
              this.loadContent(this.currentFolderId);
            }, 500);
          }
        },
        error: (error) => {
          console.error("Errore nell'upload del file:", error);
          uploadedCount++;
          this.uploadProgress = Math.round((uploadedCount / totalFiles) * 100);
        },
      });
    });
  }

  /**
   * Avvia il download di un file.
   * @param file - Il file da scaricare
   */
  downloadFile(file: FileItem): void {
    this.fileService.initiateFileDownload(file.id, file.originalFileName);
  }

  /**
   * Formatta la dimensione del file in un formato leggibile.
   * @param bytes - Dimensione in bytes
   * @returns Dimensione formattata (es: "1.5 MB")
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
