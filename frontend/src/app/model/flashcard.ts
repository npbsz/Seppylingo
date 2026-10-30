
export interface FlashcardDto {
  id: number;
  front: string;
  back: string;
  dir: 'en-de' | 'de-en';
}

export interface AnswerResultDto {
  ok: boolean;
  points: number;
  mistakes: number;
}
