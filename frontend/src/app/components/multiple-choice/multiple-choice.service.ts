import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Typen für Backend-DTOs
export interface MultipleChoiceQuestion {
  wordId: number;
  wordToTranslate: string;
  options: string[];
  totalWordsInTheme: number;
}

export interface AnswerCheckRequest {
  wordId: number;
  selectedAnswer: string;
}

@Injectable({ providedIn: 'root' })
export class MultipleChoiceService {
  private readonly baseUrl = 'http://localhost:8080/api/exercises';

  constructor(private http: HttpClient) {}

  // Holt alle Themen
  getThemes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/themes`);
  }

  // Holt eine Multiple-Choice-Frage zu einem Thema
  getQuestion(theme: string): Observable<MultipleChoiceQuestion> {
    return this.http.get<MultipleChoiceQuestion>(
      `${this.baseUrl}/multiple-choice/${theme}`
    );
  }

  // Prüft die Antwort
  checkAnswer(body: AnswerCheckRequest): Observable<boolean> {
    return this.http.post<boolean>(
      `${this.baseUrl}/multiple-choice/check`,
      body
    );
  }

  // Holt eine Frage zu einer bestimmten ID
  getQuestionById(id: number): Observable<MultipleChoiceQuestion> {
    return this.http.get<MultipleChoiceQuestion>(
      `${this.baseUrl}/question/${id}`
    );
  }
}
