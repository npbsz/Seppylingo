import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError,map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {LueckentextGame, LueckentextGameListResponse} from '../model/lueckentext.type';

@Injectable({
  providedIn: 'root'
})
export class LueckentextManagerService {

  private apiUrl = 'http://localhost:8080/api/lueckentext';
  private aiUrl= 'http://localhost:8080/api/ai/lueckentext/newai';

  constructor(private http: HttpClient) { }

  /**
   * gets new game via api call to backend
   */
  getGame(): Observable<LueckentextGame> {
    return this.http.get<LueckentextGame>(`${this.apiUrl}/new`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * gets ai game via backend call to aicontroller
   * maps the gemini json to current variables
   * @param theme of the generated Lueckentext items
   * @param count amount of generated Lueckenetext item
   */
  getAiTasks(theme: string, count: string): Observable<LueckentextGame[]> {
    return this.http.post<LueckentextGameListResponse>(this.aiUrl, { theme,count }).pipe(
      map(response => {
        return response.tasks.map((task: { englishWithGap: any; german: any; options: any; missingWord: any; synonyms: any; }) => ({
          textWithBlank: task.englishWithGap,
          translation: task.german,
          gameId: 'ai-generated',
          dummyWords: task.options || [],
          answer: task.missingWord,
          synonyms: task.synonyms || []
        }));
      }),
      catchError(this.handleError)
    );
  }
  private handleError(error: any) {
    console.error('An error occurred:', error.message);
    return throwError(() => new Error('Error occurred; please try again later.'));
  }
}
