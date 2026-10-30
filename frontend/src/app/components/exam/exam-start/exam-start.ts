import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ExamService } from '../../../services/exam.service';

type ExamMode = 'intro' | 'final';

@Component({
  selector: 'app-exam-start',
  standalone: true,
  templateUrl: './exam-start.html',
  styleUrls: ['./exam-start.css']
})
export class ExamStartComponent {

  loading = false;

  constructor(
    private examService: ExamService,
    private router: Router
  ) {}

  get examMode(): ExamMode {
    const mode = sessionStorage.getItem('examMode');
    return mode === 'intro' ? 'intro' : 'final';
  }

  get title(): string {
    return this.examMode === 'intro' ? 'EINFÜHRUNGSPRÜFUNG' : 'ABSCHLUSSPRÜFUNG';
  }

  get subtitle(): string {
    return this.examMode === 'intro'
      ? 'Kurzer Einstufungstest – danach bekommst du eine Einschätzung.'
      : 'Bereit für die Abschlussprüfung?';
  }

  get hintText(): string {
    return this.examMode === 'intro'
      ? 'Tipp: Mach dir keinen Stress – es geht nur um deinen Startpunkt 🙂'
      : 'Tipp: Hörverstehen mit Kopfhörern macht’s leichter 🎧';
  }

  startExam(): void {
    this.loading = true;

    this.examService.startSimpleExam().subscribe({
      next: exam => {
        this.examService.setExam(exam);
        this.loading = false;
        this.router.navigate(['/exam/question']);
      },
      error: () => {
        this.loading = false;
        alert('Fehler beim Starten der Prüfung');
      }
    });
  }
}
