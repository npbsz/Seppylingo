import { BehaviorSubject, Observable } from "rxjs";
import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Injectable({ providedIn: 'root' })
export class GeminiService {
  // HIER IST DIE GROSSE ÄNDERUNG:
  private currentSystemPrompt = new BehaviorSubject<string>(
    'Du bist Seppy, ein freundlicher und motivierender Englisch-Tutor. ' +
    'VERHALTEN: Sei locker, witzig und schlagfertig.  ' +
    'Fokussiere dich auf das Beibringen der englischen Sprache. Wenn der Nutzer über andere Themen reden will, mach ihn motivierend aufmerksam dass er hier ist um Englisch zu lernen.'+
    'Korrigiere Fehler nur ganz beiläufig in deiner Antwort, ohne den Fluss zu stören. ' +

   'FORMATIERUNG: Nutze KEIN Markdown (**Fett**, *Kursiv*). Nutze Text-Emoticons wie :) :D ^^ ;). Keine modernen Emojis.'
  );

  private currentContext = new BehaviorSubject<string>('Dashboard');
  private totalUserScore: number = 0;

  constructor(private http: HttpClient) {}

  setModuleConfig(systemPrompt: string, context: string) {
    this.currentSystemPrompt.next(systemPrompt);
    this.currentContext.next(context);
  }

  setTotalScore(points: number) {
    console.log("🤖 AI weiß jetzt: User Level Punkte = " + points);
    this.totalUserScore = points;
  }

  ask(message: string): Observable<any> {
    // Level-Logik bleibt, aber die Anweisung ändert sich zu "Unterhaltung" statt "Lehren"
    let skillLevel = "Anfänger (A1)";
    let styleInstruction = "Nutze einfache Alltagssprache.";

    if (this.totalUserScore > 300) {
      skillLevel = "Fortgeschritten (A2/B1)";
      styleInstruction = "Sprich ganz normal, wie mit einem Freund.";
    }
    if (this.totalUserScore > 1000) {
      skillLevel = "Experte (B2/C1)";
      styleInstruction = "Nutze Slang und Umgangssprache. Sei fordernd.";
    }

    const enrichedContext = `
      ${this.currentContext.value} |
      User Score: ${this.totalUserScore} |
      Level: ${skillLevel}.
      Anweisung: ${styleInstruction}
    `;

    const payload = {
      message,
      systemPrompt: this.currentSystemPrompt.value,
      context: enrichedContext
    };

    return this.http.post<any>('http://localhost:8080/api/ai/chat', payload);
  }
}
