import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FileItem } from '../models/file.model';
import { environment } from '../../environments/environment';

/**
 * Servizio per la gestione dei file.
 * Si occupa di interagire con le API del backend per operazioni
 * come upload, download, visualizzazione e cancellazione dei file.
 */
@Injectable({
  providedIn: 'root',
})
export class FileService {
  private readonly apiUrl = `${environment.apiBaseUrl}/files`;

  constructor(private http: HttpClient) {}

  /**
   * Recupera tutti i file in una specifica cartella o al livello root.
   * @param folderId - ID della cartella (opzionale)
   * @returns Un Observable con la lista dei file
   */
  getFiles(folderId?: number): Observable<FileItem[]> {
    let params = new HttpParams();
    if (folderId) {
      params = params.set('folderId', folderId.toString());
    }
    return this.http.get<FileItem[]>(this.apiUrl, { params });
  }

  /**
   * Ottiene i dettagli di un singolo file.
   * @param fileId - ID del file
   * @returns Un Observable con i dettagli del file
   */
  getFile(fileId: number): Observable<FileItem> {
    return this.http.get<FileItem>(`${this.apiUrl}/${fileId}`);
  }

  /**
   * Carica un file sul server.
   * @param file - Il file da caricare
   * @param folderId - ID della cartella di destinazione (opzionale)
   * @returns Un Observable con i dettagli del file caricato
   */
  uploadFile(file: File, folderId?: number): Observable<FileItem> {
    const formData = new FormData();
    formData.append('file', file);

    if (folderId) {
      formData.append('folderId', folderId.toString());
    }

    return this.http.post<FileItem>(`${this.apiUrl}/upload`, formData);
  }

  /**
   * Scarica un file.
   * @param fileId - ID del file da scaricare
   * @returns Un Observable con i dati binari del file
   */
  downloadFile(fileId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${fileId}/download`, {
      responseType: 'blob',
    });
  }

  /**
   * Elimina un file.
   * @param fileId - ID del file da eliminare
   * @returns Un Observable con la risposta del server
   */
  deleteFile(fileId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${fileId}`);
  }

  /**
   * Avvia il download di un file nel browser.
   * @param fileId - ID del file da scaricare
   * @param fileName - Nome del file
   */
  initiateFileDownload(fileId: number, fileName: string): void {
    this.downloadFile(fileId).subscribe((blob) => {
      // Crea un link temporaneo per il download
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();

      // Pulisce le risorse
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    });
  }
}
