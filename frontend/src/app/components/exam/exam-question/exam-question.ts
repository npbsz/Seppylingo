import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ExamService } from '../../../services/exam.service';
import { Exam, ExamQuestion } from '../../../model/exam.model';
import { CommonModule } from '@angular/common';
import { TtsButtonComponent } from '../../tts-button-component/tts-button.component';

@Component({
  selector: 'app-exam-question',
  standalone: true,
  imports: [CommonModule, TtsButtonComponent],
  templateUrl: './exam-question.html',
  styleUrls: ['./exam-question.css']
})
export class ExamQuestionComponent implements OnInit {

  exam!: Exam;
  currentQuestion!: ExamQuestion;
  index = 0;

  // GAP_TEXT & LISTENING State
  options: string[] = [];
  selectedAnswer = '';
  isSubmitted = false;

  constructor(
    private examService: ExamService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const exam = this.examService.getExam();

    if (!exam) {
      this.router.navigate(['/exam']);
      return;
    }

    this.exam = exam;
    this.loadQuestion();
  }

  private loadQuestion(): void {
    this.currentQuestion = this.exam.questions[this.index];
    this.selectedAnswer = '';
    this.isSubmitted = false;
    this.options = [];

    if (this.currentQuestion.type === 'GAP_TEXT') {
      this.buildGapTextOptions();
    }

    if (this.currentQuestion.type === 'LISTENING') {
      this.buildListeningOptions();
    }
  }

  /* ================= MULTIPLE CHOICE ================= */

  selectAnswer(answer: string): void {
    this.examService.saveAnswer(this.currentQuestion.questionId, answer);
    this.nextQuestion();
  }

  /* ================= GAP TEXT ================= */

  private buildGapTextOptions(): void {
    const gap = this.currentQuestion.gapText;
    if (!gap) return;

    const allOptions = [
      gap.answer,
      ...(gap.dummyWords ?? [])
    ].filter(Boolean);

    this.options = this.shuffleArray(allOptions);
  }

  confirmGapAnswer(): void {
    if (!this.selectedAnswer) return;

    this.examService.saveAnswer(
      this.currentQuestion.questionId,
      this.selectedAnswer
    );

    this.nextQuestion();
  }

  /* ================= LISTENING ================= */

  private buildListeningOptions(): void {
    const listening = this.currentQuestion.listening;
    if (!listening) return;

    this.options = this.shuffleArray([...listening.phrases]);
  }

  confirmListeningAnswer(): void {
    if (!this.selectedAnswer) return;

    this.examService.saveAnswer(
      this.currentQuestion.questionId,
      this.selectedAnswer
    );

    this.nextQuestion();
  }

  /* ================= HELPERS ================= */

  selectOption(option: string): void {
    if (!this.isSubmitted) {
      this.selectedAnswer = option;
    }
  }

  private shuffleArray(array: string[]): string[] {
    return array
      .map(value => ({ value, sort: Math.random() }))
      .sort((a, b) => a.sort - b.sort)
      .map(({ value }) => value);
  }

  private nextQuestion(): void {
    this.index++;

    //  LETZTE FRAGE → EXAM ABSCHICKEN
    if (this.index >= this.exam.questions.length) {

      this.examService
        .submitExam(this.exam.examId)
        .subscribe(result => {
          this.examService.setResult(result);
          this.router.navigate(['/exam/result']);
        });

      return;
    }

    //  NÄCHSTE FRAGE LADEN
    this.loadQuestion();
  }

}
