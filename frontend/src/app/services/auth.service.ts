import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';  
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  
  private readonly AUTH_API = `${environment.apiUrl}/auth`;
  
  private isLoggedIn$ = new BehaviorSubject<boolean>(this.hasToken());
  private currentUser$ = new BehaviorSubject<string | null>(this.getUsername());

  constructor(
    private http: HttpClient,
    private router: Router  
  ) {}

  register(data: any): Observable<string> {
    return this.http.post(`${this.AUTH_API}/register`, data, { responseType: 'text' });
  }

  login(data: any): Observable<any> {
    return this.http.post<any>(`${this.AUTH_API}/login`, data).pipe(
      tap(response => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('username', response.username);
        this.isLoggedIn$.next(true);
        this.currentUser$.next(response.username);

        // Prüfungs-Modus merken:
        // introExamRequired=true => Einführungsprüfung
        // sonst => "normal" (Abschlussprüfung / normale Prüfung)
        if (response.introExamRequired) {
          sessionStorage.setItem('examMode', 'intro');
        } else {
          // nur setzen, wenn noch nichts gesetzt ist (Dashboard kann später "final" setzen)
          if (!sessionStorage.getItem('examMode')) {
            sessionStorage.setItem('examMode', 'final');
          }
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    sessionStorage.removeItem('examMode');

    this.isLoggedIn$.next(false);
    this.currentUser$.next(null);
    this.router.navigate(['/login']);  
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  isAuthenticated(): Observable<boolean> {
    return this.isLoggedIn$.asObservable();
  }

  getCurrentUser(): Observable<string | null> {
    return this.currentUser$.asObservable();
  }

  private hasToken(): boolean {
    return !!localStorage.getItem('token');
  }
}