import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {DashboardComponent} from './components/dashboard/dashboard';
import { SnakeGameComponent } from './components/snake-game/snake-game.component';
import { LueckentextComponent } from './components/lueckentext/lueckentext.component';
import {ListeningExerciseComponent} from './components/listening-exercise/listeningExercise.component';
import { HeaderComponent } from './components/header/header.component';
import {BubbleBobbleComponent} from './components/bubble-bobble/bubble-bobble';
import { SeppyClippyComponent } from './components/seppy-clippy/seppy-clippy.component';
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, SeppyClippyComponent],

  standalone:true,
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Seppylingo');
}
