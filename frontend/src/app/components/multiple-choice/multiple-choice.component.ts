import { Component,Output,EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MultipleChoiceService,
  MultipleChoiceQuestion,
  AnswerCheckRequest,
} from './multiple-choice.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-multiple-choice',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './multiple-choice.component.html',
  styleUrls: ['./multiple-choice.component.css'],
})
export class MultipleChoiceComponent {
  themes: string[] = [];
  currentTheme: string | null = null;
  @Output() xpEarned=new EventEmitter<number>();
  // UI-Status
  gameStarted = false;
  question: MultipleChoiceQuestion | null = null;

  // Visuelles Feedback für die Buttons
  selectedOption: string | null = null;      // Welchen Button hat der User geklickt?
  isAnswerCorrect: boolean | null = null;    // War es richtig oder falsch?
  isProcessing = false;                      // Sperre, während wir warten

  // Lern-Logik
  askedIds = new Set<number>();
  wrongIds: number[] = [];
  totalWordsInTheme: number | null = null;
  allDone = false;

  // Review-Modus
  isReviewMode = false;
  reviewQueue: number[] = [];

  constructor(
    private mcService: MultipleChoiceService,
    private userService: UserService
  ) {
    this.loadThemes();
  }

  private loadThemes(): void {
    this.mcService.getThemes().subscribe((themes) => {
      this.themes = themes;
    });
  }

  // Menü/Navigation

  selectTheme(theme: string): void {
    this.currentTheme = theme;
    this.gameStarted = false;
    this.resetStats();
  }

  backToMenu(): void {
    this.currentTheme = null;
    this.gameStarted = false;
    this.resetStats();
  }

  startGame(): void {
    if (this.currentTheme) {
      this.gameStarted = true;
      this.loadNextQuestion();
    }
  }

  private resetStats() {
    this.question = null;
    this.askedIds.clear();
    this.wrongIds = [];
    this.totalWordsInTheme = null;
    this.allDone = false;
    this.isReviewMode = false;
    this.reviewQueue = [];
    this.resetButtonState();
  }

  private resetButtonState() {
    this.selectedOption = null;
    this.isAnswerCorrect = null;
    this.isProcessing = false;
  }

  //SPIEL-LOGIK

  loadNextQuestion(): void {
    this.resetButtonState(); // Farben zurücksetzen

    if (!this.currentTheme) return;

    // A) Review-Modus
    if (this.isReviewMode) {
      if (this.reviewQueue.length === 0) {
        this.allDone = true;
        this.question = null;
        return;
      }
      const nextId = this.reviewQueue.pop();
      if (nextId) {
        this.mcService.getQuestionById(nextId).subscribe(q => {
          this.question = q;
        });
      }
      return;
    }

    // B) Normaler Modus
    if (this.totalWordsInTheme !== null && this.askedIds.size >= this.totalWordsInTheme) {
      this.allDone = true;
      this.question = null;
      return;
    }

    this.mcService.getQuestion(this.currentTheme).subscribe((q) => {
      if (this.totalWordsInTheme === null) this.totalWordsInTheme = q.totalWordsInTheme;

      if (this.askedIds.has(q.wordId)) {
        this.loadNextQuestion();
        return;
      }

      this.question = q;
      this.askedIds.add(q.wordId);
    });
  }

  onAnswer(option: string): void {
    // 1. Verhindern, dass man klickt, wenn schon eine Antwort verarbeitet wird
    if (this.isProcessing || !this.question) return;

    this.isProcessing = true; // Klicks sperren
    this.selectedOption = option; // Merken, was geklickt wurde

    const currentId = this.question.wordId;
    const body: AnswerCheckRequest = { wordId: currentId, selectedAnswer: option };

    this.mcService.checkAnswer(body).subscribe((isCorrect) => {

      // 2. Ergebnis setzen (das löst die Farbe im HTML aus)
      this.isAnswerCorrect = isCorrect;

      // Fehler-Logik (wie vorher)
      if (isCorrect) {
        this.xpEarned.emit(5);
        this.userService.addPoints(10).subscribe({
          next: () => console.log('Punkte gutgeschrieben!'),
          error: (err) => console.error(err)
        });
      }
        else if (!this.wrongIds.includes(currentId)) {
            this.wrongIds.push(currentId);
        }


      // 3. Timer starten: Nach 0.4 Sekunden zur nächsten Frage
      setTimeout(() => {
        this.loadNextQuestion();
      }, 400);
    });
  }

  //Review Starten

  startReview(): void {
    this.isReviewMode = true;
    this.allDone = false;
    this.reviewQueue = [...this.wrongIds];
    this.wrongIds = [];
    this.loadNextQuestion();
  }

  restartTheme(): void {
    if (this.currentTheme) {
      this.selectTheme(this.currentTheme);
    }
  }
}
