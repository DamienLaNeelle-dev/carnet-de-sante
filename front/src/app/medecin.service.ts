import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MedecinService {
  private hasMedecin$ = new BehaviorSubject<boolean>(this.hasMedecin());

  constructor(private httpClient: HttpClient) {}

  private hasMedecin(): boolean {
    return !!localStorage.getItem('medecin');
  }

  getMedecins() {
    return this.httpClient.get<any>('/api/medecins').pipe(
      tap((data: any) => {
        localStorage.setItem('nom', data.nom);
        localStorage.setItem('prenom', data.prenom);
        localStorage.setItem('specialite', data.specialite);
        localStorage.setItem('numeroRPPS', data.numeroRpps);
        this.hasMedecin$.next(true);
      }),
    );
  }
}
