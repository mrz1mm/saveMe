import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '../models/auth.model';
import { environment } from '../../environments/environment';

/**
 * Servizio responsabile dell'autenticazione e della gestione del token JWT.
 * Si occupa di interagire con le API di autenticazione del backend e di mantenere
 * lo stato di autenticazione dell'utente nell'applicazione.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';
  private readonly apiUrl = `${environment.apiBaseUrl}/auth`;

  // BehaviorSubject per tracciare lo stato di autenticazione
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  // BehaviorSubject per tracciare se l'utente è autenticato
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {
    // Controlla se l'utente è già autenticato al caricamento del servizio
    this.checkAuthStatus();
  }

  /**
   * Registra un nuovo utente
   * @param registerData - Dati di registrazione dell'utente
   * @returns Observable con la risposta del server
   */
  register(registerData: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, registerData);
  }

  /**
   * Esegue il login di un utente
   * @param loginData - Credenziali di accesso
   * @returns Observable con i dati di autenticazione
   */
  login(loginData: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, loginData)
      .pipe(tap((response) => this.setSession(response)));
  }

  /**
   * Esegue il logout dell'utente rimuovendo i dati di sessione
   */
  logout(): void {
    // Rimuove token e dati utente dal localStorage
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);

    // Aggiorna i subject
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Recupera il token JWT corrente
   * @returns Il token JWT o null
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Verifica se l'utente è autenticato
   * @returns true se l'utente è autenticato, false altrimenti
   */
  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  /**
   * Verifica lo stato di autenticazione al caricamento dell'app
   */
  private checkAuthStatus(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userJson = localStorage.getItem(this.USER_KEY);

    if (token && userJson) {
      try {
        const user = JSON.parse(userJson) as User;
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
      } catch (e) {
        // In caso di errore, rimuove i dati potenzialmente corrotti
        this.logout();
      }
    }
  }

  /**
   * Salva i dati di sessione dopo un login riuscito
   * @param authResponse - Risposta dell'API di autenticazione
   */
  private setSession(authResponse: AuthResponse): void {
    // Salva il token nel localStorage
    localStorage.setItem(this.TOKEN_KEY, authResponse.token);

    // Crea e salva l'oggetto utente
    const user: User = {
      id: authResponse.id,
      username: authResponse.username,
      email: authResponse.email,
    };
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));

    // Aggiorna i subject
    this.currentUserSubject.next(user);
    this.isAuthenticatedSubject.next(true);
  }
}
