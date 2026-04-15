import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface RegisterPayload {
  email: string;
  password: string;
  genre: string;
  nom: string;
  prenom: string;
  dateNaissance: string;
  numeroSecu: string;
  groupeSanguin?: string;
  contactUrgence?: string;
}

@Component({
  selector: 'app-register',
  imports: [FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  formData: RegisterPayload = {
    email: '',
    password: '',
    genre: '',
    nom: '',
    prenom: '',
    dateNaissance: '',
    numeroSecu: '',
    groupeSanguin: '',
    contactUrgence: '',
  };

  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    this.isSubmitting = true;

    this.http.post('/api/auth/register/patient', this.formData).subscribe({
      next: () => {
        this.successMessage =
          'Compte cree avec succes. Redirection vers la connexion...';
        setTimeout(() => {
          this.router.navigateByUrl('/login');
        }, 1200);
      },
      error: (error) => {
        if (error?.status === 409) {
          this.errorMessage =
            'Cet email ou numero de securite sociale est deja utilise.';
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
}
