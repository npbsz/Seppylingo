import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // WICHTIG: Hier muss 8080 stehen!
  private apiUrl = 'http://localhost:8080/api/user';

  constructor(private http: HttpClient) {}

  private getHeaders() {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      })
    };
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/me`, this.getHeaders());
  }

  getUserProfile(username: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${username}`, this.getHeaders());
  }

  searchUsers(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/search?query=${query}`, this.getHeaders());
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/me`, data, this.getHeaders());
  }

  addPoints(points: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/score/add`, { points }, this.getHeaders());
  }

  // Anfrage senden
  sendFriendRequest(receiverUsername: string): Observable<any> {
    const myName = this.getProfileUsername();
    return this.http.post(`${this.apiUrl}/${myName}/friend-request/${receiverUsername}`, {}, this.getHeaders());
  }

  // Meine offenen Anfragen holen
  getFriendRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/friend-requests`, this.getHeaders());
  }

  // Anfrage annehmen
  acceptFriendRequest(requestId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/friend-requests/${requestId}/accept`, {}, this.getHeaders());
  }

  // Anfrage ablehnen
  declineFriendRequest(requestId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/friend-requests/${requestId}`, this.getHeaders());
  }

  removeFriend(username: string): Observable<any> {
    const myName = this.getProfileUsername();
    return this.http.delete(`${this.apiUrl}/${myName}/friends/${username}`, this.getHeaders());
  }

  // Hilfsmethode um eigenen Namen zu holen (speichern wir meist im localStorage)
  private getProfileUsername(): string | null {
      return localStorage.getItem('username');
  }
}
