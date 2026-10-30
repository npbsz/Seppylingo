import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LueckentextGame } from '../../model/lueckentext.type';
import { LueckentextManagerService } from '../../services/lueckentext-manager.service';
import {TtsButtonComponent} from '../tts-button-component/tts-button.component';
import {OptionsGridComponent} from '../options-grid-component/options-grid';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-lueckentext',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    CommonModule,
    MatSlideToggleModule,
    FormsModule,
    OptionsGridComponent,
    TtsButtonComponent
  ],
  templateUrl: './lueckentext.component.html',
  styleUrls: ['./lueckentext.component.css']
})
export class LueckentextComponent implements OnInit {
  @Output() xpEarned=new EventEmitter<number>();
  viewMode: 'selection'|'ai-input'|'playing'='selection';
  isLoading = false;

  tasks: LueckentextGame[] = [];
  currentIndex = 0;
  textParts: string[] = [];
  answerOptions: string[] = [];

  userAnswer = '';
  isCorrect: boolean | null = null;
  feedback = '';
  isSubmitted = false;
  aiTheme = '';
  taskCount='';

  gameData: LueckentextGame | null = null;
  isGameWon = false;
  isMultipleChoiceMode = false;
  readonly XP_Reward=10;

  // Inject new service
  constructor(
    private lueckentextManager: LueckentextManagerService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.viewMode = 'selection';
  }
  startAiMode(){
    this.viewMode = 'ai-input';
  }

  generateAiTasks(): void {
    if (!this.aiTheme.trim()) return;
    this.isLoading = true;
    this.lueckentextManager.getAiTasks(this.aiTheme, this.taskCount).subscribe({
      next: (data) => {
        this.tasks = data;
        this.currentIndex = 0;
        this.isMultipleChoiceMode = this.tasks[0].dummyWords.length > 0;
        this.setupCurrentTask();
        this.viewMode = 'playing';
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  loadNormalGame(): void {
    this.isLoading = true;
    this.lueckentextManager.getGame().subscribe(data => {
      this.tasks = [data];
      this.currentIndex = 0;
      this.setupCurrentTask();
      this.viewMode = 'playing';
      this.isLoading = false;
    });
  }
  setupCurrentTask(): void {
    const current = this.tasks[this.currentIndex];
    this.isSubmitted = false;
    this.isCorrect = null;
    this.feedback = '';
    this.userAnswer = '';

    this.textParts = current.textWithBlank.split('____');
    const allOptions = [current.answer, ...current.dummyWords];
    this.answerOptions = this.shuffleArray(allOptions);
  }
  //clears answer when switching gameModes
  resetAnswer() {
    this.userAnswer = '';
  }

  /**
   * Prepares the data for the Lueckentext game
   * splits String to make space for html <Form>
   * shuffles the multiple choice words so that the answer isnt always first option
   */
  loadGame(): void {
    this.isLoading = true;
    this.isSubmitted = false;
    this.isCorrect = null;
    this.feedback = '';
    this.userAnswer = '';
    this.isGameWon = false;
    this.lueckentextManager.getGame().subscribe(data => {
      this.gameData = data;
      this.textParts = data.textWithBlank.split('____');
      this.isLoading = false;
      const allOptions=[this.gameData.answer,
        ...this.gameData.dummyWords];
      this.answerOptions=this.shuffleArray(allOptions);
    });
  }

  /**
   * Helper function to shuffle the array
   * @param array String array
   */
  private shuffleArray(array:string[]):string[] {
    return array.map(value => ({value, sort:Math.random()}))
      .sort((a,b)=>a.sort-b.sort)
      .map(({value})=>value)
  }

  /**
   * Simple function to be called by a button in html
   * @param word that is selected
   */
  selectOption(word: string) {
    if (!this.isSubmitted) {
      this.userAnswer = word;
    }
  }

  /**
   * Method for submit button
   * checks whether answer is correct, ignores Upper/lower case and leading/trailing spaces in user answer
   */
  onSubmit(): void {
    if (!this.userAnswer || this.isSubmitted) return;

    const currentTask = this.tasks[this.currentIndex];
    this.isSubmitted = true;

    const isAnswerCorrect =
      this.userAnswer.trim().toLowerCase() === currentTask.answer.trim().toLowerCase() ||
      (currentTask.synonyms && currentTask.synonyms.some(s => s.toLowerCase() === this.userAnswer.trim().toLowerCase()));

    this.isCorrect = isAnswerCorrect;

    if (isAnswerCorrect) {
      if (this.currentIndex === this.tasks.length - 1) {
        const points = this.isMultipleChoiceMode ? (this.XP_Reward / 2) : this.XP_Reward;
        this.xpEarned.emit(Math.floor(points));
        this.savePointsToUser();
      }
    } else {
      this.xpEarned.emit(0);
    }
  }
  private savePointsToUser() {
    this.userService.addPoints(20).subscribe({
      next: (res) => console.log('20 Punkte für Lückentext erhalten!', res),
      error: (err) => console.error('Fehler beim Punkte speichern', err)
    });
  }
  nextTask() {
    this.currentIndex++;
    this.setupCurrentTask();
  }
}
