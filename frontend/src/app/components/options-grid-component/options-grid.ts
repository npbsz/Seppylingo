import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-options-grid',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './options-grid.html',
  styleUrls: ['./options-grid.css']
})
export class OptionsGridComponent {
  /** The list of shuffled text options to display */
  @Input() options: string[] = [];

  /** The option currently selected by the user */
  @Input() selectedOption: string | null = null;

  /** * If provided, the component compares this against the selected option
   * to determine if it should show Green (success) or Red (error).
   */
  @Input() correctOption: string | null = null;

  /** * Controls when to reveal the colors.*/
  @Input() showFeedback: boolean = false;

  /** Prevents interaction */
  @Input() disabled: boolean = false;

  @Output() optionSelected = new EventEmitter<string>();

  select(option: string) {
    if (this.disabled) return;
    this.optionSelected.emit(option);
  }

  // Helper to determine button color state
  getButtonClass(option: string): string {
    // Basic Selection (Blue)
    if (!this.showFeedback && this.selectedOption === option) {
      return 'selected-primary';
    }

    // Feedback Mode (Green/Red)
    if (this.showFeedback) {
      if (option === this.correctOption) {
        return 'btn-success'; // Correct answer always turns green
      }
      if (option === this.selectedOption && option !== this.correctOption) {
        return 'btn-error'; // Wrong selection turns red
      }
    }

    return '';
  }
}
