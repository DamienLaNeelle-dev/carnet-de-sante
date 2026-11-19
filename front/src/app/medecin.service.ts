import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class MedecinService {
  constructor(private httpClient: HttpClient) {}

  getMedecins() {
    return this.httpClient.get<any[]>('/api/medecins');
  }
}
