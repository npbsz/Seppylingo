import { Routes } from '@angular/router';
import { ProfileComponent } from './components/profile/profile.component';
import { DashboardComponent } from './components/dashboard/dashboard';
import { MultipleChoiceComponent } from './components/multiple-choice/multiple-choice.component';
import { FlashcardComponent } from './components/flashcards/flashcards.component';
import { SnakeGameComponent } from './components/snake-game/snake-game.component';
import { ListeningExerciseComponent } from './components/listening-exercise/listeningExercise.component';
import { LueckentextComponent } from './components/lueckentext/lueckentext.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { authGuard } from './guards/auth.guard';
import {BubbleBobbleComponent} from './components/bubble-bobble/bubble-bobble';
import { ExamShellComponent } from './components/exam/exam-shell/exam-shell';
import { ExamStartComponent } from './components/exam/exam-start/exam-start';
import { ExamQuestionComponent } from './components/exam/exam-question/exam-question';
import { ExamResultComponent } from './components/exam/exam-result/exam-result';
import {ImageQuizComponent} from './components/image-quiz/image-quiz.component';

export const routes: Routes = [
  // Auth-Routes (öffentlich, kein Guard)
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },

  //EXAM (GESCHÜTZT) – damit /exam funktioniert
  {
    path: 'exam',
    component: ExamShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: ExamStartComponent },
      { path: 'question', component: ExamQuestionComponent },
      { path: 'result', component: ExamResultComponent }
    ]
  },

  // Dashboard (GESCHÜTZT)
  {
    path: '',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [authGuard]
  },
  // Game-Routes (ALLE GESCHÜTZT)
  {
    path: 'games/snake',
    component: SnakeGameComponent,
    canActivate: [authGuard]
  },
  {
    path: 'games/lueckentext',
    component: LueckentextComponent,
    canActivate: [authGuard]
  },
  {
    path: 'games/listening-exercise',
    component: ListeningExerciseComponent,
    canActivate: [authGuard]
  },
  {
    path: 'games/flashcards',
    component: FlashcardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'games/multiple-choice',
    component: MultipleChoiceComponent,
    canActivate: [authGuard]
  },

  {
    path: 'games/image-quiz',
    component: ImageQuizComponent,
    canActivate: [authGuard]
  },
  {
    path: 'games/bubble-bobble',
    component: BubbleBobbleComponent,
    canActivate: [authGuard]
  },

  // Catch-all Route
  {
    path: '**',
    redirectTo: ''
  }
];
