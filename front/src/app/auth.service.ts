import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, tap } from 'rxjs';

interface LoginResponse {
  token: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private isLoggedIn$ = new BehaviorSubject<boolean>(this.hasToken());

  constructor(private httpClient: HttpClient) {}

  private hasToken(): boolean {
    return !!localStorage.getItem('token');
  }

  login(email: string, password: string) {
    return this.httpClient
      .post<LoginResponse>('/api/auth/login', { email, password })
      .pipe(
        tap((data) => {
          localStorage.setItem('token', data.token);
          localStorage.setItem('role', data.role);
          this.isLoggedIn$.next(true);
        }),
      );
  }

  logout() {
    localStorage.clear();
    this.isLoggedIn$.next(false);
  }

  isLoggedIn() {
    return this.isLoggedIn$.asObservable();
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
