import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ImageService } from '../../services/image.service';

@Component({
  selector: 'app-image-quiz',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './image-quiz.component.html',
  styleUrls: ['./image-quiz.component.css']
})
export class ImageQuizComponent implements OnInit {
  // Spiel-Zustand
  score = 0;
  loading = false;
  answered = false;
  isGameOver = false; // Neu: Zeigt an, ob das Spiel vorbei ist

  // Runden-Logik
  currentRound = 0;
  readonly MAX_ROUNDS = 10;

  // Daten
  currentImageUrl: string | null = null;
  correctWord: string = '';
  selectedOption: string = '';
  options: string[] = [];

  // Wörter für das aktuelle Spiel (werden beim Start gemischt)
  private gameWords: string[] = [];

  // Gesamtliste aller möglichen Wörter
  private fullWordList = [
    'Cat', 'Dog', 'Apple', 'Bread', 'Water', 'Milk', 'Car', 'Computer', 'Sun', 'Tree', 'Book',
    'Pizza', 'Fish', 'Bird', 'House', 'Moon', 'Flower', 'Chair',
    'Robot', 'Star', 'Sword', 'Shield', 'Potion', 'Mouse', 'Keyboard', 'Phone', 'Clock'
  ];

  constructor(
    private imageService: ImageService,
    private router: Router
  ) {}

  ngOnInit() {
    this.startGame();
  }

  startGame() {
    this.score = 0;
    this.currentRound = 0;
    this.isGameOver = false;

    // Die gesamte Liste mischen und die ersten 10 Wörter nehmen
    // Das garantiert, dass kein Wort in einer Session doppelt vorkommt!
    const shuffled = this.shuffleArray([...this.fullWordList]);
    this.gameWords = shuffled.slice(0, this.MAX_ROUNDS);

    this.nextRound();
  }

  nextRound() {
    // Check: Sind wir fertig?
    if (this.currentRound >= this.gameWords.length) {
      this.finishGame();
      return;
    }

    this.answered = false;
    this.selectedOption = '';
    this.loading = true;
    this.currentImageUrl = null;

    // Das nächste Wort aus unserer vorbereiteten Liste nehmen
    this.correctWord = this.gameWords[this.currentRound];

    // Optionen generieren
    this.generateOptions();

    // Bild laden
    this.imageService.getImageForWord(this.correctWord).subscribe({
      next: (url) => {
        this.currentImageUrl = url;
        this.loading = false;
        if (!url) console.warn(`Kein Bild gefunden für: ${this.correctWord}`);
      },
      error: (err) => {
        console.error('Fehler beim Laden des Bildes:', err);
        this.loading = false;
      }
    });
  }

  generateOptions() {
    // Distraktoren suchen (Wörter, die NICHT das aktuelle Lösungswort sind)
    const distractors = this.fullWordList
      .filter(w => w !== this.correctWord)
      .sort(() => 0.5 - Math.random()) // Zufällig mischen
      .slice(0, 3); // 3 Stück nehmen

    // Richtige Antwort hinzufügen und alles mischen
    this.options = this.shuffleArray([...distractors, this.correctWord]);
  }

  checkAnswer(selected: string) {
    if (this.answered) return;

    this.answered = true;
    this.selectedOption = selected;

    if (selected === this.correctWord) {
      this.score += 10;
    }
// setTimeout, damit Angular Zeit hat, den Button erst zu rendern (ngIf)
    setTimeout(() => {
      const container = document.querySelector('.game-container');
      if (container) {
        // Scrollt den Container nach unten
        container.scrollIntoView({ behavior: 'smooth', block: 'end' });
      }
    }, 100);

  }

  // Wird aufgerufen, wenn der Nutzer auf "Nächstes Bild" klickt
  goToNextQuestion() {
    this.currentRound++; // Erst hier den Zähler hochsetzen
    this.nextRound();
  }

  finishGame() {
    this.isGameOver = true;

  }

  goBack() {
    this.router.navigate(['/']);
  }

  // Hilfsfunktion zum Mischen von Arrays
  private shuffleArray(array: any[]): any[] {
    return array.sort(() => 0.5 - Math.random());
  }
}
