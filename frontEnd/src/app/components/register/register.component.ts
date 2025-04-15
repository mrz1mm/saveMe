import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest } from '../../models/auth.model';
import { CommonModule } from '@angular/common';

/**
 * Componente per la registrazione di nuovi utenti.
 * Fornisce un form con i campi necessari per la registrazione
 * e interagisce con AuthService per creare un nuovo account.
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent implements OnInit {
  // Form di registrazione
  registerForm: FormGroup;
  // Flag per indicare se c'è stato un errore di registrazione
  isRegisterError = false;
  // Messaggio di errore
  errorMessage = '';
  // Flag per indicare se la registrazione è stata completata con successo
  isSuccess = false;
  // Flag per indicare se è in corso un tentativo di registrazione
  isLoading = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    // Inizializza il form con le validazioni
    this.registerForm = this.formBuilder.group(
      {
        username: ['', [Validators.required, Validators.minLength(3)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      {
        validators: this.checkPasswords, // Validazione custom per il match delle password
      }
    );
  }

  ngOnInit(): void {
    // Se l'utente è già autenticato, reindirizza alla dashboard
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  /**
   * Gestisce il submit del form di registrazione
   */
  onSubmit(): void {
    // Resetta gli eventuali errori precedenti
    this.isRegisterError = false;
    this.errorMessage = '';
    this.isSuccess = false;

    // Verifica se il form è valido
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;

    // Crea l'oggetto con i dati di registrazione
    const registerData: RegisterRequest = {
      username: this.registerForm.value.username,
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
    };

    // Effettua la chiamata al servizio di autenticazione
    this.authService.register(registerData).subscribe({
      next: () => {
        // Registrazione riuscita
        this.isLoading = false;
        this.isSuccess = true;

        // Reindirizza al login dopo un breve intervallo
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        // Gestione dell'errore
        this.isLoading = false;
        this.isRegisterError = true;
        this.errorMessage =
          error.error || 'Errore durante la registrazione. Riprova più tardi.';
      },
    });
  }

  /**
   * Funzione di validazione custom per verificare che le password coincidano.
   * @param group FormGroup contenente i campi password e confirmPassword
   * @returns null se le password coincidono, altrimenti oggetto con errore
   */
  private checkPasswords(group: FormGroup): { notMatching: boolean } | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;

    return password === confirmPassword ? null : { notMatching: true };
  }
}
