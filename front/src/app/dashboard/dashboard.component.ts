import {
  HttpClient,
  HttpHeaders,
  HttpEvent,
  HttpEventType,
} from '@angular/common/http';
import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '../auth.service';
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

interface PatientFile {
  id: number;
  fileName: string;
  filePath: string;
  fileType: string;
  fileSize: number;
  uploadedAt: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild('medecinModalClose')
  private medecinModalClose!: ElementRef<HTMLButtonElement>;

  userEmail = localStorage.getItem('email') ?? '';
  hasMedecinTraitant = false;
  fileName = '';
  uploadProgress = 0;
  isUploading = false;
  uploadError = '';
  files: PatientFile[] = [];
  selectedFile: PatientFile | null = null;
  previewUrl: SafeResourceUrl | null = null;
  isDragging = false;
  activeSection: 'informations' | 'documents' | 'rdv' = 'informations';

  private currentBlobUrl: string | null = null;

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

  medecin: Medecin | null = null;
  medecins: Medecin[] = [];
  searchQuery = '';
  filteredMedecins: Medecin[] = [];
  selectedMedecinInModal: Medecin | null = null;

  title = 'front';
  isLoggedIn = false;

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly sanitizer: DomSanitizer,
    private authService: AuthService,
    private readonly medecinService: MedecinService,
  ) {
    this.authService
      .isLoggedIn()
      .subscribe((val: boolean) => (this.isLoggedIn = val));
  }

  ngOnInit(): void {
    if (!this.currentToken || !this.currentUserId) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.loadPatientInfo();
    this.loadMedecins();
    this.loadMedecinInfo();
    this.loadFiles();
  }

  ngOnDestroy(): void {
    if (this.currentBlobUrl) {
      window.URL.revokeObjectURL(this.currentBlobUrl);
    }
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
          Authorization: `Bearer ${this.currentToken}`,
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
          }
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
          this.medecin = this.selectedMedecinInModal;
          this.medecinModalClose.nativeElement.click();
          this.searchQuery = '';
          this.filteredMedecins = [];
          this.selectedMedecinInModal = null;
        },
        error: (error) => {
          if (error?.status === 401) {
            this.logout();
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
    this.medecinService.getMedecins().subscribe({
      next: (medecins) => {
        this.medecins = medecins.filter(
          (m: Medecin) => m.specialite === 'GENERALISTE',
        );
      },
      error: (error) => {
        console.error('Erreur lors du chargement des medecins', error);
      },
    });
  }

  loadFiles(): void {
    if (!this.currentUserId || !this.currentToken) {
      return;
    }

    this.http
      .get<PatientFile[]>(`/api/files/${this.currentUserId}`, {
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.currentToken}`,
        }),
      })
      .subscribe({
        next: (files) => {
          this.files = files;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des fichiers', error);
        },
      });
  }

  downloadFile(file: PatientFile): void {
    if (!this.currentToken) {
      return;
    }

    this.http
      .get(`/api/files/download/${file.id}`, {
        responseType: 'blob',
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.currentToken}`,
        }),
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = file.fileName;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (error) => {
          console.error('Erreur lors du téléchargement du fichier', error);
        },
      });
  }

  openFile(file: PatientFile): void {
    if (!this.currentToken) {
      return;
    }

    if (this.currentBlobUrl) {
      window.URL.revokeObjectURL(this.currentBlobUrl);
      this.currentBlobUrl = null;
    }

    this.selectedFile = file;

    this.http
      .get(`/api/files/download/${file.id}`, {
        responseType: 'blob',
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.currentToken}`,
        }),
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          this.currentBlobUrl = url;
          this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        },
        error: (error) => {
          console.error("Erreur lors de l'ouverture du fichier", error);
        },
      });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      const fakeEvent = { target: { files: files } };
      this.fileSended(fakeEvent);
    }
  }

  deleteFile(file: PatientFile): void {
    if (!this.currentToken) {
      return;
    }

    if (!confirm('Êtes-vous sûr de vouloir supprimer ce document ?')) {
      return;
    }

    this.http
      .delete(`/api/files/${file.id}`, {
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.currentToken}`,
        }),
      })
      .subscribe({
        next: () => {
          this.files = this.files.filter((f) => f.id !== file.id);
          if (this.selectedFile?.id === file.id) {
            this.selectedFile = null;
            if (this.currentBlobUrl) {
              window.URL.revokeObjectURL(this.currentBlobUrl);
              this.currentBlobUrl = null;
            }
            this.previewUrl = null;
          }
        },
        error: (error) => {
          console.error('Erreur lors de la suppression du fichier', error);
          alert('Erreur lors de la suppression');
        },
      });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  fileSended(event: any): void {
    const uploadedFile = event.target.files[0] as globalThis.File;

    if (!uploadedFile) {
      return;
    }

    if (
      !uploadedFile.type.includes('pdf') &&
      !uploadedFile.name.toLowerCase().endsWith('.pdf')
    ) {
      this.uploadError = 'Veuillez sélectionner un fichier PDF';
      return;
    }

    const maxSize = 10 * 1024 * 1024;
    if (uploadedFile.size > maxSize) {
      this.uploadError = 'Le fichier doit faire moins de 10MB';
      return;
    }

    this.fileName = uploadedFile.name;
    this.uploadError = '';
    this.isUploading = true;
    this.uploadProgress = 0;

    const formData = new FormData();
    formData.append('thumbnail', uploadedFile);
    formData.append('patientId', this.currentUserId || '');

    this.http
      .post<HttpEvent<any>>('/api/thumbnail-upload', formData, {
        reportProgress: true,
        observe: 'events',
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.currentToken}`,
        }),
      })
      .subscribe({
        next: (httpEvent: HttpEvent<any>) => {
          if (httpEvent.type === HttpEventType.UploadProgress) {
            this.uploadProgress = Math.round(
              (httpEvent.loaded! / httpEvent.total!) * 100,
            );
          } else if (httpEvent.type === HttpEventType.Response) {
            this.uploadProgress = 100;
            this.isUploading = false;
            this.fileName = 'Fichier envoyé avec succès !';

            const fileInput = document.querySelector(
              'input[type="file"]',
            ) as HTMLInputElement;
            if (fileInput) {
              fileInput.value = '';
            }

            setTimeout(() => {
              this.loadFiles();
              this.uploadProgress = 0;
              this.fileName = '';
            }, 800);
          }
        },
        error: (error: any) => {
          console.error("Erreur lors de l'envoi du fichier", error);
          this.isUploading = false;
          this.uploadProgress = 0;
          this.uploadError = "Erreur lors de l'envoi du fichier";
        },
      });
  }

  cancelUpload(): void {
    this.uploadProgress = 0;
    this.isUploading = false;
    this.fileName = '';
  }

  calculateAge(birthDate: Date | string | undefined): number {
    if (!birthDate) return 0;
    const date = new Date(birthDate);
    const today = new Date();
    let age = today.getFullYear() - date.getFullYear();
    const monthDiff = today.getMonth() - date.getMonth();
    if (
      monthDiff < 0 ||
      (monthDiff === 0 && today.getDate() < date.getDate())
    ) {
      age--;
    }
    return age;
  }
}
