import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Exam, ExamQuestion, ExamResult } from '../model/exam.model';

type ExerciseType = 'MULTIPLE_CHOICE' | 'GAP_TEXT' | 'LISTENING';

interface ExamSubmissionDto {
  examId: string;
  answers: ExamAnswerDto[];
}

interface ExamAnswerDto {
  questionId: string;
  type: ExerciseType;
  answer: string;
}

@Injectable({
  providedIn: 'root'
})
export class ExamService {

  // Wenn Proxy korrekt eingerichtet ist, bleibt das /api genau richtig.
  // Dann gehen Requests automatisch an http://localhost:8080
  private readonly API_BASE = '/api/exam';

  private exam: Exam | null = null;
  private result: ExamResult | null = null;

  // pro QuestionId genau eine Antwort
  private answers = new Map<string, ExamAnswerDto>();

  constructor(private http: HttpClient) {}

  /* ================= START ================= */

  startSimpleExam() {
    return this.http.post<Exam>(`${this.API_BASE}/simple/start`, {});
  }

  setExam(exam: Exam): void {
    this.exam = exam;
  }

  getExam(): Exam | null {
    return this.exam;
  }

  clearExam(): void {
    this.exam = null;
  }

  /* ================= ANSWERS ================= */

  saveAnswer(questionId: string, answer: string): void {
    const type = this.getQuestionType(questionId);
    if (!type) return;

    this.answers.set(questionId, {
      questionId,
      type,
      answer
    });
  }

  private getQuestionType(questionId: string): ExerciseType | null {
    const q: ExamQuestion | undefined =
      this.exam?.questions?.find(x => x.questionId === questionId);

    return (q?.type as ExerciseType) ?? null;
  }

  /* ================= SUBMIT ================= */

  submitExam(examId: string) {
    const payload: ExamSubmissionDto = {
      examId,
      answers: Array.from(this.answers.values())
    };

    return this.http.post<ExamResult>(`${this.API_BASE}/submit`, payload);
  }

  /* ================= RESULT ================= */

  setResult(result: ExamResult): void {
    this.result = result;
  }

  getResult(): ExamResult | null {
    return this.result;
  }

  clearResult(): void {
    this.result = null;
  }

  reset(): void {
    this.exam = null;
    this.result = null;
    this.answers.clear();
  }
}
