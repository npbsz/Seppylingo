import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin() {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.username || !this.password) {
      this.errorMessage = 'Bitte alle Felder ausfüllen!';
      return;
    }

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: (response) => {
        this.successMessage = 'Login erfolgreich! Weiterleitung...';

        // Routing im Component
        if (response.introExamRequired) {
          this.router.navigate(['/exam']);  // falls bei dir /exam/start ist -> hier ändern
        } else {
          this.router.navigate(['/']);
        }
      },
      error: (error) => {
        console.error('Login Error:', error);
        this.errorMessage = 'Login fehlgeschlagen! Benutzername oder Passwort falsch.';
      }
    });
  }
}
