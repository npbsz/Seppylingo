import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import {
  FlashcardService,
  FlashcardExplainRequest,
  FlashcardExplainResponse
} from '../../services/flashcard.service';

import { FlashcardDto, AnswerResultDto } from '../../model/flashcard';
import { UserService } from '../../services/user.service';

type DirectionMode = 'en-de' | 'de-en' | 'mixed';
type Phase =
  | 'idle'
  | 'training'
  | 'ask-errors'
  | 'errors'
  | 'ask-errors-again'
  | 'finished';

@Component({
  selector: 'app-flashcards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './flashcards.component.html',
  styleUrls: ['./flashcards.component.css']
})
export class FlashcardComponent implements OnInit {
  mode: DirectionMode = 'en-de';
  phase: Phase = 'idle';
  maxCards = 15;

  currentIndex = 0;
  points = 0;
  mistakes = 0;

  errorRoundSize: number | null = null;

  card: FlashcardDto | null = null;
  isBack = false;

  loading = false;
  error: string | null = null;

  // ------------------------------
  // KI State
  // ------------------------------
  aiLoading = false;
  aiError: string | null = null;
  aiResult: FlashcardExplainResponse | null = null;

  // Merkt sich die Richtung der aktuell geladenen Karte (wichtig bei mixed)
  // (nicht private, damit Template/Getter sauber damit arbeiten kann)
  lastDir: 'en-de' | 'de-en' | null = null;

  constructor(
    private flashcards: FlashcardService,
    private userService: UserService
  ) {}

  ngOnInit(): void {}

  // ------------------------------------------------------------
  // UI Labels abhängig von Richtung
  // en-de => Englisch, de-en => Deutsch
  // ------------------------------------------------------------

  get isEnglishUi(): boolean {
    return this.lastDir === 'en-de';
  }

  get labelExplanation(): string {
    return this.isEnglishUi ? 'Explanation' : 'Erklärung';
  }

  get labelExamples(): string {
    return this.isEnglishUi ? 'Examples' : 'Beispiele';
  }

  get labelTips(): string {
    return this.isEnglishUi ? 'Tips' : 'Tipps';
  }

  // ------------------------------------------------------------
  // MODE & LEKTION
  // ------------------------------------------------------------

  setMode(mode: DirectionMode): void {
    if (this.loading) return;
    if (this.phase === 'idle' || this.phase === 'finished') {
      this.mode = mode;
    }
  }

  startLesson(): void {
    this.points = 0;
    this.mistakes = 0;
    this.currentIndex = 0;
    this.phase = 'training';
    this.card = null;
    this.isBack = false;
    this.error = null;
    this.errorRoundSize = null;

    this.clearAiBox();
    this.lastDir = null;

    this.loading = true;

    // Fehler im Backend löschen
    this.flashcards.reset().subscribe({
      next: () => this.loadNextCard(),
      error: () => this.loadNextCard()
    });
  }

  private pickDir(): 'en-de' | 'de-en' {
    if (this.mode === 'mixed') {
      return Math.random() < 0.5 ? 'en-de' : 'de-en';
    }
    return this.mode;
  }

  // ------------------------------------------------------------
  // KARTE LADEN
  // ------------------------------------------------------------

  private loadNextCard(): void {
    this.error = null;
    this.loading = true;
    this.isBack = false;

    // Bei neuer Karte: alte KI-Erklärung weg
    this.clearAiBox();

    const dir = this.pickDir();
    this.lastDir = dir;

    const source$ =
      this.phase === 'errors'
        ? this.flashcards.nextError(dir)
        : this.flashcards.next(dir);

    source$.subscribe({
      next: (card: FlashcardDto) => {
        this.card = card;
        this.loading = false;
      },
      error: () => {
        this.card = null;
        this.loading = false;
        this.error = 'Karte konnte nicht geladen werden.';
      }
    });
  }

  // ------------------------------------------------------------
  // KARTE DREHEN
  // ------------------------------------------------------------

  toggleCard(): void {
    if (!this.card || this.loading) return;
    this.isBack = !this.isBack;
  }

  // ------------------------------------------------------------
  // KI: KARTE ERKLÄREN
  // ------------------------------------------------------------

  explainCard(): void {
    if (!this.card || this.loading || this.aiLoading) return;

    this.aiLoading = true;
    this.aiError = null;
    this.aiResult = null;

    const payload: FlashcardExplainRequest = {
      direction: this.lastDir ?? 'en-de',
      front: this.card.front,
      back: this.card.back
      // Optional: userScore hinzufügen, wenn du ihn hier verfügbar machst
      // userScore: ...
    };

    this.flashcards.explain(payload).subscribe({
      next: (res: FlashcardExplainResponse) => {
        this.aiLoading = false;
        this.aiResult = res;
      },
      error: () => {
        this.aiLoading = false;
        this.aiError = 'KI-Erklärung konnte nicht geladen werden.';
      }
    });
  }

  private clearAiBox(): void {
    this.aiLoading = false;
    this.aiError = null;
    this.aiResult = null;
  }

  // ------------------------------------------------------------
  // ANTWORT-LOGIK
  // ------------------------------------------------------------

  canAnswer(): boolean {
    return (
      !!this.card &&
      !this.loading &&
      this.phase !== 'idle' &&
      this.phase !== 'finished' &&
      this.phase !== 'ask-errors' &&
      this.phase !== 'ask-errors-again'
    );
  }

  onAnswer(isRight: boolean): void {
    if (!this.canAnswer() || !this.card) return;

    this.loading = true;
    const cardId = this.card.id;

    const call$ =
      this.phase === 'errors'
        ? this.flashcards.answerError(cardId, isRight)
        : this.flashcards.answer(cardId, isRight);

    call$.subscribe({
      next: (res: AnswerResultDto) => {
        this.loading = false;

        this.points = res.points;
        this.mistakes = res.mistakes ?? this.mistakes;

        // --- PUNKTE AUF GLOBALEN ACCOUNT SPEICHERN ---
        if (isRight) {
          this.userService.addPoints(5).subscribe({
            next: () => console.log('5 Punkte gespeichert'),
            error: (e) => console.error('Punktefehler', e)
          });
        }
        // --------------------------------------------------

        // ======================================================
        // TRAININGSRUNDE
        // ======================================================
        if (this.phase === 'training') {
          this.currentIndex++;

          if (this.currentIndex >= this.maxCards) {
            if (this.mistakes > 0) {
              this.phase = 'ask-errors';
              this.card = null;
              this.isBack = false;
              this.clearAiBox();
            } else {
              this.finishLesson();
            }
            return;
          }

          this.loadNextCard();
          return;
        }

        // ======================================================
        // FEHLERRUNDE
        // ======================================================
        if (this.phase === 'errors') {
          if (this.mistakes === 0) {
            this.finishLesson();
            return;
          }

          this.currentIndex++;

          if (
            this.errorRoundSize !== null &&
            this.currentIndex >= this.errorRoundSize
          ) {
            if (this.mistakes > 0) {
              this.phase = 'ask-errors-again';
              this.card = null;
              this.isBack = false;
              this.clearAiBox();
            } else {
              this.finishLesson();
            }
            return;
          }

          this.loadNextCard();
          return;
        }
      },

      error: () => {
        this.loading = false;
        this.error = 'Antwort konnte nicht gespeichert werden.';
      }
    });
  }

  // ------------------------------------------------------------
  // FEHLERRUNDEN-STEUERUNG
  // ------------------------------------------------------------

  startErrorRound(): void {
    this.phase = 'errors';
    this.currentIndex = 0;
    this.card = null;
    this.isBack = false;
    this.error = null;

    this.clearAiBox();

    this.errorRoundSize = this.mistakes > 0 ? this.mistakes : 0;

    this.loadNextCard();
  }

  skipErrorRound(): void {
    this.finishLesson();
  }

  repeatErrorsAgain(): void {
    this.phase = 'errors';
    this.currentIndex = 0;
    this.card = null;
    this.isBack = false;
    this.error = null;

    this.clearAiBox();

    this.errorRoundSize = this.mistakes > 0 ? this.mistakes : 0;

    this.loadNextCard();
  }

  skipErrorsAgain(): void {
    this.finishLesson();
  }

  // ------------------------------------------------------------
  // LEKTION BEENDEN
  // ------------------------------------------------------------

  private finishLesson(): void {
    this.phase = 'finished';
    this.card = null;
    this.isBack = false;
    this.currentIndex = this.maxCards;
    this.errorRoundSize = null;

    this.clearAiBox();
  }

  restartFromFinished(): void {
    this.phase = 'idle';
    this.points = 0;
    this.mistakes = 0;
    this.currentIndex = 0;
    this.card = null;
    this.isBack = false;
    this.error = null;
    this.errorRoundSize = null;

    this.clearAiBox();
    this.lastDir = null;
  }
}
