import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  PermissionType,
  ResourceType,
  ShareRequest,
  ShareResponse,
} from '../models/share.model';
import { environment } from '../../environments/environment';

/**
 * Servizio per la gestione delle condivisioni.
 * Si occupa di interagire con le API del backend per operazioni
 * sulle condivisioni di file e cartelle.
 */
@Injectable({
  providedIn: 'root',
})
export class ShareService {
  private readonly apiUrl = `${environment.apiBaseUrl}/shares`;

  constructor(private http: HttpClient) {}

  /**
   * Condivide un file con un altro utente o crea un link pubblico.
   * @param fileId - ID del file da condividere
   * @param shareData - Dati della condivisione
   * @returns Un Observable con i dettagli della condivisione creata
   */
  shareFile(
    fileId: number,
    shareData: ShareRequest
  ): Observable<ShareResponse> {
    return this.http.post<ShareResponse>(
      `${this.apiUrl}/file/${fileId}`,
      shareData
    );
  }

  /**
   * Condivide una cartella con un altro utente o crea un link pubblico.
   * @param folderId - ID della cartella da condividere
   * @param shareData - Dati della condivisione
   * @returns Un Observable con i dettagli della condivisione creata
   */
  shareFolder(
    folderId: number,
    shareData: ShareRequest
  ): Observable<ShareResponse> {
    return this.http.post<ShareResponse>(
      `${this.apiUrl}/folder/${folderId}`,
      shareData
    );
  }

  /**
   * Ottiene tutte le condivisioni di un file.
   * @param fileId - ID del file
   * @returns Un Observable con la lista delle condivisioni
   */
  getFileShares(fileId: number): Observable<ShareResponse[]> {
    return this.http.get<ShareResponse[]>(`${this.apiUrl}/file/${fileId}`);
  }

  /**
   * Ottiene tutte le condivisioni di una cartella.
   * @param folderId - ID della cartella
   * @returns Un Observable con la lista delle condivisioni
   */
  getFolderShares(folderId: number): Observable<ShareResponse[]> {
    return this.http.get<ShareResponse[]>(`${this.apiUrl}/folder/${folderId}`);
  }

  /**
   * Elimina una condivisione.
   * @param shareId - ID della condivisione da eliminare
   * @returns Un Observable con la risposta del server
   */
  deleteShare(shareId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${shareId}`);
  }

  /**
   * Crea un oggetto ShareRequest con i dati predefiniti per una condivisione pubblica in sola lettura.
   * @returns Un oggetto ShareRequest configurato per condivisione pubblica in sola lettura
   */
  createPublicReadShareRequest(): ShareRequest {
    return {
      permissionType: PermissionType.READ,
      isPublicLink: true,
    };
  }

  /**
   * Crea un oggetto ShareRequest con i dati predefiniti per una condivisione con un utente specifico in sola lettura.
   * @param userId - ID dell'utente con cui condividere
   * @returns Un oggetto ShareRequest configurato per condivisione con un utente specifico
   */
  createUserShareRequest(userId: number): ShareRequest {
    return {
      sharedWithUserId: userId,
      permissionType: PermissionType.READ,
      isPublicLink: false,
    };
  }
}
