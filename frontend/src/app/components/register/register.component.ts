import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  username = '';
  email = '';
  password = '';
  
  // Steuerung für das Modal
  showAgbModal = false;
  // Steuerung für die Warnung im Modal
  showWarning = false;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  // 1. "Registrieren" Button geklickt
  openAgbCheck() {
    this.errorMessage = '';
    
    if (!this.username || !this.email || !this.password) {
      this.errorMessage = 'Bitte erst alle Felder ausfüllen!';
      return;
    }
    if (this.password.length < 6) {
      this.errorMessage = 'Passwort zu kurz (min. 6 Zeichen).';
      return;
    }

    // Modal öffnen, Warnung zurücksetzen
    this.showAgbModal = true;
    this.showWarning = false;
  }

  // 2. Nutzer klickt auf "ABLEHNEN"
  onDecline() {
    // Statt zu schließen, zeigen wir die Warnung
    this.showWarning = true;
  }

  // 3. Nutzer klickt bei der Warnung auf "ZURÜCK" (will doch lesen)
  onBackToAgb() {
    this.showWarning = false;
  }

  // 4. Nutzer klickt bei der Warnung auf "JA, ABBRECHEN" (ganz raus)
  closeModal() {
    this.showAgbModal = false;
    this.showWarning = false;
  }

  // 5. Nutzer akzeptiert -> Ab zum Server
  confirmAndRegister() {
    const registerData = {
      username: this.username,
      email: this.email,
      password: this.password,
      agbAccepted: true 
    };

    this.authService.register(registerData).subscribe({
      next: () => {
        this.showAgbModal = false;
        this.successMessage = 'Registrierung erfolgreich! Weiterleitung...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (error: any) => {
        this.showAgbModal = false;
        console.error(error);
        if (error.status === 409) {
            this.errorMessage = 'Nutzername/Email schon vergeben.';
        } else {
            this.errorMessage = 'Fehler bei der Registrierung.';
        }
      }
    });
  }
}