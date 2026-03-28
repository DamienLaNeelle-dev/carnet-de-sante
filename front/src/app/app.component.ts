import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './auth.service';
import { NgIf } from '../../node_modules/@angular/common/common_module.d-NEF7UaHr';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NgIf],
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
    this.authService.isLoggedIn().subscribe((val) => (this.isLoggedIn = val));
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
