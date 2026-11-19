import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
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
