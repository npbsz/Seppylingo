import {Component, Input, OnDestroy} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-tts-button',
  imports: [MatIconModule, CommonModule, MatButtonModule],
  templateUrl: './tts-button.component.html',
  standalone: true,
  styleUrls: ['./tts-button.component.css']
})
/**
 * Creates a reusable Text to Speech button that is supported on most modern Browsers, afaik
 */
export class TtsButtonComponent implements OnDestroy {
  @Input() textToSpeak: string = 'Hello, world!';
  @Input() lang: string = 'en-US';
  private synth: SpeechSynthesis;

  //gets the browser specific speechSynthesis
  constructor() {
    this.synth = window.speechSynthesis;
  }
  speak(): void {
    if (this.synth.speaking) {
      console.error('Speech ongoing, please wait.');
      return;
    }

    if (!this.textToSpeak) {
      console.error('No text provided.');
      return;
    }

    const utterance = new SpeechSynthesisUtterance(this.textToSpeak);

    utterance.lang = this.lang;
    utterance.pitch = 1;      //0 - 2
    utterance.rate = 1;       //0.1 - 10
    utterance.volume = 1;     //0 - 1

    utterance.onend = () => {
      console.log('Speech finished.');
    };

    utterance.onerror = (event) => {
      console.error('Speech synthesis error:', event);
    };

    this.synth.speak(utterance);
  }

  stop(): void {
    if (this.synth.speaking) {
      this.synth.cancel();
    }
  }

  ngOnDestroy(): void {
    this.stop();
  }
}
