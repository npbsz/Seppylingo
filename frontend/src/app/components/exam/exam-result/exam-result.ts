import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ExamService } from '../../../services/exam.service';
import { ExamResult } from '../../../model/exam.model';

type ExamMode = 'intro' | 'final';

@Component({
  selector: 'app-exam-result',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './exam-result.html',
  styleUrls: ['./exam-result.css']
})
export class ExamResultComponent implements OnInit {

  result!: ExamResult;
  examMode: ExamMode = 'final';

  constructor(
    private examService: ExamService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.examMode = sessionStorage.getItem('examMode') === 'intro' ? 'intro' : 'final';

    const res: any = this.examService.getResult();

    if (!res) {
      this.router.navigate(['/']);
      return;
    }

    // Kompatibel mit beiden Varianten:
    this.result = {
      total: res.total ?? res.totalQuestions ?? 0,
      correct: res.correct ?? res.correctAnswers ?? 0,
      percentage: res.percentage ?? 0,
      passed: res.passed ?? false
    };
  }

  goHome(): void {
    this.examService.reset();

    // Intro ist "einmalig" → nach Abschluss Mode entfernen
    if (this.examMode === 'intro') {
      sessionStorage.removeItem('examMode');
    }

    this.router.navigate(['/']);
  }
}
