import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GameTaskService {
  taskResult$ = new Subject<number>();
  showTask$ = new Subject<void>();
  goToLandingPage$ = new Subject<void>();
}
