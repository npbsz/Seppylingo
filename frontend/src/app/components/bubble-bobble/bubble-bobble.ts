// noinspection AngularUnusedComponentImport

import {Component,OnInit, OnDestroy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import * as Phaser from 'phaser';
//scenes
import {MainGameScene} from './main-game-scene'
import {TitleScene} from './title-scene'
import {GameTaskService} from './bubbleBobbleService'
import {LueckentextComponent} from '../lueckentext/lueckentext.component'

@Component({
  selector: 'app-bubble-bobble-game',
  templateUrl: './bubble-bobble.html',
  styleUrls: ['./bubble-bobble.css'],
  standalone: true,
  imports: [CommonModule, LueckentextComponent]
})
export class BubbleBobbleComponent implements OnInit, OnDestroy {
  private game!: Phaser.Game;
  isShowingTask=false;
  constructor(
    private gameTaskService: GameTaskService,
    private router: Router) {}

  ngOnInit() {
    this.gameTaskService.goToLandingPage$.subscribe(() => {
      this.router.navigate(['/']);
    });
    this.gameTaskService.showTask$.subscribe(() => {
      this.isShowingTask = true;
    });
    const config: Phaser.Types.Core.GameConfig = {
      type: Phaser.AUTO,
      width: 800,
      height: 600,
      // 'parent' must match html container id
      parent: 'game-canvas-container',
      scale:{ mode: Phaser.Scale.FIT,
        autoCenter: Phaser.Scale.CENTER_BOTH,
        autoRound: true,
      },
      pixelArt: true,
      physics: {
        default: 'arcade',
        arcade: {
          gravity: {x:0, y:1000 },
          debug: false,
        },
      },
      scene: [TitleScene, MainGameScene],
    };

    this.game = new Phaser.Game(config);
    this.game.registry.set('gameTaskService', this.gameTaskService);
  }
  handleTaskResult(score:number){
    this.isShowingTask=false;
    this.gameTaskService.taskResult$.next(score);
  }
  ngOnDestroy() {
    if (this.game) {
      this.game.destroy(true);
    }
  }
}
