import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.model';
import { CommonModule } from '@angular/common';

/**
 * Componente per la gestione del login degli utenti.
 * Fornisce un form di accesso e interagisce con AuthService per autenticare l'utente.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  // Form di login
  loginForm: FormGroup;
  // Flag per indicare se c'è stato un errore di login
  isLoginError = false;
  // Messaggio di errore
  errorMessage = '';
  // Flag per indicare se è in corso un tentativo di login
  isLoading = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    // Inizializza il form
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnInit(): void {
    // Se l'utente è già autenticato, reindirizza alla dashboard
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  /**
   * Gestisce il submit del form di login
   */
  onSubmit(): void {
    // Resetta gli eventuali errori precedenti
    this.isLoginError = false;
    this.errorMessage = '';

    // Verifica se il form è valido
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading = true;

    // Crea l'oggetto con i dati di login
    const loginData: LoginRequest = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password,
    };

    // Effettua la chiamata al servizio di autenticazione
    this.authService.login(loginData).subscribe({
      next: () => {
        // Login riuscito, reindirizza alla dashboard
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        // Gestione dell'errore
        this.isLoading = false;
        this.isLoginError = true;
        this.errorMessage =
          error.error || 'Errore durante il login. Verifica le credenziali.';
      },
    });
  }
}
