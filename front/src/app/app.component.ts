import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  title = 'front';
  isLoggedIn = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
    this.authService
      .isLoggedIn()
      .subscribe((val: boolean) => (this.isLoggedIn = val));
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
