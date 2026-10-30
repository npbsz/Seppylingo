import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
// Router Import entfernt (Chat Clean-Up)

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  user: any;
  currentUserUsername: string | null = '';
  isOwnProfile: boolean = true;

  // NEU: Liste für offene Freundschaftsanfragen
  friendRequests: any[] = [];

  // Einstellungen
  selectedPreference: string = '';
  selectedFavGame: string = '';

  // Suche
  searchTerm: string = '';
  searchResults: any[] = [];
  showSearchResults: boolean = false;

  // Avatar
  showAvatarModal: boolean = false;
  customAvatarUrl: string = '';
  availableAvatars: string[] = [
    '/assets/logo.png',
    'https://api.dicebear.com/7.x/pixel-art/svg?seed=Mario',
    'https://api.dicebear.com/7.x/pixel-art/svg?seed=Peach',
    'https://api.dicebear.com/7.x/pixel-art/svg?seed=Bowser',
    'https://api.dicebear.com/7.x/pixel-art/svg?seed=Link',
    'https://api.dicebear.com/7.x/pixel-art/svg?seed=Zelda',
    'https://api.dicebear.com/7.x/pixel-art/svg?seed=MegaMan',
  ];

  constructor(
    private userService: UserService,
    private authService: AuthService
    // Router Injection entfernt
  ) {}

  ngOnInit() {
    this.currentUserUsername = this.authService.getUsername();
    this.loadMyProfile();
  }

  loadMyProfile() {
    this.userService.getProfile().subscribe({
      next: (data) => {
        this.user = data;
        this.isOwnProfile = true;
        this.syncLocalData();
        // Wenn es mein Profil ist, lade auch Anfragen
        this.loadFriendRequests();
      },
      error: (err) => console.error('Fehler beim Laden des Profils:', err)
    });
  }

  // --- NEU: ANFRAGEN MANAGEMENT ---

  loadFriendRequests() {
    this.userService.getFriendRequests().subscribe({
      next: (reqs) => this.friendRequests = reqs,
      error: (err) => console.error('Fehler beim Laden der Anfragen', err)
    });
  }

  acceptRequest(id: number) {
    this.userService.acceptFriendRequest(id).subscribe({
      next: () => {
        alert('Freundschaft angenommen! 🎉');
        this.loadMyProfile(); // Profil & Liste aktualisieren
      },
      error: () => alert('Fehler beim Annehmen.')
    });
  }

  declineRequest(id: number) {
    this.userService.declineFriendRequest(id).subscribe({
      next: () => this.loadFriendRequests(), // Liste aktualisieren
      error: () => alert('Fehler beim Ablehnen.')
    });
  }
  // --------------------------------

  viewFriendProfile(username: string) {
    if (username === this.currentUserUsername) {
      this.loadMyProfile();
      return;
    }

    this.userService.getUserProfile(username).subscribe({
      next: (data) => {
        this.user = data;
        this.isOwnProfile = false;
        this.syncLocalData();
        this.searchTerm = '';
        this.showSearchResults = false;
      },
      error: (err) => alert('Profil konnte nicht geladen werden.')
    });
  }

  removeFriend(friendName: string) {
    if(!confirm(`Möchtest du ${friendName} wirklich entfernen?`)) return;

    this.userService.removeFriend(friendName).subscribe({
      next: () => {
        alert('Freund entfernt.');
        this.loadMyProfile();
      },
      error: (err) => alert('Fehler beim Entfernen.')
    });
  }

  // --- SUCHE & ADD ---
//hier kann man einstellen ab wie vielen buchstaben der user angezeigt werden solll
  onSearchInput() {
    if (!this.searchTerm || this.searchTerm.length < 2) {
      this.searchResults = [];
      this.showSearchResults = false;
      return;
    }

    this.userService.searchUsers(this.searchTerm).subscribe({
      next: (results) => {
        this.searchResults = results.filter(u => u.username !== this.currentUserUsername);
        this.showSearchResults = true;
      },
      error: (err) => console.error('Fehler bei der Suche:', err)
    });
  }

  // GEÄNDERT: Sendet jetzt eine Anfrage statt direkt zu adden
  addSearchedUser(username: string) {
    this.userService.sendFriendRequest(username).subscribe({
      next: () => {
        alert(`Anfrage an ${username} gesendet! 📨`);
        // Suchfeld leeren
        this.searchTerm = '';
        this.showSearchResults = false;
      },
      error: (err) => {
        console.error(err);
        // Zeigt Backend-Fehler an (z.B. "Anfrage bereits gesendet")
        const msg = err.error?.message || err.error || 'Fehler beim Senden.';
        alert(msg);
      }
    });
  }

  // --- AVATAR & CONFIG ---

  syncLocalData() {
    this.selectedPreference = this.user.learningPreferences || '';
    this.selectedFavGame = this.user.favoriteGame || '';
  }

  openAvatarModal() {
    if(!this.isOwnProfile) return;
    this.showAvatarModal = true;
  }

  closeAvatarModal() {
    this.showAvatarModal = false;
  }

  selectAvatar(url: string) {
    this.updateProfileData({ profilePictureUrl: url });
    this.showAvatarModal = false;
  }

  saveCustomAvatar() {
    if (this.customAvatarUrl) {
      this.updateProfileData({ profilePictureUrl: this.customAvatarUrl });
      this.showAvatarModal = false;
    }
  }

  savePreferences() {
    this.updateProfileData({
      learningPreferences: this.selectedPreference,
      favoriteGame: this.selectedFavGame
    });
  }

  private updateProfileData(data: any) {
    this.userService.updateProfile(data).subscribe({
      next: (updatedUser) => {
        this.user = updatedUser;
        alert('Profil gespeichert! 💾');
      },
      error: (err) => alert('Fehler beim Speichern.')
    });
  }
}
