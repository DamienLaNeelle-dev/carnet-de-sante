import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MedecinService } from '../medecin.service';

interface Patient {
  nom: string;
  prenom: string;
  genre?: string;
  dateNaissance: string;
  groupeSanguin?: string | null;
  numeroSecu: string;
}

interface Medecin {
  id?: number;
  nom: string;
  prenom: string;
  specialite: string;
  numeroRpps: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private token = localStorage.getItem('token');
  private userId = localStorage.getItem('userId');
  userEmail = localStorage.getItem('email') ?? '';
  hasMedecinTraitant = false;

  get currentToken(): string | null {
    return localStorage.getItem('token');
  }

  get currentUserId(): string | null {
    return localStorage.getItem('userId');
  }

  patient: Patient | null = null;
  userName = 'Chargement...';
  isLoading = true;
  errorMessage = 'Patient non trouve.';

  medecin: Medecin | null = null; // Médecin assigné au patient
  medecins: Medecin[] = [];
  searchQuery = '';
  filteredMedecins: Medecin[] = [];
  selectedMedecinInModal: Medecin | null = null; // Médecin temporaire dans la modale

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private medecinService: MedecinService,
  ) {
    this.medecinService.getMedecins().subscribe({
      next: (medecins) => {
        this.medecins = medecins;
      },
    });
  }

  ngOnInit(): void {
    if (!this.currentToken || !this.currentUserId) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.loadPatientInfo();
    this.loadMedecins();
    this.loadMedecinInfo();
  }

  loadPatientInfo(): void {
    if (!this.currentToken || !this.currentUserId) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    this.http
      .get<Patient>(`/api/patients/by-user/${this.currentUserId}`, {
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

  loadMedecinInfo(): void {
    if (!this.currentToken || !this.currentUserId) {
      return;
    }

    this.http
      .get<Medecin>(`/api/medecins/by-patient/${this.currentUserId}`, {
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.currentToken}`,
        }),
      })
      .subscribe({
        next: (medecin) => {
          this.medecin = medecin;
        },
        error: (error) => {
          if (error?.status === 401) {
            this.logout();
            return;
          }
          // Pas d'affichage d'erreur si aucun médecin trouvé, c'est normal
        },
      });
  }

  filterMedecins(query: string): void {
    this.searchQuery = query;
    if (!query.trim()) {
      this.filteredMedecins = [];
      return;
    }

    const lowerQuery = query.toLowerCase();
    this.filteredMedecins = this.medecins.filter((m) => {
      const fullName = `${m.nom} ${m.prenom}`.toLowerCase();
      return fullName.includes(lowerQuery);
    });
  }

  selectMedecin(selectedMedecin: Medecin): void {
    this.selectedMedecinInModal = selectedMedecin;
    this.searchQuery = `${selectedMedecin.nom} ${selectedMedecin.prenom}`;
    this.filteredMedecins = [];
  }

  saveMedecinTraitant(medecin: Medecin): void {
    console.log('saveMedecinTraitant appelée avec:', medecin);
    console.log('currentToken:', this.currentToken);
    console.log('currentUserId:', this.currentUserId);

    this.http
      .post(
        `/api/patients/${this.currentUserId}/medecin-traitant`,
        { numeroRPPS: medecin.numeroRpps },
        {
          headers: new HttpHeaders({
            Authorization: `Bearer ${this.currentToken}`,
          }),
        },
      )
      .subscribe({
        next: () => {
          console.log('Réponse reçue du serveur');
          this.medecin = this.selectedMedecinInModal;
          console.log('this.medecin assigné à:', this.medecin);

          // Fermer la modale en cliquant le bouton de fermeture
          const closeButton = document.querySelector(
            '[data-bs-dismiss="modal"]',
          ) as HTMLButtonElement | null;
          if (closeButton) {
            console.log('Clic sur le bouton de fermeture');
            closeButton.click();
          }

          // Réinitialiser l'état après un léger délai
          setTimeout(() => {
            this.searchQuery = '';
            this.filteredMedecins = [];
            this.selectedMedecinInModal = null;
            console.log('État réinitialisé');
          }, 100);
        },
        error: (error) => {
          console.error('Erreur HTTP:', error);
          if (error?.status === 401) {
            this.logout();
            return;
          }
        },
      });
  }

  formatDate(date: string | undefined): string {
    if (!date) {
      return '-';
    }

    return new Date(date).toLocaleDateString('fr-FR');
  }

  formatGenre(genre: string | null | undefined): string {
    if (!genre) {
      return '-';
    }

    return genre.charAt(0).toUpperCase() + genre.slice(1).toLowerCase();
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

  loadMedecins(): void {
    this.http.get<Medecin[]>(`/api/medecins`).subscribe({
      next: (medecins) => {
        // Filtrer pour ne garder que les généralistes
        this.medecins = medecins.filter((m) => m.specialite === 'GENERALISTE');
      },
      error: (error) => {
        console.error('Erreur lors du chargement des medecins', error);
      },
    });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    this.router.navigateByUrl('/login');
  }
}
