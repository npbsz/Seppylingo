export interface Exam {
  examId: string;
  passThresholdPercent: number;
  questions: ExamQuestion[];
}

export interface ExamQuestion {
  questionId: string;
  type: 'MULTIPLE_CHOICE' | 'GAP_TEXT' | 'LISTENING';
  multipleChoice?: MultipleChoiceQuestion;
  gapText?: any;
  listening?: any;
}

export interface MultipleChoiceQuestion {
  wordId: number;
  wordToTranslate: string;
  options: string[];
}

export interface ExamResult {
  total: number;
  correct: number;
  percentage: number;
  passed: boolean;
}
