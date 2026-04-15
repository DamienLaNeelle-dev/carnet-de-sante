import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

interface LoginResponse {
  token: string;
  email: string;
  role: string;
  userId: string | number;
}

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  email = '';
  password = '';
  isSubmitting = false;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  onSubmit(): void {
    this.errorMessage = '';

    if (!this.email || !this.password) {
      this.errorMessage =
        'Veuillez renseigner votre email et votre mot de passe.';
      return;
    }

    this.isSubmitting = true;

    this.authService.login(this.email, this.password).subscribe({
      next: (data) => {
        if (data.role === 'PATIENT') {
          this.router.navigateByUrl('/dashboard');
          return;
        }

        if (data.role === 'ADMIN') {
          this.router.navigateByUrl('/admin/dashboard');
          return;
        }

        this.router.navigateByUrl('/');
      },
      error: (error) => {
        if (error?.status === 401 || error?.status === 403) {
          this.errorMessage = 'Email ou mot de passe incorrect.';
        } else {
          this.errorMessage = 'Erreur de connexion au serveur.';
        }
        this.isSubmitting = false;
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }
}
