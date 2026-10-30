import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  AfterViewInit,
  ViewChild, EventEmitter, Output
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service'; // Pfad anpassen falls nötig

/**
 * Mögliche Bewegungsrichtungen der Schlange.
 */
type Direction = 'up' | 'down' | 'left' | 'right';

/**
 * Ein Feld im Grid (x = Spalte, y = Zeile).
 */
interface Position {
  x: number;
  y: number;
}

/**
 * Vokabel-Objekt, so wie es vom Backend geliefert wird.
 */
interface Vocabulary {
  id: number;
  wordDE: string;
  wordEN: string;
  wordType: string;
  theme: string;
}

@Component({
  selector: 'app-snake-game',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './snake-game.component.html',
  styleUrl: './snake-game.component.css'
})
export class SnakeGameComponent implements AfterViewInit, OnDestroy {
  /* ------------------------------------------------------------------------
   * Spielfeld / Snake-Grunddaten
   * --------------------------------------------------------------------- */
  @Output() xpEarned=new EventEmitter<number>();
  /** Anzahl Felder pro Zeile/Spalte im Grid. */
  readonly gridSize = 20;

  /** Aktuelle Schlange: erstes Element = Kopf. */
  snake: Position[] = [{ x: 10, y: 10 }];

  /**
   * Letzter Zustand der Schlange vor dem aktuellen Move.
   * Wird genutzt, um bei Tod einen Schritt "zurückzuspringen"
   * und die Schlange sichtbar zu lassen.
   */
  private previousSnakeState: Position[] = this.cloneSnake();

  /** Aktuelle Food-Position. */
  food: Position = this.generateFood();

  /** Höchster erreichter Score in dieser Session. */
  highScore = 0;

  /** Aktuelle Bewegungsrichtung. */
  direction: Direction = 'right';

  /** Referenz auf das Game-Loop-Interval. */
  private gameInterval: ReturnType<typeof setInterval> | null = null;

  /** Delay zwischen Moves in ms (steigt mit wachsendem Score). */
  gameSpeedDelay = 200;

  /** Ob das Spiel aktuell läuft. */
  gameStarted = false;

  /** Aktueller Score (abhängig von der Schlangenlänge). */
  score = 0;

  /* ------------------------------------------------------------------------
   * Revive / Game-Over-State
   * --------------------------------------------------------------------- */

  /** Zeigt den Dialog: "Revive oder Neu starten?" */
  isReviveChoicePending = false;

  /** True, solange das Vokabel-Quiz aktiv ist. */
  isRevivePending = false;

  /**
   * Nach erfolgreichem Revive: Spiel pausiert,
   * wartet auf Leertaste zum Fortsetzen.
   */
  revivePauseActive = false;

  /* ------------------------------------------------------------------------
   * Revive-Vokabeln / Quiz-State
   * --------------------------------------------------------------------- */

  /** Die drei zufällig geladenen Revive-Vokabeln. */
  reviveWords: Vocabulary[] = [];

  /** Index der aktuell abgefragten Vokabel (0–2). */
  currentReviveWordIndex = 0;

  /** Anzahl bisher korrekt beantworteter Vokabeln. */
  reviveCorrectCount = 0;

  /** Eingabe des Spielers im Revive-Quiz. */
  userReviveAnswer = '';

  /* ------------------------------------------------------------------------
   * UI-Flags
   * --------------------------------------------------------------------- */

  /** Kurzzeitige Anzeige "REVIVE!" nach erfolgreichem Revive. */
  showReviveFlash = false;

  /** Hinweis "Klicke Leertaste zum Weiterspielen!" nach Revive. */
  showResumeHint = false;

  /** Popup "Neuer Highscore!" nach Game-Over mit Highscore. */
  showHighscoreMessage = false;

  /* ------------------------------------------------------------------------
   * Backend-URL für Vokabeln
   * --------------------------------------------------------------------- */

  /** Endpoint, der eine zufällige Vokabel zurückliefert. */
  private readonly apiUrl = 'http://localhost:8080/api/exercises/random';

  /* ------------------------------------------------------------------------
   * DOM-Referenzen
   * --------------------------------------------------------------------- */

  /** Referenz auf das Spielfeld-Element (#gameBoard). */
  @ViewChild('gameBoard', { static: false })
  gameBoard?: ElementRef<HTMLDivElement>;

  /* ------------------------------------------------------------------------
   * Audio
   * --------------------------------------------------------------------- */

  /** Stellt sicher, dass die Audio-Objekte nur einmal erzeugt werden. */
  private audioInitialized = false;

  /** Ob bereits ein Audio über User-Interaktion "freigeschaltet" wurde. */
  private audioUnlocked = false;

  /** Sound-Effekte. */
  private audioEat?: HTMLAudioElement;
  private audioDie?: HTMLAudioElement;
  private audioStart?: HTMLAudioElement;
  private audioRevive?: HTMLAudioElement;
  private audioHighscore?: HTMLAudioElement;

  /** Hintergrundmusik (läuft in Schleife). */
  private backgroundMusic?: HTMLAudioElement;

  /* ------------------------------------------------------------------------
   * Countdown-State
   * --------------------------------------------------------------------- */

  /** Ob der Start-Countdown aktuell angezeigt wird. */
  isCountdownActive = false;

  /** Text, der im Countdown-Overlay angezeigt wird ("3", "2", "1", "LOS GEHT'S"). */
  countdownText = '';

  /** Interner Zähler für den Countdown (3 → 2 → 1 → "LOS GEHT'S"). */
  private countdownValue = 3;

  /** Referenz auf das Countdown-Interval. */
  private countdownInterval: ReturnType<typeof setInterval> | null = null;

  constructor(
    private readonly http: HttpClient,
    private userService: UserService // UserService injizieren
  ) {}

  /* ------------------------------------------------------------------------
   * Lifecycle Hooks
   * --------------------------------------------------------------------- */

  /**
   * Nach dem Initialisieren der View:
   * - Audio-Struktur vorbereiten
   * - Erstes Rendering der Schlange/Food
   */
  ngAfterViewInit(): void {
    this.initAudio();
    this.draw();
  }

  /**
   * Beim Zerstören der Komponente:
   * - Game-Loop stoppen
   * - Musik stoppen
   * - Countdown aufräumen
   */
  ngOnDestroy(): void {
    //emit xp before game is closed
    if (this.score > 0) {
      this.xpEarned.emit(this.score * 10);
    }

    this.stopGame();
    this.stopBackgroundMusic();
    this.clearCountdown();
  }

  /* ------------------------------------------------------------------------
   * Audio-Initialisierung / Steuerung
   * --------------------------------------------------------------------- */

  /**
   * Erzeugt einmalig alle Audio-Objekte und setzt Lautstärken.
   * Wichtig: Pfade müssen exakt zu deinen Dateien passen.
   */
  private initAudio(): void {
    if (this.audioInitialized) return;

    // ACHTUNG: Dateiname muss zu deiner Datei passen (z.B. death.mp3)
    this.audioEat = new Audio('assets/sounds/eat.mp3');
    this.audioDie = new Audio('assets/sounds/death.mp3');
    this.audioStart = new Audio('assets/sounds/start.mp3');   // Mario Kart Start
    this.audioRevive = new Audio('assets/sounds/revive.mp3');
    this.audioHighscore = new Audio('assets/sounds/highscore.mp3');

    this.backgroundMusic = new Audio('assets/sounds/bg-music.mp3');
    this.backgroundMusic.loop = true;
    this.backgroundMusic.volume = 0.25;

    // SFX etwas lauter einstellen
    this.audioEat.volume = 0.8;
    this.audioDie.volume = 0.7;
    this.audioStart.volume = 0.9;
    this.audioRevive.volume = 0.7;
    this.audioHighscore.volume = 0.8;

    // Vorladen der Audios, damit beim ersten Abspielen weniger Verzögerung ist
    this.audioEat.load();
    this.audioDie.load();
    this.audioStart.load();
    this.audioRevive.load();
    this.audioHighscore.load();
    this.backgroundMusic.load();

    this.audioInitialized = true;
  }

  /**
   * Versucht, Audio-Wiedergabe über eine User-Interaktion freizuschalten
   * (Browser-Restriction bei Autoplay).
   */
  private unlockAudio(): void {
    if (this.audioUnlocked) return;
    this.audioUnlocked = true;

    const testAudio =
      this.backgroundMusic || this.audioStart || this.audioEat;
    if (!testAudio) return;

    try {
      testAudio.muted = true;
      testAudio
        .play()
        .then(() => {
          testAudio.pause();
          testAudio.currentTime = 0;
          testAudio.muted = false;
        })
        .catch(() => {});
    } catch {}
  }

  /**
   * Spielt einen Sound-Effekt einmalig ab,
   * indem das Audio-Objekt geklont wird.
   */
  private playOneShot(audio?: HTMLAudioElement): void {
    if (!audio) return;
    try {
      const clone = audio.cloneNode(true) as HTMLAudioElement;
      clone.volume = audio.volume;
      clone.play().catch(() => {});
    } catch {}
  }

  /**
   * Startet die Hintergrundmusik.
   */
  private startBackgroundMusic(): void {
    if (!this.backgroundMusic) return;
    try {
      this.backgroundMusic.play().catch(() => {});
    } catch {}
  }

  /**
   * Stoppt die Hintergrundmusik und setzt den Zeitpunkt zurück.
   */
  private stopBackgroundMusic(): void {
    if (!this.backgroundMusic) return;
    try {
      this.backgroundMusic.pause();
      this.backgroundMusic.currentTime = 0;
    } catch {}
  }

  /**
   * Spielt zuerst den Start-Sound, danach automatisch die Hintergrundmusik.
   */
  private playStartSoundAndThenBg(): void {
    if (!this.audioStart) {
      this.startBackgroundMusic();
      return;
    }

    try {
      this.audioStart.currentTime = 0;
      this.audioStart.onended = () => {
        this.audioStart!.onended = null;
        this.startBackgroundMusic();
      };
      this.audioStart.play().catch(() => {
        this.audioStart!.onended = null;
        this.startBackgroundMusic();
      });
    } catch {
      this.startBackgroundMusic();
    }
  }

  /**
   * Spielt zuerst den Revive-Sound, danach wieder die Hintergrundmusik.
   */
  private playReviveSoundAndThenBg(): void {
    if (!this.audioRevive) {
      this.startBackgroundMusic();
      return;
    }

    try {
      this.audioRevive.currentTime = 0;
      this.audioRevive.onended = () => {
        this.audioRevive!.onended = null;
        this.startBackgroundMusic();
      };
      this.audioRevive.play().catch(() => {
        this.audioRevive!.onended = null;
        this.startBackgroundMusic();
      });
    } catch {
      this.startBackgroundMusic();
    }
  }

  /**
   * Zentraler Death-Sound-Wrapper.
   * Wird in handleDeath() aufgerufen, damit der Tod-Sound immer sicher
   * beim Game-Over abgespielt wird.
   */
  private playDeathSound(): void {
    this.playOneShot(this.audioDie);
  }

  /* ------------------------------------------------------------------------
   * Countdown-Logik
   * --------------------------------------------------------------------- */

  /**
   * Bricht einen laufenden Countdown ab und setzt den Zustand zurück.
   */
  private clearCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
    this.isCountdownActive = false;
    this.countdownText = '';
  }

  /**
   * Startet den 3-2-1-LOS Countdown, stoppt das Spiel in der Zeit
   * und startet danach den Game-Loop.
   */
  private startCountdown(): void {
    this.clearCountdown();

    this.isCountdownActive = true;
    this.countdownValue = 3;
    this.countdownText = this.countdownValue.toString();

    this.stopGame();
    this.draw();

    // Start-Sound parallel zu "3"
    this.playStartSoundAndThenBg();

    this.countdownInterval = setInterval(() => {
      this.countdownValue--;

      if (this.countdownValue > 0) {
        this.countdownText = this.countdownValue.toString();
      } else {
        if (this.countdownInterval) {
          clearInterval(this.countdownInterval);
          this.countdownInterval = null;
        }

        this.countdownText = "LOS GEHT'S!";

        this.gameStarted = true;
        this.restartGameLoop();
        this.draw();

        setTimeout(() => {
          this.isCountdownActive = false;
          this.countdownText = '';
        }, 800);
      }
    }, 1000);
  }

  /* ------------------------------------------------------------------------
   * Rendering / Spielfeld
   * --------------------------------------------------------------------- */

  /**
   * Klont die aktuelle Schlange (deep copy),
   * damit wir den alten Zustand als Backup haben.
   */
  private cloneSnake(): Position[] {
    return this.snake.map((segment) => ({ ...segment }));
  }

  /**
   * Zeichnet Schlange und Food auf das Spielfeld neu.
   */
  private draw(): void {
    if (!this.gameBoard) return;

    const board = this.gameBoard.nativeElement;
    board.innerHTML = '';
    this.drawSnake(board);
    this.drawFood(board);
  }

  /**
   * Zeichnet alle Segmente der Schlange.
   */
  private drawSnake(board: HTMLElement): void {
    this.snake.forEach((segment) => {
      const snakeElement = this.createGameElement('div', 'snake');
      this.setPosition(snakeElement, segment);
      board.appendChild(snakeElement);
    });
  }

  /**
   * Zeichnet das Food, wenn das Spiel gestartet ist.
   */
  private drawFood(board: HTMLElement): void {
    if (!this.gameStarted) return;

    const foodElement = this.createGameElement('div', 'food');
    this.setPosition(foodElement, this.food);
    board.appendChild(foodElement);
  }

  /**
   * Hilfsfunktion zum Erstellen eines Spielfeld-Elements mit CSS-Klasse.
   */
  private createGameElement(tag: string, className: string): HTMLElement {
    const element = document.createElement(tag);
    element.className = className;
    return element;
  }

  /**
   * Setzt die Position eines Elements im CSS-Grid.
   */
  private setPosition(element: HTMLElement, position: Position): void {
    element.style.gridColumn = position.x.toString();
    element.style.gridRow = position.y.toString();
  }

  /**
   * Generiert eine neue Food-Position, die nicht von der Schlange belegt ist.
   */
  private generateFood(): Position {
    const occupied = new Set(
      this.snake.map((segment) => `${segment.x}-${segment.y}`)
    );

    for (let attempt = 0; attempt < 100; attempt++) {
      const x = Math.floor(Math.random() * this.gridSize) + 1;
      const y = Math.floor(Math.random() * this.gridSize) + 1;
      const key = `${x}-${y}`;

      if (!occupied.has(key)) {
        return { x, y };
      }
    }

    // Fallback: notfalls auf Kopf setzen (praktisch nie relevant)
    return { ...this.snake[0] };
  }

  /* ------------------------------------------------------------------------
   * Game Loop / Bewegung
   * --------------------------------------------------------------------- */

  /**
   * Führt einen einzelnen Spielschritt aus:
   * - Kopf in Bewegungsrichtung verschieben
   * - ggf. Food fressen
   * - Kollisionen prüfen
   * - Score & Geschwindigkeit aktualisieren
   */
  private move(): void {
    // Vorherigen Zustand sichern (für "ein Schritt zurück" bei Tod)
    this.previousSnakeState = this.cloneSnake();

    // Neuen Kopf berechnen
    const head = { ...this.snake[0] };
    switch (this.direction) {
      case 'up':
        head.y--;
        break;
      case 'down':
        head.y++;
        break;
      case 'left':
        head.x--;
        break;
      case 'right':
        head.x++;
        break;
    }

    // Kopf vorne einfügen
    this.snake.unshift(head);

    // Prüfen, ob Food gefressen wurde
    const ateFood = head.x === this.food.x && head.y === this.food.y;
    if (!ateFood) {
      // Kein Food gefressen → Schwanz entfernen
      this.snake.pop();
    }

    // Wände oder eigener Körper getroffen?
    if (this.isOutOfBounds(head) || this.isSelfCollision()) {
      // Einen Schritt zurück, damit die Position des "Todes" sichtbar bleibt
      this.snake = this.previousSnakeState.map((segment) => ({ ...segment }));
      this.draw();

      // Zentraler Death-Handler (inkl. Sound)
      this.handleDeath();
      return;
    }

    // Food gefressen → neues Food + Speed erhöhen + Sound
    if (ateFood) {
      this.food = this.generateFood();
      this.increaseSpeed();
      this.playOneShot(this.audioEat);
      this.restartGameLoop();
    }

    this.updateScore();
    this.draw();
  }

  /**
   * Prüft, ob eine Position außerhalb des Spielfeldes liegt.
   */
  private isOutOfBounds(position: Position): boolean {
    return (
      position.x < 1 ||
      position.x > this.gridSize ||
      position.y < 1 ||
      position.y > this.gridSize
    );
  }

  /**
   * Prüft, ob der Kopf in ein eigenes Segment gelaufen ist.
   */
  private isSelfCollision(): boolean {
    const [head, ...body] = this.snake;
    return body.some(
      (segment) => segment.x === head.x && segment.y === head.y
    );
  }

  /**
   * Startet den Game-Loop mit dem aktuellen Delay neu.
   */
  private restartGameLoop(): void {
    if (!this.gameStarted) return;

    if (this.gameInterval) {
      clearInterval(this.gameInterval);
    }

    this.gameInterval = setInterval(() => {
      this.move();
    }, this.gameSpeedDelay);
  }

  /**
   * Score = Schlangenlänge - 1 (ohne Kopf).
   */
  private updateScore(): void {
    this.score = this.snake.length - 1;
  }

  /**
   * Wenn der aktuelle Score größer ist als der Highscore,
   * wird der Highscore aktualisiert.
   */
  private updateHighScore(): void {
    if (this.score > this.highScore) {
      this.highScore = this.score;
    }
  }

  /**
   * Erhöht die Spielgeschwindigkeit, je länger die Schlange wird.
   */
  private increaseSpeed(): void {
    if (this.gameSpeedDelay > 150) {
      this.gameSpeedDelay -= 5;
    } else if (this.gameSpeedDelay > 100) {
      this.gameSpeedDelay -= 3;
    } else if (this.gameSpeedDelay > 50) {
      this.gameSpeedDelay -= 2;
    } else if (this.gameSpeedDelay > 25) {
      this.gameSpeedDelay -= 1;
    }
  }

  /* ------------------------------------------------------------------------
   * Start / Stop
   * --------------------------------------------------------------------- */

  /**
   * Wird vom Button / Leertaste ausgelöst.
   * Startet das Spiel nur, wenn aktuell kein Revive/Countdown läuft.
   */
  startGame(): void {
    if (
      this.gameStarted ||
      this.isRevivePending ||
      this.isCountdownActive ||
      this.isReviveChoicePending ||
      this.revivePauseActive
    ) {
      return;
    }

    this.unlockAudio();
    this.startCountdown();
  }

  /**
   * Stoppt den Game-Loop, setzt gameStarted aber nicht zurück,
   * damit der Zustand klar steuerbar bleibt.
   */
  private stopGame(): void {
    if (this.gameInterval) {
      clearInterval(this.gameInterval);
      this.gameInterval = null;
    }
    this.gameStarted = false;
  }

  /* ------------------------------------------------------------------------
   * Tod / Revive-Auswahl
   * --------------------------------------------------------------------- */

  /**
   * Wird aufgerufen, sobald das Spiel "Game Over" ist.
   * - Stoppt Spiel & Musik
   * - Spielt Death-Sound
   * - Setzt Revive-States zurück
   * - Öffnet den Revive-/Neustart-Dialog
   */
  private handleDeath(): void {
    // Game-Loop und Musik stoppen
    this.stopGame();
    this.stopBackgroundMusic();
    this.clearCountdown();

    // --- NEU: Punkte speichern ---
    if (this.score > 0) {
      this.userService.addPoints(this.score).subscribe({
        next: (res) => console.log('Points saved!', res),
        error: (err) => console.error('Failed to save points', err)
      });
    }
    // -----------------------------

    // Death-Sound abspielen
    this.playDeathSound();

    // Revive-Zustände zurücksetzen
    this.isRevivePending = false;
    this.revivePauseActive = false;
    this.showReviveFlash = false;
    this.showResumeHint = false;
    this.reviveWords = [];
    this.currentReviveWordIndex = 0;
    this.reviveCorrectCount = 0;
    this.userReviveAnswer = '';

    // Dialog anzeigen: Revive oder Neu starten
    this.isReviveChoicePending = true;
  }

  /**
   * Spieler entscheidet sich im Dialog für Revive.
   * → Starte Vokabel-Quiz.
   */
  chooseRevive(): void {
    this.isReviveChoicePending = false;
    this.startReviveQuiz();
  }

  /**
   * Spieler verzichtet bewusst auf Revive.
   * → Vollständiger Reset und neues Spiel.
   */
  chooseRestart(): void {
    this.isReviveChoicePending = false;
    this.triggerFullReset();
  }

  /**
   * Initialisiert den State für das Revive-Quiz
   * und lädt 3 Vokabeln vom Backend.
   */
  private startReviveQuiz(): void {
    this.isRevivePending = true;
    this.reviveWords = [];
    this.currentReviveWordIndex = 0;
    this.reviveCorrectCount = 0;
    this.userReviveAnswer = '';

    this.fetchReviveWord();
    this.fetchReviveWord();
    this.fetchReviveWord();
  }

  /* ------------------------------------------------------------------------
   * Revive-Vokabeln (Backend-Integration)
   * --------------------------------------------------------------------- */

  /**
   * Holt eine zufällige Vokabel vom Backend.
   * Fehlerfall: vollständiger Reset (damit das Spiel nicht hängen bleibt).
   */
  private fetchReviveWord(): void {
    const url = `${this.apiUrl}?t=${Date.now()}-${Math.random()}`;

    this.http.get<Vocabulary>(url).subscribe({
      next: (vocab) => {
        if (!this.isRevivePending) return;
        this.reviveWords.push(vocab);
      },
      error: (error) => {
        console.error('Fehler beim Abrufen der Vokabel:', error);
        this.triggerFullReset();
      }
    });
  }

  /**
   * Positioniert die Schlange nach einem erfolgreichen Revive
   * sicher in der Mitte des Feldes, damit sie nicht sofort wieder stirbt.
   */
  private repositionSnakeAfterRevive(): void {
    const centerX = Math.ceil(this.gridSize / 2);
    const centerY = Math.ceil(this.gridSize / 2);

    const maxLength = centerX;
    const effectiveLength = Math.min(this.snake.length, maxLength);

    const newSnake: Position[] = [];
    for (let i = 0; i < effectiveLength; i++) {
      newSnake.push({
        x: centerX - i,
        y: centerY
      });
    }

    this.snake = newSnake;
    this.direction = 'right';
  }

  /**
   * Wird aufgerufen, wenn der Spieler Enter drückt oder auf "Bestätigen" klickt.
   * Prüft die Eingabe für die aktuelle Vokabel.
   */
  handleReviveAnswer(): void {
    if (
      !this.isRevivePending ||
      this.reviveWords.length < 3 ||
      this.currentReviveWordIndex >= 3 ||
      !this.userReviveAnswer
    ) {
      return;
    }

    const currentWord = this.reviveWords[this.currentReviveWordIndex];
    const user = this.userReviveAnswer.trim().toLowerCase();
    const correct = currentWord.wordDE.trim().toLowerCase();

    if (user === correct) {
      this.reviveCorrectCount++;
    }

    this.currentReviveWordIndex++;
    this.userReviveAnswer = '';

    // Nach der 3. Frage wird entschieden, ob Revive erfolgreich war
    if (this.currentReviveWordIndex >= 3) {
      if (this.reviveCorrectCount === 3) {
        this.finishReviveSuccess();
      } else {
        this.triggerFullReset();
      }
    }
  }

  /**
   * Wird ausgeführt, wenn alle 3 Vokabeln korrekt beantwortet wurden.
   * - Schlange in Mitte setzen
   * - Food neu generieren
   * - Revive-Pause aktivieren
   * - Hinweis + Revive-Flash anzeigen
   */
  private finishReviveSuccess(): void {
    this.isRevivePending = false;
    this.reviveWords = [];
    this.currentReviveWordIndex = 0;
    this.reviveCorrectCount = 0;
    this.userReviveAnswer = '';

    // Schlange sicher platzieren + neues Essen
    this.repositionSnakeAfterRevive();
    this.food = this.generateFood();

    // Revive-Pause: Spiel wartet auf Leertaste
    this.revivePauseActive = true;
    this.showReviveFlash = true;
    this.showResumeHint = true;
    this.gameStarted = false;
    this.stopGame();
    this.draw();

    this.playReviveSoundAndThenBg();

    setTimeout(() => {
      this.showReviveFlash = false;
    }, 1000);
  }

  /**
   * Vollständiger Reset nach Game Over.
   * - Highscore prüfen
   * - States zurücksetzen
   * - Schlange und Food zurück auf Start
   */
  private triggerFullReset(): void {
    //emit xp when game is reset
    if (this.score > 0) {
      this.xpEarned.emit(this.score * 10);
    }

    this.stopGame();
    this.stopBackgroundMusic();
    this.clearCountdown();

    const isNewHighscore = this.score > this.highScore;

    if (isNewHighscore) {
      this.updateHighScore();
      this.showHighscoreMessage = true;
      this.playOneShot(this.audioHighscore);
      setTimeout(() => {
        this.showHighscoreMessage = false;
      }, 2000);
    } else {
      this.updateHighScore();
      this.showHighscoreMessage = false;
    }

    this.isRevivePending = false;
    this.isReviveChoicePending = false;
    this.revivePauseActive = false;
    this.showReviveFlash = false;
    this.showResumeHint = false;

    this.reviveWords = [];
    this.currentReviveWordIndex = 0;
    this.reviveCorrectCount = 0;
    this.userReviveAnswer = '';

    this.snake = [{ x: 10, y: 10 }];
    this.previousSnakeState = this.cloneSnake();
    this.food = this.generateFood();
    this.direction = 'right';
    this.gameSpeedDelay = 200;
    this.updateScore();
    this.draw();
  }

  /* ------------------------------------------------------------------------
   * Input / Tastatursteuerung
   * --------------------------------------------------------------------- */

  /**
   * Globale Key-Events:
   * - Enter im Revive-Quiz = Antwort bestätigen
   * - Space = Start oder nach Revive fortsetzen
   * - Pfeiltasten = Richtungsänderung der Schlange
   */
  @HostListener('document:keydown', ['$event'])
  handleKeyPress(event: KeyboardEvent): void {
    // Während Vokabel-Quiz: Enter = Bestätigen
    if (this.isRevivePending) {
      if (event.key === 'Enter') {
        event.preventDefault();
        this.handleReviveAnswer();
      }
      return;
    }

    // Während Revive-Auswahl: keine Steuerung, nur Buttons
    if (this.isReviveChoicePending) {
      return;
    }

    // Nach Revive: Leertaste zum Weiterspielen
    if (this.revivePauseActive) {
      if (event.code === 'Space' || event.key === ' ') {
        event.preventDefault();
        this.revivePauseActive = false;
        this.showResumeHint = false;
        this.gameStarted = true;
        this.restartGameLoop();
        this.draw();
      }
      return;
    }

    // Normaler Start per Leertaste
    if (event.code === 'Space' || event.key === ' ') {
      event.preventDefault();
      this.startGame();
      return;
    }

    // Bewegung per Pfeiltasten (Richtungswechsel, aber kein U-Turn)
    switch (event.key) {
      case 'ArrowUp':
        if (this.direction !== 'down') {
          event.preventDefault();
          this.direction = 'up';
        }
        break;
      case 'ArrowDown':
        if (this.direction !== 'up') {
          event.preventDefault();
          this.direction = 'down';
        }
        break;
      case 'ArrowLeft':
        if (this.direction !== 'right') {
          event.preventDefault();
          this.direction = 'left';
        }
        break;
      case 'ArrowRight':
        if (this.direction !== 'left') {
          event.preventDefault();
          this.direction = 'right';
        }
        break;
    }
  }
}
