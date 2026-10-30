import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { Location, CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { GeminiService } from '../../services/gemini.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, AfterViewChecked {

  @ViewChild('chatContainer') private chatContainer!: ElementRef;

  langMenuOpen = false;
  showBackButton = false;
  chatOpen = false;
  currentRoute = "";
  userInput = "";
  isNudging = false;

  // Begrüßung etwas lockerer machen
  messages: { role: string, text: string }[] = [
    { role: 'assistant', text: 'Hey! I am Seppy. Ready to chat? ^^' }
  ];

  constructor(
    private location: Location,
    private router: Router,
    private geminiService: GeminiService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.fetchUserProfile();

    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.showBackButton = (this.router.url !== '/' && this.router.url !== '/login' && this.router.url !== '/register');
        this.currentRoute = this.router.url;
        this.updateAiContext();
      }
    });
  }

  fetchUserProfile() {
    this.userService.getProfile().subscribe({
      next: (data) => {
        const score = data.score || data.points || data.totalScore || 0;
        this.geminiService.setTotalScore(score);
      },
      error: (err) => {
        console.warn("Konnte Profil nicht laden.", err);
      }
    });
  }

  ngAfterViewChecked() {
    // Manuelles Scrollen
  }

  updateAiContext() {

    const basePersonality = "Du bist ein freundlicher und motivierender Englisch-Tutor. " ;

    let specificContext = "User ist im Dashboard.";

    if (this.currentRoute.includes('snake')) {
      specificContext = "User spielt Snake. Feuere ihn an oder ärgere ihn spielerisch, wenn er verliert.";
    } else if (this.currentRoute.includes('multiple-choice')) {
      specificContext = "User macht ein Quiz. Gib nur kurze Tipps, keine Vorlesung.";
    }

    this.geminiService.setModuleConfig(basePersonality, specificContext);
  }

  toggleChat() {
    this.chatOpen = !this.chatOpen;
    if (this.chatOpen) {
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  // --- MENÜ FUNKTIONEN ---

  saveChat() {
    if (this.messages.length <= 1) return;

    const chatContent = this.messages
      .map(m => `[${m.role === 'assistant' ? 'Seppy' : 'You'}]: ${m.text}`)
      .join('\n\n');

    const blob = new Blob([chatContent], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'seppylingo_chat.txt';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  clearChat() {
    this.messages = [
      { role: 'assistant', text: 'Alles weg! Worüber wollen wir quatschen? :D' }
    ];
  }

  triggerNudge() {
    this.isNudging = true;
    setTimeout(() => {
      this.isNudging = false;
    }, 500);
  }

  rickRoll() {
    window.open('https://youtu.be/xvFZjo5PgG0?si=L7ckNIXH4p7tHp9I', '_blank');

    this.messages.push({
      role: 'assistant',
      text: 'Hahaha. Gotcha! XD Genug Spaß, weiter gehts!'
    });

    setTimeout(() => this.scrollToBottom(), 100);
  }


  // --- CHAT LOGIK ---

  handleEnter(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      if (!event.shiftKey) {
        event.preventDefault();
        this.sendMessage();
      }
    }
  }

  sendMessage() {
    if (!this.userInput.trim()) return;

    const userMsg = this.userInput;
    this.messages.push({ role: 'user', text: userMsg });
    this.userInput = '';

    setTimeout(() => this.scrollToBottom(), 50);

    this.geminiService.ask(userMsg).subscribe({
      next: (response: any) => {
        const aiText = response.candidates?.[0]?.content?.parts?.[0]?.text || "Sorry, Blackout. :/";
        this.messages.push({ role: 'assistant', text: aiText });
        setTimeout(() => this.scrollToBottom(), 50);
      },
      error: (err) => {
        console.error('Gemini API Fehler:', err);
        this.messages.push({ role: 'assistant', text: 'Server ist down oder so. Geht grad nicht. -.-' });
        setTimeout(() => this.scrollToBottom(), 50);
      }
    });
  }

  scrollToBottom(): void {
    try {
      if (this.chatContainer) {
        this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
      }
    } catch(err) {
      console.log('Scroll Fehler ignoriert');
    }
  }

  toggleLangMenu() { this.langMenuOpen = !this.langMenuOpen; }
  goBack() { this.location.back(); }
  goToProfile() { this.router.navigate(['/profile']); }
  goToDashboard() { this.router.navigate(['/']); }
}
