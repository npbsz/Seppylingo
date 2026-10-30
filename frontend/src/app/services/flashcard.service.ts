import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FlashcardDto, AnswerResultDto } from '../model/flashcard';

// Proxy / Base URL
const BASE = 'http://localhost:8080/api/flashcards';

const AI_EXPLAIN_URL = 'http://localhost:8080/api/ai/flashcards/explain';

export type ExplainDirection = 'en-de' | 'de-en';

export interface FlashcardExplainRequest {
  direction: ExplainDirection;
  front: string;
  back: string;
}

export interface FlashcardExplainResponse {
  explanation: string;
  examples: string[];
  usageTips?: string[];
}

@Injectable({ providedIn: 'root' })
export class FlashcardService {
  constructor(private http: HttpClient) {}

  // ============================================================
  // TRAININGS-KARTE LADEN
  // ============================================================
  next(dir: 'en-de' | 'de-en' = 'en-de'): Observable<FlashcardDto> {
    return this.http.get<FlashcardDto>(`${BASE}/next`, {
      params: { dir } as any
    });
  }

  // ============================================================
  // FEHLER-KARTE LADEN
  // ============================================================
  nextError(dir: 'en-de' | 'de-en' = 'en-de'): Observable<FlashcardDto> {
    return this.http.get<FlashcardDto>(`${BASE}/next-error`, {
      params: { dir } as any
    });
  }

  // ============================================================
  // TRAININGS-ANTWORT
  // ============================================================
  answer(cardId: number, isRight: boolean): Observable<AnswerResultDto> {
    return this.http.post<AnswerResultDto>(`${BASE}/answer`, {
      cardId,
      isRight
    });
  }

  // ============================================================
  // FEHLER-MODUS ANTWORT
  // ============================================================
  answerError(cardId: number, isRight: boolean): Observable<AnswerResultDto> {
    return this.http.post<AnswerResultDto>(`${BASE}/answer-error`, {
      cardId,
      isRight
    });
  }

  // ============================================================
  // FEHLER-ZÄHLER IM BACKEND LÖSCHEN
  // ============================================================
  reset(): Observable<string> {
    return this.http.delete(`${BASE}/reset`, { responseType: 'text' });
  }

  // ============================================================
  // KI: KARTE ERKLÄREN (NEU)
  // ============================================================
  explain(payload: FlashcardExplainRequest): Observable<FlashcardExplainResponse> {
    return this.http.post<FlashcardExplainResponse>(AI_EXPLAIN_URL, payload);
  }
}
