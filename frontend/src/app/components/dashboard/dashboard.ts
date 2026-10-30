import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';

// Interface für Spiel / Übung
interface Game {
  id: string;
  name: string;
  description: string;
  icon: string;
  status: 'active' | 'disabled';
}

interface NewVocabulary {
  wordDE: string;
  wordEN: string;
  wordType: string;
  theme: string;
}

interface Vocabulary {
  id: number;
  wordDE: string;
  wordEN: string;
  wordType: string;
  theme: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {

  // ========= User Data =========
  user: any = null; // Speichert das geladene Profil (Punkte, Badges, etc.)

  // ========= Titel-Animation =========
  titleText: string = 'SeppyLingo';
  titleLetters: string[] = this.titleText.split('');
  animateTitle: boolean = false;
  private titleIntervalId: any;

  // ========= Lernübungen (normal) =========
  learningGames: Game[] = [
    {
      id: 'listening-exercise',
      name: 'Hörverstehen',
      description: 'Verbessere dein Hörverstehen mit spannenden Audio-Übungen.',
      icon: '🎧',
      status: 'active',
    },
    {
      id: 'lueckentext',
      name: 'Lückentext',
      description: 'Fülle Lücken im Satz und trainiere Grammatik & Wortschatz.',
      icon: '📝',
      status: 'active',
    },
    {
      id: 'flashcards',
      name: 'Flashcards',
      description: 'Lerne Vokabeln mit interaktiven Karteikarten.',
      icon: '🃏',
      status: 'active',
    },
    {
      id: 'multiple-choice',
      name: 'Multiple Choice',
      description: 'Teste dein Wissen mit Multiple-Choice-Fragen.',
      icon: '❓',
      status: 'active',
    },

    {
      id: 'image-quiz',
      name: 'Bilder-Quiz',
      description: 'Erkenne Wörter anhand von Bildern und erweitere deinen Wortschatz.',
      icon: '🖼️',
      status: 'active',
    },

    // Neutrale Coming-Soon-Karten (keine Hinweise auf Inhalte)
    {
      id: 'exercise-soon-1',
      name: 'Coming Soon',
      description: 'Neue Inhalte werden bald verfügbar.',
      icon: '⭐',
      status: 'disabled',
    },
    {
      id: 'exercise-soon-2',
      name: 'Coming Soon',
      description: 'Weitere Übungen folgen.',
      icon: '⭐',
      status: 'disabled',
    },
    {
      id: 'exercise-soon-3',
      name: 'Coming Soon',
      description: 'Bald stehen dir mehr Optionen zur Verfügung.',
      icon: '⭐',
      status: 'disabled',
    },
    {
      id: 'exercise-soon-4',
      name: 'Coming Soon',
      description: 'Hier entsteht ein neuer Bereich.',
      icon: '⭐',
      status: 'disabled',
    },
    {
      id: 'exercise-soon-5',
      name: 'Coming Soon',
      description: 'Weitere Inhalte sind in Planung.',
      icon: '⭐',
      status: 'disabled',
    },
  ];

  // ========= Arcade-Ecke (Spiele) =========
  arcadeGames: Game[] = [
    {
      id: 'snake',
      name: 'Snake',
      description: 'Spiele Snake und sammle Punkte! Lerne zum Wiederbeleben Vokabeln.',
      icon: '🐍',
      status: 'active',
    },

    {
      id: 'bubble-bobble',
      name: 'Seppy Bobble',
      description: 'Retro Klassiker Bubblebobble:\nJetzt mit Lernen statt GAME OVER.',
      icon: '🦖',
      status: 'active',
    },
    // Neutrale Coming-Soon-Karten (Arcade)
    {
      id: 'arcade-soon-2',
      name: 'Coming Soon',
      description: 'Mehr Spiele werden hinzugefügt.',
      icon: '🔒',
      status: 'disabled',
    },
    {
      id: 'arcade-soon-3',
      name: 'Coming Soon',
      description: 'Weitere Arcade-Features sind geplant.',
      icon: '🔒',
      status: 'disabled',
    },
    {
      id: 'arcade-soon-4',
      name: 'Coming Soon',
      description: 'Hier entsteht ein neues Spiel.',
      icon: '🔒',
      status: 'disabled',
    },
    {
      id: 'arcade-soon-5',
      name: 'Coming Soon',
      description: 'Zukünftige Erweiterungen werden hier erscheinen.',
      icon: '🔒',
      status: 'disabled',
    },
  ];

  // ========= Formular für neue Vokabeln =========
  vocabForm: FormGroup;
  addVocabMessage: string = '';

  // ========= Motivations-Sprüche =========
  motivationalQuotes: string[] = [
    'Jede Vokabel bringt dich einen Schritt näher zu flüssigem Sprechen.',
    'Emirhan ist der beste aus der Gruppe T05!!!!!!',
    'Fehler sind Beweise dafür, dass du es versuchst – weiter so!',
    'Sprache lernt man nicht auswendig, sondern durch Wiederholung.',
    'Auch 10 Minuten heute sind besser als 0 Minuten morgen.',
    'Wenn du es laut aussprichst, merkt sich dein Gehirn es doppelt.',
    'Du musst nicht perfekt sein, nur ein bisschen besser als gestern.',
    'Jede Übung hier ist ein Invest in dein zukünftiges Ich.',
  ];

  currentQuote: string = '';
  displayedQuote: string = '';
  animateQuote: boolean = true;
  quotePulse: boolean = false;
  private quoteIntervalId: any;
  private typingIntervalId: any;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    public authService: AuthService,
    private userService: UserService // Inject User Service
  ) {
    this.vocabForm = this.fb.group({
      wordDE: ['', Validators.required],
      wordEN: ['', Validators.required],
      wordType: ['Nomen', Validators.required],
      theme: ['Basics', Validators.required],
    });

    this.currentQuote = this.getRandomQuote();
  }

  ngOnInit(): void {
    this.startQuoteRotation();
    this.startTitleAnimation();
    this.loadUserProfile(); // Profil laden
  }

  ngOnDestroy(): void {
    if (this.quoteIntervalId) {
      clearInterval(this.quoteIntervalId);
    }
    if (this.typingIntervalId) {
      clearInterval(this.typingIntervalId);
    }
    if (this.titleIntervalId) {
      clearInterval(this.titleIntervalId);
    }
  }

  // ================== User Profil ==================
  loadUserProfile() {
    this.userService.getProfile().subscribe({
      next: (data) => {
        this.user = data;
        console.log('User Profil geladen:', this.user);
      },
      error: (err) => {
        console.error('Konnte Profil nicht laden:', err);
      }
    });
  }

  // ================== Motivationslogik ==================

  private startQuoteRotation(): void {
    // direkt beim Start tippen
    this.startTypingQuote();

    // alle 10 Sekunden neuer Spruch
    this.quoteIntervalId = setInterval(() => {
      this.currentQuote = this.getRandomQuote();
      this.startTypingQuote();
    }, 10000);
  }

  private getRandomQuote(): string {
    const index = Math.floor(Math.random() * this.motivationalQuotes.length);
    return this.motivationalQuotes[index];
  }

  private startTypingQuote(): void {
    if (this.typingIntervalId) {
      clearInterval(this.typingIntervalId);
    }

    const text = this.currentQuote;
    this.displayedQuote = '';
    this.quotePulse = false;
    this.restartQuoteAnimation();

    let i = 0;
    const typingSpeed = 25; // etwas schneller

    this.typingIntervalId = setInterval(() => {
      if (i < text.length) {
        this.displayedQuote += text.charAt(i);
        i++;
      } else {
        clearInterval(this.typingIntervalId);
        this.typingIntervalId = null;
        this.quotePulse = true;
      }
    }, typingSpeed);
  }

  private restartQuoteAnimation(): void {
    this.animateQuote = false;
    setTimeout(() => {
      this.animateQuote = true;
    }, 0);
  }

  // ================== Titel-Animation ==================

  private startTitleAnimation(): void {
    this.triggerTitleAnimation();

    this.titleIntervalId = setInterval(() => {
      this.triggerTitleAnimation();
    }, 3000); // alle 3 Sekunden
  }

  private triggerTitleAnimation(): void {
    this.animateTitle = false;
    setTimeout(() => {
      this.animateTitle = true;
    }, 0);
  }

  // ================ Spiele-/Übungsauswahl ==================

  onGameSelect(game: Game): void {
    if (game.status === 'disabled') {
      console.log('Feature noch nicht verfügbar');
      return;
    }
    this.router.navigate(['/games', game.id]);
    console.log(`Ausgewählt: ${game.name}`);
    console.log(`Navigiere zu /games/${game.id}`);
  }

  // ================ Prüfungsmodus ==================

  onStartExam(): void {
    console.log('Starte Prüfungsmodus...');

    // ✅ Dashboard-Start = Abschlussprüfung (final)
    sessionStorage.setItem('examMode', 'final');

    this.router.navigate(['/exam']);
  }

  // ================ Vokabel hinzufügen ==================

  onAddVocabulary(): void {
    if (this.vocabForm.invalid) {
      this.addVocabMessage = 'Bitte alle Felder ausfüllen.';
      return;
    }

    const newVocab: NewVocabulary = this.vocabForm.value;
    const apiUrl = 'http://localhost:8080/api/exercises';

    this.http.post<Vocabulary>(apiUrl, newVocab).subscribe({
      next: (response) => {
        console.log('Vokabel gespeichert:', response);
        this.addVocabMessage = `Vokabel "${response.wordDE}" erfolgreich hinzugefügt.`;
        this.vocabForm.reset();
      },
      error: (err) => {
        console.error('Fehler beim Speichern:', err);
        this.addVocabMessage = 'Fehler beim Hinzufügen der Vokabel. Bitte erneut versuchen.';
      }
    });
  }

  // ================ Logout ==================

  logout(): void {
    this.authService.logout();
  }

  getCurrentUsername(): string | null {
    // Fallback falls User-Objekt noch nicht geladen ist
    return this.user ? this.user.username : this.authService.getUsername();
  }

}
