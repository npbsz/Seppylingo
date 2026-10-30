import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, of, catchError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ImageService {

  private readonly API_KEY = 'jiYY0ZFeT8fhhEX0auFTtuKZtKr2HYZ9';
  private readonly BASE_URL = 'https://api.giphy.com/v1/stickers/search';

  constructor(private http: HttpClient) {}

  getImageForWord(query: string): Observable<string | null> {
    if (!query) return of(null);

    // Suche direkt nach dem Wort
    const url = `${this.BASE_URL}?api_key=${this.API_KEY}&q=${encodeURIComponent(query)}&limit=1&rating=g`;

    return this.http.get<any>(url).pipe(
      map(response => {
        if (response.data && response.data.length > 0) {
          // Giphy liefert viele Formate.
          // 'fixed_height_small' ist gut für Spiele (schnell, klein).

          // Animierter Sticker
          return response.data[0].images.fixed_height.url;


        }
        return null;
      }),
      catchError(err => {
        console.error('Giphy API Fehler:', err);
        return of(null);
      })
    );
  }
}
