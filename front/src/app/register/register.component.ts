import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule, NgIf } from '@angular/common';
import { Router } from '@angular/router';

interface RegisterPayload {
  email: string;
  password: string;
  genre: string;
  nom: string;
  prenom: string;
  dateNaissance: string;
  numeroSecu: string;
  groupeSanguin?: string | null;
  contactUrgence?: string | null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, NgIf, FormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  formData: RegisterPayload = {
    email: '',
    password: '',
    genre: 'HOMME',
    nom: '',
    prenom: '',
    dateNaissance: '',
    numeroSecu: '',
    groupeSanguin: '',
    contactUrgence: '',
  };

  isSubmitting = false;
  errorMessage = '';
  fieldErrors: Record<string, string> = {};
  successMessage = '';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  onSubmit(): void {
    this.errorMessage = '';
    this.fieldErrors = {};
    this.successMessage = '';
    this.isSubmitting = true;

    const payload = {
      ...this.formData,
      groupeSanguin: this.formData.groupeSanguin || null,
      contactUrgence: this.formData.contactUrgence || null,
    };

    console.log('[Register] Envoi de la requête', payload);
    this.http.post('/api/auth/register/patient', payload).subscribe({
      next: () => {
        this.successMessage =
          'Compte cree avec succes. Redirection vers la connexion...';
        setTimeout(() => {
          this.router.navigateByUrl('/login');
        }, 1200);
      },
      error: (error) => {
        console.log('[Register] Erreur HTTP', error?.status, error);
        if (error?.status === 409) {
          this.errorMessage =
            'Cet email ou numero de securite sociale est deja utilise.';
        } else if (error?.status === 400 && error?.error?.errors) {
          this.fieldErrors = error.error.errors;
        } else {
          this.errorMessage =
            'Erreur lors de la creation du compte. Verifiez vos informations.';
        }
        this.isSubmitting = false;
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  getFieldError(field: string): string {
    return this.fieldErrors[field] ?? '';
  }
}
