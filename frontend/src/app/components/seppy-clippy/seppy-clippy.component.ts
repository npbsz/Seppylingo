import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-seppy-clippy',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './seppy-clippy.component.html',
  styleUrls: ['./seppy-clippy.component.css']
})
export class SeppyClippyComponent implements OnInit, OnDestroy {

  isVisible = false;
  hasImage = true; // Setze auf false, wenn du kein Bild hast
  currentMessage = "";

  private idleTime = 0;
  private idleInterval: any;

  // Die nervigen Sprüche im Retro-Stil
  private messages = [
    "Sieht so aus, als würdest du Englisch lernen. Brauchst du Hilfe? :D",
    "Na? Eingeschlafen? ^^",
    "Ich sehe, du bewegst die Maus nicht mehr. Alles okay? o.O",
    "Pssst... Vokabeln lernen sich nicht von alleine! ;)",
    "Langweilig... mach mal was! XD",
    "Ein bisschen Bewegung schadet nie! Wie wär's mit einer Runde Snake? 🐍",
    "Erinnerung: Trinken nicht vergessen! 🥤",
    "Hallo? Jemand zuhause? *Klopf Klopf*",
    "Ich bin hier, wenn du mich brauchst! :D",
    "Lass uns zusammen lernen!",
    "Zeit für eine kurze Pause? Stretching ist wichtig! 🧘‍♂️",
    "Wusstest du, dass regelmäßige Pausen die Produktivität steigern? ⏰",
    "Dein Englisch wird immer besser! Weiter so! 🚀",
    "Hey! Wie wäre es mit einem kleinen Spiel zwischendurch? 🎮",
    "Vergiss nicht, tief durchzuatmen! 🌬️",
    "Ich bin dein Lernbegleiter! Frag mich, wenn du Hilfe brauchst! 🤖",
  ];

  ngOnInit() {
    // Timer starten: Jede Sekunde prüfen
    this.idleInterval = setInterval(() => {
      this.timerIncrement();
    }, 1000);

    // Start-Begrüßung nach 2 Sekunden
    setTimeout(() => this.show("Hi! Ich bin Seppy. Ich pass auf dich auf! ^^"), 2000);
  }

  ngOnDestroy() {
    if (this.idleInterval) clearInterval(this.idleInterval);
  }

  // Setzt den Timer zurück, wenn User etwas tut
  @HostListener('document:mousemove')
  @HostListener('document:keydown')
  resetTimer() {
    this.idleTime = 0;
    // Wenn er sichtbar war und der User wieder aktiv wird, verschwindet er manchmal automatisch
    if (this.isVisible && Math.random() > 0.8) {
      this.hide();
    }
  }

  timerIncrement() {
    this.idleTime++;

    // Nach 15 Sekunden Inaktivität -> Nerven
    if (this.idleTime === 15) {
      this.showRandomMessage();
    }

    // Nach 60 Sekunden -> Nochmal nerven
    if (this.idleTime === 60) {
      this.show("Du bist ja immer noch weg... Ich geh dann mal Snake spielen. -.-");
    }
  }

  showRandomMessage() {
    const randomIndex = Math.floor(Math.random() * this.messages.length);
    this.show(this.messages[randomIndex]);
  }

  show(msg: string) {
    this.currentMessage = msg;
    this.isVisible = true;

    // Automatisches Ausblenden nach 8 Sekunden
    setTimeout(() => {
      this.isVisible = false;
    }, 8000);
  }

  hide() {
    this.isVisible = false;
  }

  pokeSeppy() {
    const reactions = ["Aua! >.<", "Nicht drücken! XD", "Kitzel mich nicht! ^^", "Hör auf! :D"];
    this.show(reactions[Math.floor(Math.random() * reactions.length)]);
    this.idleTime = 0;
  }
}
