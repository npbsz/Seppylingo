import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-exam-shell',
  standalone: true,
  templateUrl: './exam-shell.html',
  styleUrls: ['./exam-shell.css'],
  imports: [RouterOutlet]
})
export class ExamShellComponent {}
