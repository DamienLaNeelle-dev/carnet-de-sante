import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

interface Patient {
  nom: string;
  prenom: string;
  dateNaissance: string;
  groupeSanguin?: string | null;
  numeroSecu: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  token = localStorage.getItem('token');
  userId = localStorage.getItem('userId');
  userEmail = localStorage.getItem('email') ?? '';

  patient: Patient | null = null;
  userName = 'Chargement...';
  isLoading = true;
  errorMessage = '';

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    if (!this.token || !this.userId) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.loadPatientInfo();
  }

  loadPatientInfo(): void {
    if (!this.token || !this.userId) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    this.http
      .get<Patient>(`/api/patients/by-user/${this.userId}`, {
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.token}`,
        }),
      })
      .subscribe({
        next: (patient) => {
          this.patient = patient;
          this.userName = `${patient.prenom} ${patient.nom}`;
        },
        error: (error) => {
          if (error?.status === 401) {
            this.logout();
            return;
          }

          this.errorMessage = 'Erreur lors du chargement des donnees patient.';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        },
      });
  }

  formatDate(date: string | undefined): string {
    if (!date) {
      return '-';
    }

    return new Date(date).toLocaleDateString('fr-FR');
  }

  formatBloodGroup(groupeSanguin: string | null | undefined): string {
    if (!groupeSanguin) {
      return 'Non renseigne';
    }

    return groupeSanguin.replace('_', ' ');
  }

  formatNumeroSecu(numeroSecu: string | undefined): string {
    if (!numeroSecu) {
      return '-';
    }

    return numeroSecu.replace(
      /(\d{1})(\d{2})(\d{2})(\d{2})(\d{3})(\d{3})(\d{2})/g,
      '$1 $2 $3 $4 $5 $6 $7',
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    this.router.navigateByUrl('/login');
  }
}
