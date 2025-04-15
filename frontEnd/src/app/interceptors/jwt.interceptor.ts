import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor HTTP che aggiunge automaticamente il token JWT nell'header Authorization
 * per tutte le richieste alle API protette.
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  /**
   * Intercetta le richieste HTTP e aggiunge il token JWT se disponibile.
   * @param request La richiesta HTTP originale
   * @param next L'handler per continuare la catena delle richieste
   * @returns Un Observable con la risposta HTTP
   */
  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Ottiene il token dall'AuthService
    const token = this.authService.getToken();

    if (token) {
      // Clona la richiesta e aggiunge l'header Authorization con il token JWT
      const authRequest = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });

      // Continua con la richiesta modificata
      return next.handle(authRequest);
    }

    // Se non c'è token, continua con la richiesta originale
    return next.handle(request);
  }
}
