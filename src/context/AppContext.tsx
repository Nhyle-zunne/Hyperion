import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import {
  Question,
  ReviewerTrack,
  ScreenType,
  PracticeArgs,
  ActiveExamState,
  SavedSessionLog,
  CompetencyCount,
  DailyGoal,
  ImportValidationResult,
  PracticeFilterMode,
  StudyMode
} from '../types';
import * as db from '../db/indexedDb';
import { OIC_NW_COMPETENCIES, GMDSS_COMPETENCIES } from '../data/competencyMetadata';
import { processCsvContent } from '../data/seedQuestions';

interface OngoingSessionPromptState {
  existingSession: SavedSessionLog;
  newSessionTitle: string;
  onStartNew: () => void;
}

interface AppContextType {
  // Navigation
  currentScreen: ScreenType;
  navigateTo: (screen: ScreenType) => void;
  navigateBack: () => boolean;
  popToHome: () => void;
  practiceArgs: PracticeArgs | null;
  setPracticeArgs: (args: PracticeArgs | null) => void;
  activeRecordId: number | null;
  setActiveRecordId: (id: number | null) => void;

  // Track
  currentTrack: ReviewerTrack;
  selectTrack: (track: ReviewerTrack) => void;

  // Questions
  allQuestions: Question[];
  favorites: Question[];
  flagged: Question[];
  incorrect: Question[];
  competencyStats: CompetencyCount[];
  refreshQuestions: () => Promise<void>;
  toggleFavorite: (q: Question) => Promise<void>;
  toggleFlag: (q: Question) => Promise<void>;
  recordPracticeAnswer: (q: Question, selectedIndex: number) => Promise<void>;
  removeFromIncorrect: (questionId: number) => Promise<void>;
  reportQuestion: (questionId: number, reason: string, details: string) => Promise<void>;

  // Session Logs (Resume)
  sessionLogs: SavedSessionLog[];
  latestOngoingSession: SavedSessionLog | null;
  ongoingPrompt: OngoingSessionPromptState | null;
  dismissOngoingSessionPrompt: () => void;
  resumeSession: (log: SavedSessionLog) => Promise<void>;
  deleteSessionLog: (id: number) => Promise<void>;
  clearAllSessionLogs: (trackOnly?: boolean) => Promise<void>;
  requestStartPractice: (args: PracticeArgs, onStartNew?: () => void) => Promise<void>;
  requestStartOfficialExam: () => Promise<void>;
  requestStartCustomExam: (selectedComps: string[], questionCount: number, timeLimitMinutes: number) => Promise<void>;
  savePracticeSession: (params: {
    title: string;
    filterMode: PracticeFilterMode;
    studyMode: StudyMode;
    competencyCode: string | null;
    partNumber: number | null;
    itemLimit: number | null;
    isRandomized: boolean;
    shuffleOptions: boolean;
    timeLimitSecondsPerItem: number | null;
    randomSeed: number;
    questions: Question[];
    currentIndex: number;
    answers: Record<number, number>;
    results: Record<number, boolean>;
    existingLogId?: number | null;
  }) => Promise<void>;

  // Active Exam
  activeExam: ActiveExamState | null;
  startOfficialExam: () => Promise<void>;
  startCustomExam: (selectedCompetencies: string[], questionCount: number, timeLimitMinutes: number) => Promise<void>;
  selectExamAnswer: (questionId: number, optionIndex: number) => void;
  toggleExamQuestionFlag: (questionId: number) => void;
  setExamCurrentIndex: (index: number) => void;
  submitExam: () => Promise<void>;
  saveExamSession: () => Promise<void>;

  // Stopwatch & Study Timer
  isStopwatchRunning: boolean;
  stopwatchSeconds: number;
  startStopwatch: () => void;
  pauseStopwatch: () => void;
  stopAndSaveStopwatch: () => Promise<void>;
  totalStudySeconds: number;
  todayGoal: DailyGoal | null;
  refreshTodayGoal: () => Promise<void>;
  updateGoal: (updates: Partial<DailyGoal>) => Promise<void>;

  // Admin & Import
  isProcessing: boolean;
  importPreview: ImportValidationResult | null;
  setImportPreview: (preview: ImportValidationResult | null) => void;
  adminMessage: string | null;
  setAdminMessage: (msg: string | null) => void;
  dismissAdminMessage: () => void;
  confirmImport: () => Promise<void>;
  cancelImportPreview: () => void;
  resetReviewerDatabase: (track: ReviewerTrack) => Promise<void>;
  reseedDatabase: () => Promise<void>;
  scanAndImportAssets: () => Promise<void>;
  exportBackupJson: () => Promise<string>;
  restoreBackupJson: (json: string) => Promise<number>;
  addOrEditQuestion: (q: Omit<Question, 'id'>, existingId?: number) => Promise<void>;
}

const AppContext = createContext<AppContextType | null>(null);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Navigation
  const [screenStack, setScreenStack] = useState<ScreenType[]>(['TrackSelect']);
  const currentScreen = screenStack[screenStack.length - 1] || 'TrackSelect';

  const [practiceArgs, setPracticeArgs] = useState<PracticeArgs | null>(null);
  const [activeRecordId, setActiveRecordId] = useState<number | null>(null);

  // Track
  const [currentTrack, setCurrentTrack] = useState<ReviewerTrack>('OIC-NW');

  // Question Data
  const [allQuestions, setAllQuestions] = useState<Question[]>([]);
  const [favorites, setFavorites] = useState<Question[]>([]);
  const [flagged, setFlagged] = useState<Question[]>([]);
  const [incorrect, setIncorrect] = useState<Question[]>([]);
  const [competencyStats, setCompetencyStats] = useState<CompetencyCount[]>([]);

  // Session logs
  const [sessionLogs, setSessionLogs] = useState<SavedSessionLog[]>([]);
  const latestOngoingSession = sessionLogs[0] || null;
  const [ongoingPrompt, setOngoingPrompt] = useState<OngoingSessionPromptState | null>(null);

  // Active Exam
  const [activeExam, setActiveExam] = useState<ActiveExamState | null>(null);
  const examTimerRef = useRef<number | null>(null);

  // Stopwatch
  const [isStopwatchRunning, setIsStopwatchRunning] = useState(false);
  const [stopwatchSeconds, setStopwatchSeconds] = useState(0);
  const [totalStudySeconds, setTotalStudySeconds] = useState(0);
  const [todayGoal, setTodayGoal] = useState<DailyGoal | null>(null);
  const stopwatchRef = useRef<number | null>(null);

  // Admin
  const [isProcessing, setIsProcessing] = useState(false);
  const [importPreview, setImportPreview] = useState<ImportValidationResult | null>(null);
  const [adminMessage, setAdminMessage] = useState<string | null>(null);

  // Refresh question state
  const refreshQuestions = useCallback(async () => {
    try {
      const all = await db.getAllQuestions(currentTrack);
      setAllQuestions(all);
      setFavorites(all.filter((q) => q.isFavorite));
      setFlagged(all.filter((q) => q.isFlagged));
      setIncorrect(all.filter((q) => q.timesIncorrect > 0));

      const stats = await db.getCompetencyStats(currentTrack);
      setCompetencyStats(stats);

      const logs = await db.getSessionLogsByTrack(currentTrack);
      setSessionLogs(logs);

      const totalSec = await db.getTotalStudySeconds(currentTrack);
      setTotalStudySeconds(totalSec);

      const goal = await db.getTodayGoal();
      setTodayGoal(goal);
    } catch (e) {
      console.error('Error refreshing questions:', e);
    }
  }, [currentTrack]);

  // Initial seed check on mount
  useEffect(() => {
    (async () => {
      setIsProcessing(true);
      try {
        await db.seedInitialDataIfNeeded();
        await refreshQuestions();
      } catch (e) {
        console.error('Initial seeding error:', e);
      } finally {
        setIsProcessing(false);
      }
    })();
  }, [refreshQuestions]);

  // Navigation handlers
  const navigateTo = useCallback((screen: ScreenType) => {
    setScreenStack((prev) => [...prev, screen]);
  }, []);

  const navigateBack = useCallback((): boolean => {
    if (screenStack.length > 1) {
      setScreenStack((prev) => prev.slice(0, prev.length - 1));
      return true;
    }
    return false;
  }, [screenStack.length]);

  const popToHome = useCallback(() => {
    setScreenStack(['TrackSelect', 'Home']);
  }, []);

  const selectTrack = useCallback(
    (track: ReviewerTrack) => {
      setCurrentTrack(track);
      navigateTo('Home');
    },
    [navigateTo]
  );

  // Question actions
  const toggleFavorite = async (q: Question) => {
    await db.toggleFavorite(q.id);
    await refreshQuestions();
  };

  const toggleFlag = async (q: Question) => {
    await db.toggleFlag(q.id);
    await refreshQuestions();
  };

  const recordPracticeAnswer = async (q: Question, selectedIndex: number) => {
    await db.recordAnswer(q.id, selectedIndex);
    await refreshQuestions();
  };

  const removeFromIncorrect = async (questionId: number) => {
    await db.removeFromIncorrect(questionId);
    await refreshQuestions();
  };

  const reportQuestion = async (questionId: number, reason: string, details: string) => {
    await db.reportQuestion(questionId, currentTrack, reason, details);
    setAdminMessage('Question report submitted successfully. Thank you!');
  };

  // Stopwatch timer
  useEffect(() => {
    if (isStopwatchRunning) {
      stopwatchRef.current = window.setInterval(() => {
        setStopwatchSeconds((s) => s + 1);
      }, 1000);
    } else {
      if (stopwatchRef.current) clearInterval(stopwatchRef.current);
    }
    return () => {
      if (stopwatchRef.current) clearInterval(stopwatchRef.current);
    };
  }, [isStopwatchRunning]);

  const startStopwatch = () => setIsStopwatchRunning(true);
  const pauseStopwatch = () => setIsStopwatchRunning(false);
  const stopAndSaveStopwatch = async () => {
    setIsStopwatchRunning(false);
    if (stopwatchSeconds > 0) {
      await db.recordStudySession(currentTrack, stopwatchSeconds);
      setStopwatchSeconds(0);
      await refreshQuestions();
    }
  };

  const refreshTodayGoal = async () => {
    const goal = await db.getTodayGoal();
    setTodayGoal(goal);
  };

  const updateGoal = async (updates: Partial<DailyGoal>) => {
    const updated = await db.updateDailyGoal(updates);
    setTodayGoal(updated);
  };

  // Exam timer logic
  useEffect(() => {
    if (activeExam && !activeExam.isSubmitted && activeExam.timeRemainingSeconds > 0) {
      examTimerRef.current = window.setInterval(() => {
        setActiveExam((prev) => {
          if (!prev || prev.isSubmitted) return prev;
          const nextRemaining = Math.max(0, prev.timeRemainingSeconds - 1);
          if (nextRemaining === 0) {
            submitExam();
          }
          return { ...prev, timeRemainingSeconds: nextRemaining };
        });
      }, 1000);
    } else {
      if (examTimerRef.current) clearInterval(examTimerRef.current);
    }
    return () => {
      if (examTimerRef.current) clearInterval(examTimerRef.current);
    };
  }, [activeExam?.isSubmitted, activeExam?.timeRemainingSeconds]);

  // Exam actions
  const startOfficialExam = async () => {
    setIsProcessing(true);
    try {
      const all = await db.getAllQuestions(currentTrack);
      const competencies = currentTrack === 'OIC-NW' ? OIC_NW_COMPETENCIES : GMDSS_COMPETENCIES;

      // Group questions by competency
      const byComp: Record<string, Question[]> = {};
      competencies.forEach((c) => (byComp[c.code] = []));
      all.forEach((q) => {
        if (!byComp[q.competencyCode]) byComp[q.competencyCode] = [];
        byComp[q.competencyCode].push(q);
      });

      // Sample per quota (or all available)
      const selected: Question[] = [];
      for (const comp of competencies) {
        const pool = byComp[comp.code] || [];
        const shuffled = [...pool].sort(() => 0.5 - Math.random());
        const count = Math.min(comp.examQuota, pool.length);
        selected.push(...shuffled.slice(0, count));
      }

      // If bank has fewer items than quota, fallback to random shuffle of all questions
      let finalQuestions = selected;
      if (finalQuestions.length < 20 && all.length >= 20) {
        finalQuestions = [...all].sort(() => 0.5 - Math.random()).slice(0, Math.min(100, all.length));
      }

      // Shuffle order
      finalQuestions = [...finalQuestions].sort(() => 0.5 - Math.random());

      const timeLimitSeconds = currentTrack === 'OIC-NW' ? 180 * 60 : 90 * 60;

      setActiveExam({
        title: `${currentTrack} Official MARINA Simulation`,
        track: currentTrack,
        examType: 'OFFICIAL_SIMULATION',
        questions: finalQuestions,
        userAnswers: {},
        flaggedQuestionIds: [],
        currentIndex: 0,
        timeLimitSeconds,
        timeRemainingSeconds: timeLimitSeconds,
        isSubmitted: false
      });

      navigateTo('ExamRunner');
    } finally {
      setIsProcessing(false);
    }
  };

  const startCustomExam = async (
    selectedCompetencies: string[],
    questionCount: number,
    timeLimitMinutes: number
  ) => {
    setIsProcessing(true);
    try {
      const all = await db.getAllQuestions(currentTrack);
      const filtered = all.filter((q) => selectedCompetencies.includes(q.competencyCode));
      const shuffled = [...filtered].sort(() => 0.5 - Math.random());
      const selected = shuffled.slice(0, Math.min(questionCount, shuffled.length));

      const timeLimitSeconds = timeLimitMinutes * 60;

      setActiveExam({
        title: `${currentTrack} Custom Mock Exam`,
        track: currentTrack,
        examType: 'CUSTOM',
        questions: selected,
        userAnswers: {},
        flaggedQuestionIds: [],
        currentIndex: 0,
        timeLimitSeconds,
        timeRemainingSeconds: timeLimitSeconds,
        isSubmitted: false
      });

      navigateTo('ExamRunner');
    } finally {
      setIsProcessing(false);
    }
  };

  const selectExamAnswer = (questionId: number, optionIndex: number) => {
    setActiveExam((prev) => {
      if (!prev) return null;
      return {
        ...prev,
        userAnswers: { ...prev.userAnswers, [questionId]: optionIndex }
      };
    });
  };

  const toggleExamQuestionFlag = (questionId: number) => {
    setActiveExam((prev) => {
      if (!prev) return null;
      const flags = new Set(prev.flaggedQuestionIds);
      if (flags.has(questionId)) flags.delete(questionId);
      else flags.add(questionId);
      return { ...prev, flaggedQuestionIds: Array.from(flags) };
    });
  };

  const setExamCurrentIndex = (index: number) => {
    setActiveExam((prev) => {
      if (!prev) return null;
      if (index >= 0 && index < prev.questions.length) {
        return { ...prev, currentIndex: index };
      }
      return prev;
    });
  };

  const submitExam = async () => {
    if (!activeExam || activeExam.isSubmitted) return;
    setIsProcessing(true);
    try {
      const timeUsed = activeExam.timeLimitSeconds - activeExam.timeRemainingSeconds;
      const { record } = await db.submitExam({
        track: activeExam.track,
        examType: activeExam.examType,
        title: activeExam.title,
        questions: activeExam.questions,
        userAnswers: activeExam.userAnswers,
        timeUsedSeconds: Math.max(1, timeUsed)
      });

      if (activeExam.savedSessionLogId) {
        await db.deleteSessionLog(activeExam.savedSessionLogId);
      }

      setActiveExam((prev) => (prev ? { ...prev, isSubmitted: true, completedRecordId: record.id } : null));
      setActiveRecordId(record.id);
      navigateTo('ExamResult');
      await refreshQuestions();
    } finally {
      setIsProcessing(false);
    }
  };

  const saveExamSession = async () => {
    if (!activeExam || activeExam.isSubmitted || activeExam.questions.length === 0) return;
    const answeredCount = Object.keys(activeExam.userAnswers).length;
    const payload = {
      questionIds: activeExam.questions.map((q) => q.id),
      userAnswers: activeExam.userAnswers,
      flaggedQuestionIds: activeExam.flaggedQuestionIds,
      timeLimitSeconds: activeExam.timeLimitSeconds,
      timeRemainingSeconds: activeExam.timeRemainingSeconds,
      examType: activeExam.examType
    };

    await db.saveSessionLog({
      id: activeExam.savedSessionLogId || undefined,
      sessionType: 'EXAM',
      track: activeExam.track,
      title: activeExam.title,
      subtitle: `${answeredCount} / ${activeExam.questions.length} answered • ${Math.round(
        activeExam.timeRemainingSeconds / 60
      )}m left`,
      currentIndex: activeExam.currentIndex,
      totalQuestions: activeExam.questions.length,
      answeredCount,
      lastActiveTimestamp: Date.now(),
      payloadJson: JSON.stringify(payload)
    });

    setActiveExam(null);
    await refreshQuestions();
  };

  // Practice session saving
  const savePracticeSession = async (params: {
    title: string;
    filterMode: PracticeFilterMode;
    studyMode: StudyMode;
    competencyCode: string | null;
    partNumber: number | null;
    itemLimit: number | null;
    isRandomized: boolean;
    shuffleOptions: boolean;
    timeLimitSecondsPerItem: number | null;
    randomSeed: number;
    questions: Question[];
    currentIndex: number;
    answers: Record<number, number>;
    results: Record<number, boolean>;
    existingLogId?: number | null;
  }) => {
    if (params.questions.length === 0) return;

    const payload = {
      questionIds: params.questions.map((q) => q.id),
      answers: params.answers,
      results: params.results,
      filterMode: params.filterMode,
      studyMode: params.studyMode,
      competencyCode: params.competencyCode,
      partNumber: params.partNumber,
      itemLimit: params.itemLimit,
      isRandomized: params.isRandomized,
      shuffleOptions: params.shuffleOptions,
      timeLimitSecondsPerItem: params.timeLimitSecondsPerItem,
      randomSeed: params.randomSeed
    };

    const answeredCount = Object.keys(params.answers).length;

    await db.saveSessionLog({
      id: params.existingLogId || undefined,
      sessionType: 'PRACTICE',
      track: currentTrack,
      title: params.title,
      subtitle: `Question ${params.currentIndex + 1} of ${params.questions.length} • ${answeredCount} answered`,
      currentIndex: params.currentIndex,
      totalQuestions: params.questions.length,
      answeredCount,
      lastActiveTimestamp: Date.now(),
      payloadJson: JSON.stringify(payload)
    });

    await refreshQuestions();
  };

  // Resuming sessions
  const resumeSession = async (log: SavedSessionLog) => {
    if (log.sessionType === 'PRACTICE') {
      const payload = JSON.parse(log.payloadJson);
      const qIds: number[] = payload.questionIds || [];
      const questions = await db.getQuestionsByIds(qIds);
      if (questions.length > 0) {
        setPracticeArgs({
          title: log.title,
          filterMode: payload.filterMode || 'ALL',
          competencyCode: payload.competencyCode || null,
          partNumber: payload.partNumber || null,
          customQuestionIds: qIds,
          itemLimit: payload.itemLimit || null,
          isRandomized: !!payload.isRandomized,
          shuffleOptions: !!payload.shuffleOptions,
          studyMode: payload.studyMode || 'TUTOR',
          timeLimitSecondsPerItem: payload.timeLimitSecondsPerItem || null,
          resumeSessionLogId: log.id,
          initialIndex: log.currentIndex || 0,
          initialAnswers: payload.answers || {},
          initialResults: payload.results || {},
          randomSeed: payload.randomSeed || 1
        });
        navigateTo('Practice');
      }
    } else if (log.sessionType === 'EXAM') {
      const payload = JSON.parse(log.payloadJson);
      const qIds: number[] = payload.questionIds || [];
      const questions = await db.getQuestionsByIds(qIds);
      if (questions.length > 0) {
        setActiveExam({
          title: log.title,
          track: log.track,
          examType: payload.examType || 'OFFICIAL_SIMULATION',
          questions,
          userAnswers: payload.userAnswers || {},
          flaggedQuestionIds: payload.flaggedQuestionIds || [],
          currentIndex: log.currentIndex || 0,
          timeLimitSeconds: payload.timeLimitSeconds || 0,
          timeRemainingSeconds: payload.timeRemainingSeconds || 0,
          isSubmitted: false,
          savedSessionLogId: log.id
        });
        navigateTo('ExamRunner');
      }
    }
  };

  const deleteSessionLog = async (id: number) => {
    await db.deleteSessionLog(id);
    await refreshQuestions();
  };

  const clearAllSessionLogs = async (trackOnly = false) => {
    await db.clearSessionLogs(trackOnly ? currentTrack : undefined);
    await refreshQuestions();
  };

  const dismissOngoingSessionPrompt = () => setOngoingPrompt(null);

  const requestStartPractice = async (args: PracticeArgs, onStartNew?: () => void) => {
    const existing = await db.getLatestSessionLog(currentTrack);
    if (existing) {
      setOngoingPrompt({
        existingSession: existing,
        newSessionTitle: args.title,
        onStartNew: async () => {
          setOngoingPrompt(null);
          await db.deleteSessionLog(existing.id);
          if (onStartNew) onStartNew();
          else {
            setPracticeArgs(args);
            navigateTo('Practice');
          }
        }
      });
    } else {
      if (onStartNew) onStartNew();
      else {
        setPracticeArgs(args);
        navigateTo('Practice');
      }
    }
  };

  const requestStartOfficialExam = async () => {
    const existing = await db.getLatestSessionLog(currentTrack);
    if (existing) {
      setOngoingPrompt({
        existingSession: existing,
        newSessionTitle: `${currentTrack} Official MARINA Simulation`,
        onStartNew: async () => {
          setOngoingPrompt(null);
          await db.deleteSessionLog(existing.id);
          await startOfficialExam();
        }
      });
    } else {
      await startOfficialExam();
    }
  };

  const requestStartCustomExam = async (
    selectedCompetencies: string[],
    questionCount: number,
    timeLimitMinutes: number
  ) => {
    const existing = await db.getLatestSessionLog(currentTrack);
    if (existing) {
      setOngoingPrompt({
        existingSession: existing,
        newSessionTitle: `${currentTrack} Custom Mock Exam (${questionCount} items)`,
        onStartNew: async () => {
          setOngoingPrompt(null);
          await db.deleteSessionLog(existing.id);
          await startCustomExam(selectedCompetencies, questionCount, timeLimitMinutes);
        }
      });
    } else {
      await startCustomExam(selectedCompetencies, questionCount, timeLimitMinutes);
    }
  };

  // Admin operations
  const confirmImport = async () => {
    if (!importPreview) return;
    setIsProcessing(true);
    try {
      await db.insertBatch(importPreview.validatedQuestions);
      setAdminMessage(`Successfully imported ${importPreview.validCount} questions for ${importPreview.reviewer}!`);
      setImportPreview(null);
      await refreshQuestions();
    } catch (e: any) {
      setAdminMessage(`Failed to save questions: ${e.message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const cancelImportPreview = () => setImportPreview(null);

  const resetReviewerDatabase = async (track: ReviewerTrack) => {
    setIsProcessing(true);
    try {
      await db.clearReviewer(track);
      setAdminMessage(`${track} question bank cleared.`);
      await refreshQuestions();
    } catch (e: any) {
      setAdminMessage(`Reset failed: ${e.message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const reseedDatabase = async () => {
    setIsProcessing(true);
    try {
      const count = await db.seedInitialDataIfNeeded();
      setAdminMessage(`Seed questions verified/restored (${count} total questions).`);
      await refreshQuestions();
    } finally {
      setIsProcessing(false);
    }
  };

  const scanAndImportAssets = async () => {
    setIsProcessing(true);
    try {
      let totalLoaded = 0;
      const csvFiles = ['/data/gmdss_reviewer.csv', '/data/tmp_chunk.csv'];
      for (const url of csvFiles) {
        try {
          const res = await fetch(url);
          if (res.ok) {
            const csvText = await res.text();
            const parsed = processCsvContent(csvText, 'GMDSS');
            if (parsed.validatedQuestions.length > 0) {
              await db.insertBatch(parsed.validatedQuestions);
              totalLoaded += parsed.validCount;
            }
          }
        } catch (err) {
          console.warn(`Could not load CSV from ${url}:`, err);
        }
      }

      if (totalLoaded > 0) {
        setAdminMessage(`Successfully imported ${totalLoaded} questions from bundled assets into Hyperion!`);
      } else {
        setAdminMessage('No new questions loaded from bundled assets.');
      }
      await refreshQuestions();
    } catch (e: any) {
      setAdminMessage(`Asset scan error: ${e.message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const dismissAdminMessage = () => setAdminMessage(null);

  const exportBackupJson = async () => {
    return await db.exportBackupJson();
  };

  const restoreBackupJson = async (json: string) => {
    const count = await db.restoreBackupJson(json);
    await refreshQuestions();
    return count;
  };

  const addOrEditQuestion = async (q: Omit<Question, 'id'>, existingId?: number) => {
    if (existingId) {
      await db.updateQuestion({ ...q, id: existingId } as Question);
    } else {
      await db.addQuestion(q);
    }
    await refreshQuestions();
  };

  return (
    <AppContext.Provider
      value={{
        currentScreen,
        navigateTo,
        navigateBack,
        popToHome,
        practiceArgs,
        setPracticeArgs,
        activeRecordId,
        setActiveRecordId,
        currentTrack,
        selectTrack,
        allQuestions,
        favorites,
        flagged,
        incorrect,
        competencyStats,
        refreshQuestions,
        toggleFavorite,
        toggleFlag,
        recordPracticeAnswer,
        removeFromIncorrect,
        reportQuestion,
        sessionLogs,
        latestOngoingSession,
        ongoingPrompt,
        dismissOngoingSessionPrompt,
        resumeSession,
        deleteSessionLog,
        clearAllSessionLogs,
        requestStartPractice,
        requestStartOfficialExam,
        requestStartCustomExam,
        savePracticeSession,
        activeExam,
        startOfficialExam,
        startCustomExam,
        selectExamAnswer,
        toggleExamQuestionFlag,
        setExamCurrentIndex,
        submitExam,
        saveExamSession,
        isStopwatchRunning,
        stopwatchSeconds,
        startStopwatch,
        pauseStopwatch,
        stopAndSaveStopwatch,
        totalStudySeconds,
        todayGoal,
        refreshTodayGoal,
        updateGoal,
        isProcessing,
        importPreview,
        setImportPreview,
        adminMessage,
        setAdminMessage,
        dismissAdminMessage,
        confirmImport,
        cancelImportPreview,
        resetReviewerDatabase,
        reseedDatabase,
        scanAndImportAssets,
        exportBackupJson,
        restoreBackupJson,
        addOrEditQuestion
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export function useApp(): AppContextType {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
}
