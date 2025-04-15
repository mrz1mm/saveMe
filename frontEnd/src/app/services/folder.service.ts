import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FolderItem, FolderRequest } from '../models/file.model';
import { environment } from '../../environments/environment';

/**
 * Servizio per la gestione delle cartelle.
 * Si occupa di interagire con le API del backend per operazioni
 * come creazione, aggiornamento, visualizzazione e cancellazione delle cartelle.
 */
@Injectable({
  providedIn: 'root',
})
export class FolderService {
  private readonly apiUrl = `${environment.apiBaseUrl}/folders`;

  constructor(private http: HttpClient) {}

  /**
   * Recupera tutte le cartelle in una specifica cartella parent o al livello root.
   * @param parentFolderId - ID della cartella parent (opzionale)
   * @returns Un Observable con la lista delle cartelle
   */
  getFolders(parentFolderId?: number): Observable<FolderItem[]> {
    let params = new HttpParams();
    if (parentFolderId) {
      params = params.set('parentFolderId', parentFolderId.toString());
    }
    return this.http.get<FolderItem[]>(this.apiUrl, { params });
  }

  /**
   * Ottiene i dettagli di una singola cartella.
   * @param folderId - ID della cartella
   * @returns Un Observable con i dettagli della cartella
   */
  getFolder(folderId: number): Observable<FolderItem> {
    return this.http.get<FolderItem>(`${this.apiUrl}/${folderId}`);
  }

  /**
   * Crea una nuova cartella.
   * @param folderData - Dati della cartella da creare
   * @returns Un Observable con i dettagli della cartella creata
   */
  createFolder(folderData: FolderRequest): Observable<FolderItem> {
    return this.http.post<FolderItem>(this.apiUrl, folderData);
  }

  /**
   * Aggiorna una cartella esistente.
   * @param folderId - ID della cartella da aggiornare
   * @param folderData - Nuovi dati della cartella
   * @returns Un Observable con i dettagli della cartella aggiornata
   */
  updateFolder(
    folderId: number,
    folderData: FolderRequest
  ): Observable<FolderItem> {
    return this.http.put<FolderItem>(`${this.apiUrl}/${folderId}`, folderData);
  }

  /**
   * Elimina una cartella.
   * @param folderId - ID della cartella da eliminare
   * @returns Un Observable con la risposta del server
   */
  deleteFolder(folderId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${folderId}`);
  }
}
