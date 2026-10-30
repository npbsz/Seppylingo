import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {TtsButtonComponent} from '../tts-button-component/tts-button.component';
import { ListExGame } from '../../model/listeningExercise.type';
import {ListeningExerciseManagerService} from '../../services/listeningExercise-manager.service';
import {OptionsGridComponent} from '../options-grid-component/options-grid';
import {CommonModule} from '@angular/common';
import {UserService} from '../../services/user.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-listening-exercise',
  imports: [CommonModule, FormsModule, MatButtonModule, TtsButtonComponent, OptionsGridComponent],
  templateUrl: './listeningExercise.component.html',
  standalone: true,
  styleUrl: './listeningExercise.component.css'
})
export class ListeningExerciseComponent implements OnInit{
  @Output() xpEarned=new EventEmitter<number>();
  isLoading = true;
  gameData: ListExGame | null = null;
  userAnswer = '';
  correctPhrase: string = '';
  isCorrect: boolean | null = null;
  feedback = '';
  isSubmitted = false;
  isGameWon = false;
  readonly XP_Reward=10;

  //Inject service
  constructor(private listExManServ: ListeningExerciseManagerService,
              private userService: UserService) { }

  ngOnInit(): void {
    this.loadGame();
  }

  loadGame(): void {
    this.isLoading = true;
    this.isSubmitted = false;
    this.isCorrect = null;
    this.feedback = '';
    this.userAnswer = '';
    this.isGameWon = false;
    this.listExManServ.getGame().subscribe(data=>{
      this.gameData = data;
      this.isLoading = false;
      const allOptions=this.gameData.phrases;
      const randomIndex=Math.floor(Math.random()*allOptions.length);
      this.correctPhrase=allOptions[randomIndex];
    })
  }
  selectOption(word: string) {
    if (!this.isSubmitted) {
      this.userAnswer = word;
    }
  }
  onSubmit(): void {
    if (!this.userAnswer || !this.gameData || this.isSubmitted) {
      return;
    }
    this.isSubmitted = true;
    const correctAnswer = this.correctPhrase;
    const isAnswerCorrect = this.userAnswer.trim().toLowerCase() === correctAnswer.trim().toLowerCase();
    this.isCorrect = isAnswerCorrect;
    if (isAnswerCorrect) {
      this.feedback = correctAnswer;
      this.isGameWon = true;
      this.xpEarned.emit(this.XP_Reward);
      this.userService.addPoints(15).subscribe({
        next: (res) => console.log('15 Punkte für Hörverstehen erhalten!', res),
        error: (err) => console.error('Fehler beim Punkte speichern', err)
      });
    } else {
      this.xpEarned.emit(0);
      this.feedback = correctAnswer;
    }
  }
}
