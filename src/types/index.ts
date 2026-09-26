export type ReviewerTrack = 'OIC-NW' | 'GMDSS';

export type MasteryStatus = 'NOT_ATTEMPTED' | 'NEEDS_REVIEW' | 'IMPROVING' | 'MASTERED';

export interface Question {
  id: number;
  reviewer: ReviewerTrack;
  function?: string | null; // "F1", "F2", "F3"
  competencyCode: string; // "C1", "C2", ... "C17"
  competencyDescription: string;
  partNumber?: number | null;
  questionNumber?: number | null;
  questionText: string;
  questionType: string; // "Multiple Choice"
  optionA: string;
  optionB: string;
  optionC?: string | null;
  optionD?: string | null;
  optionE?: string | null;
  optionF?: string | null;
  correctAnswerLetter: string; // "A" | "B" | "C" | "D" | "E" | "F"
  correctAnswerIndex: number; // 0..5
  explanation?: string | null;
  point: number;
  section?: string | null;
  sourceSheet?: string | null;
  source?: string | null;
  isFavorite: boolean;
  isFlagged: boolean;
  userNotes?: string | null;
  masteryStatus: MasteryStatus;
  consecutiveCorrect: number;
  timesAttempted: number;
  timesCorrect: number;
  timesIncorrect: number;
  lastAnsweredTimestamp?: number | null;
  lastSelectedOption?: number | null;
  createdAt: number;
  updatedAt: number;
}

export interface ExamRecord {
  id: number;
  track: ReviewerTrack;
  examType: 'OFFICIAL_SIMULATION' | 'CUSTOM';
  title: string;
  timestamp: number;
  totalQuestions: number;
  score: number;
  percentage: number;
  passed: boolean;
  timeUsedSeconds: number;
  competencyBreakdownJson: string; // record of code -> { total, correct }
}

export interface ExamAnswer {
  id?: number;
  examId: number;
  questionId: number;
  questionText: string;
  selectedIndex: number; // -1 if skipped
  correctIndex: number;
  isCorrect: boolean;
  competencyCode: string;
}

export interface StudySession {
  id?: number;
  track: ReviewerTrack;
  timestamp: number;
  durationSeconds: number;
  questionsAnswered: number;
}

export interface DailyGoal {
  dateKey: string; // YYYY-MM-DD
  targetQuestions: number;
  completedQuestions: number;
  targetStudySeconds: number;
  completedStudySeconds: number;
  targetAccuracy: number;
}

export interface QuestionReport {
  id?: number;
  questionId: number;
  track: ReviewerTrack;
  reason: string;
  details: string;
  timestamp: number;
}

export interface SavedSessionLog {
  id: number;
  sessionType: 'PRACTICE' | 'EXAM';
  track: ReviewerTrack;
  title: string;
  subtitle: string;
  currentIndex: number;
  totalQuestions: number;
  answeredCount: number;
  lastActiveTimestamp: number;
  payloadJson: string;
}

export type StudyMode = 'TUTOR' | 'DRILL_EXAM' | 'FLASHCARD' | 'SPEED_RUN';

export type PracticeFilterMode =
  | 'ALL'
  | 'UNANSWERED'
  | 'INCORRECT'
  | 'FAVORITES'
  | 'FLAGGED'
  | 'NEEDS_REVIEW'
  | 'MASTERED'
  | 'SMART_REVIEW';

export interface PracticeArgs {
  title: string;
  filterMode?: PracticeFilterMode;
  competencyCode?: string | null;
  partNumber?: number | null;
  customQuestionIds?: number[] | null;
  itemLimit?: number | null;
  isRandomized?: boolean;
  shuffleOptions?: boolean;
  studyMode?: StudyMode;
  timeLimitSecondsPerItem?: number | null;
  resumeSessionLogId?: number | null;
  initialIndex?: number;
  initialAnswers?: Record<number, number>;
  initialResults?: Record<number, boolean>;
  randomSeed?: number;
}

export interface ActiveExamState {
  title: string;
  track: ReviewerTrack;
  examType: 'OFFICIAL_SIMULATION' | 'CUSTOM';
  questions: Question[];
  userAnswers: Record<number, number>; // questionId -> optionIndex
  flaggedQuestionIds: number[];
  currentIndex: number;
  timeLimitSeconds: number;
  timeRemainingSeconds: number;
  isSubmitted: boolean;
  completedRecordId?: number | null;
  savedSessionLogId?: number | null;
}

export interface CompetencyCount {
  competencyCode: string;
  total: number;
  mastered: number;
  attempted: number;
  correct: number;
}

export interface ImportValidationResult {
  reviewer: string;
  validCount: number;
  duplicateCount: number;
  totalParsed: number;
  errors: string[];
  warnings: string[];
  validatedQuestions: Question[];
}

export type ScreenType =
  | 'TrackSelect'
  | 'Home'
  | 'QuestionBank'
  | 'Practice'
  | 'ExamSetup'
  | 'ExamRunner'
  | 'ExamResult'
  | 'ExamReview'
  | 'Incorrect'
  | 'Favorites'
  | 'Flagged'
  | 'Search'
  | 'Stats'
  | 'StudyTimer'
  | 'PlayStore'
  | 'Admin';
