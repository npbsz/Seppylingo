import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ListExGame} from '../model/listeningExercise.type';

@Injectable({
  providedIn: 'root'
})
export class ListeningExerciseManagerService {

  private apiUrl = 'http://localhost:8080/api/listEx';

  constructor(private http: HttpClient) { }

  /**
   * gets new game via api call to backend
   */
  getGame(): Observable<ListExGame> {
    return this.http.get<ListExGame>(`${this.apiUrl}/new`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: any) {
    console.error('An error occurred:', error.message);
    return throwError(() => new Error('Error occurred; please try again later.'));
  }
}
