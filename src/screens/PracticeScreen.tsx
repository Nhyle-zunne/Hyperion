import React, { useState, useEffect, useMemo, useRef, useCallback } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { Question, StudyMode, PracticeFilterMode } from '../types';
import {
  Star,
  Flag,
  ArrowLeft,
  ArrowRight,
  RotateCcw,
  CheckCircle2,
  XCircle,
  HelpCircle,
  LayoutGrid,
  Zap,
  BookOpen,
  CheckSquare,
  Layers,
  Shuffle,
  AlertTriangle,
  X
} from 'lucide-react';

export const PracticeScreen: React.FC = () => {
  const {
    allQuestions,
    practiceArgs,
    navigateBack,
    toggleFavorite,
    toggleFlag,
    recordPracticeAnswer,
    reportQuestion,
    savePracticeSession
  } = useApp();

  const title = practiceArgs?.title || 'Practice Review';
  const competencyCode = practiceArgs?.competencyCode || null;
  const partNumber = practiceArgs?.partNumber || null;

  const [activeFilter, setActiveFilter] = useState<PracticeFilterMode>(practiceArgs?.filterMode || 'ALL');
  const [isRandomized, setIsRandomized] = useState(practiceArgs?.isRandomized ?? false);
  const [shuffleOptions, setShuffleOptions] = useState(practiceArgs?.shuffleOptions ?? false);
  const [currentStudyMode, setCurrentStudyMode] = useState<StudyMode>(practiceArgs?.studyMode || 'TUTOR');
  const [itemLimit, setItemLimit] = useState<number | null>(practiceArgs?.itemLimit || null);
  const [randomSeed, setRandomSeed] = useState(practiceArgs?.randomSeed || 1);

  // Filter and order questions
  const filteredQuestions = useMemo(() => {
    let list = [...allQuestions];

    if (practiceArgs?.customQuestionIds && practiceArgs.customQuestionIds.length > 0) {
      const idMap = new Map(allQuestions.map((q) => [q.id, q]));
      list = practiceArgs.customQuestionIds.map((id) => idMap.get(id)).filter(Boolean) as Question[];
    } else {
      if (competencyCode) {
        list = list.filter((q) => q.competencyCode.toUpperCase() === competencyCode.toUpperCase());
      }
      if (partNumber !== null && partNumber !== undefined) {
        list = list.filter((q) => q.partNumber === partNumber);
      }

      switch (activeFilter) {
        case 'ALL':
          break;
        case 'UNANSWERED':
          list = list.filter((q) => q.timesAttempted === 0);
          break;
        case 'INCORRECT':
          list = list.filter((q) => q.timesIncorrect > 0);
          break;
        case 'FAVORITES':
          list = list.filter((q) => q.isFavorite);
          break;
        case 'FLAGGED':
          list = list.filter((q) => q.isFlagged);
          break;
        case 'NEEDS_REVIEW':
          list = list.filter((q) => q.masteryStatus === 'NEEDS_REVIEW' || q.timesIncorrect > 0);
          break;
        case 'MASTERED':
          list = list.filter((q) => q.masteryStatus === 'MASTERED');
          break;
        case 'SMART_REVIEW':
          list.sort((a, b) => b.timesIncorrect - a.timesIncorrect || (a.timesAttempted === 0 ? -1 : 1));
          break;
      }
    }

    if (isRandomized) {
      let seed = randomSeed;
      const pseudoRandom = () => {
        const x = Math.sin(seed++) * 10000;
        return x - Math.floor(x);
      };
      list = [...list].sort(() => 0.5 - pseudoRandom());
    }

    if (itemLimit && itemLimit > 0) {
      list = list.slice(0, itemLimit);
    }

    return list;
  }, [allQuestions, practiceArgs, competencyCode, partNumber, activeFilter, isRandomized, randomSeed, itemLimit]);

  // Session state
  const [currentIndex, setCurrentIndex] = useState(practiceArgs?.initialIndex || 0);
  const [userAnswers, setUserAnswers] = useState<Record<number, number>>(practiceArgs?.initialAnswers || {});
  const [answerResults, setAnswerResults] = useState<Record<number, boolean>>(practiceArgs?.initialResults || {});

  // Flashcard flip state
  const [isFlipped, setIsFlipped] = useState(false);

  // Speed run timer
  const [speedSecondsLeft, setSpeedSecondsLeft] = useState(practiceArgs?.timeLimitSecondsPerItem || 30);
  const speedTimerRef = useRef<number | null>(null);

  // Dialogs
  const [showGridModal, setShowGridModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportReason, setReportReason] = useState('Incorrect Answer Key');
  const [reportDetails, setReportDetails] = useState('');
  const [showDrillScorecard, setShowDrillScorecard] = useState(false);

  const currentQuestion: Question | undefined = filteredQuestions[currentIndex];

  // Options mapping (support shuffle)
  const options = useMemo(() => {
    if (!currentQuestion) return [];
    const raw = [
      { text: currentQuestion.optionA, originalIdx: 0, letter: 'A' },
      { text: currentQuestion.optionB, originalIdx: 1, letter: 'B' },
      currentQuestion.optionC ? { text: currentQuestion.optionC, originalIdx: 2, letter: 'C' } : null,
      currentQuestion.optionD ? { text: currentQuestion.optionD, originalIdx: 3, letter: 'D' } : null,
      currentQuestion.optionE ? { text: currentQuestion.optionE, originalIdx: 4, letter: 'E' } : null,
      currentQuestion.optionF ? { text: currentQuestion.optionF, originalIdx: 5, letter: 'F' } : null
    ].filter(Boolean) as { text: string; originalIdx: number; letter: string }[];

    if (!shuffleOptions) return raw;

    // Stable shuffle based on question id
    const qSeed = currentQuestion.id * 17;
    return [...raw].sort((a, b) => {
      const ha = (a.text.length * qSeed) % 100;
      const hb = (b.text.length * qSeed) % 100;
      return ha - hb;
    });
  }, [currentQuestion, shuffleOptions]);

  // Speed Run Timer logic
  useEffect(() => {
    if (currentStudyMode === 'SPEED_RUN' && currentQuestion) {
      const timeLimit = practiceArgs?.timeLimitSecondsPerItem || 30;
      setSpeedSecondsLeft(timeLimit);

      if (speedTimerRef.current) clearInterval(speedTimerRef.current);
      speedTimerRef.current = window.setInterval(() => {
        setSpeedSecondsLeft((prev) => {
          if (prev <= 1) {
            // Time out!
            handleOptionSelect(-1); // timeout mark
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else {
      if (speedTimerRef.current) clearInterval(speedTimerRef.current);
    }

    return () => {
      if (speedTimerRef.current) clearInterval(speedTimerRef.current);
    };
  }, [currentIndex, currentStudyMode, currentQuestion?.id]);

  // Save session state on unmount / navigate
  const handleBack = useCallback(() => {
    if (filteredQuestions.length > 0) {
      savePracticeSession({
        title,
        filterMode: activeFilter,
        studyMode: currentStudyMode,
        competencyCode,
        partNumber,
        itemLimit,
        isRandomized,
        shuffleOptions,
        timeLimitSecondsPerItem: practiceArgs?.timeLimitSecondsPerItem || null,
        randomSeed,
        questions: filteredQuestions,
        currentIndex,
        answers: userAnswers,
        results: answerResults,
        existingLogId: practiceArgs?.resumeSessionLogId
      });
    }
    navigateBack();
  }, [
    filteredQuestions,
    savePracticeSession,
    title,
    activeFilter,
    currentStudyMode,
    competencyCode,
    partNumber,
    itemLimit,
    isRandomized,
    shuffleOptions,
    practiceArgs?.timeLimitSecondsPerItem,
    practiceArgs?.resumeSessionLogId,
    randomSeed,
    currentIndex,
    userAnswers,
    answerResults,
    navigateBack
  ]);

  const handleOptionSelect = (originalIdx: number) => {
    if (!currentQuestion) return;

    // If already answered in Tutor mode, don't re-answer
    if (currentStudyMode === 'TUTOR' && userAnswers[currentQuestion.id] !== undefined) {
      return;
    }

    const isCorrect = originalIdx === currentQuestion.correctAnswerIndex;
    setUserAnswers((prev) => ({ ...prev, [currentQuestion.id]: originalIdx }));
    setAnswerResults((prev) => ({ ...prev, [currentQuestion.id]: isCorrect }));

    recordPracticeAnswer(currentQuestion, originalIdx);

    if (currentStudyMode === 'SPEED_RUN') {
      if (speedTimerRef.current) clearInterval(speedTimerRef.current);
      setTimeout(() => {
        if (currentIndex < filteredQuestions.length - 1) {
          setCurrentIndex((i) => i + 1);
        }
      }, 700);
    }
  };

  const handleNext = () => {
    if (currentIndex < filteredQuestions.length - 1) {
      setCurrentIndex((i) => i + 1);
      setIsFlipped(false);
    } else {
      if (currentStudyMode === 'DRILL_EXAM') {
        setShowDrillScorecard(true);
      }
    }
  };

  const handlePrev = () => {
    if (currentIndex > 0) {
      setCurrentIndex((i) => i - 1);
      setIsFlipped(false);
    }
  };

  const handleReportSubmit = () => {
    if (!currentQuestion) return;
    reportQuestion(currentQuestion.id, reportReason, reportDetails);
    setShowReportModal(false);
    setReportDetails('');
  };

  if (!currentQuestion) {
    return (
      <div className="min-h-screen bg-[#070F1B] text-white flex flex-col">
        <HyperionTopBar title={title} onBack={handleBack} />
        <div className="flex-1 flex flex-col items-center justify-center p-6 text-center">
          <HelpCircle className="w-12 h-12 text-slate-500 mb-3" />
          <h2 className="text-lg font-bold">No Questions Found</h2>
          <p className="text-xs text-slate-400 mt-1 max-w-sm">
            No questions match your current filter settings for {title}. Try selecting "All Items" or adjusting your criteria.
          </p>
          <button
            onClick={() => setActiveFilter('ALL')}
            className="mt-4 px-4 py-2 bg-[#D4AF37] text-[#0B192C] font-bold text-xs rounded-xl shadow cursor-pointer"
          >
            Show All Questions
          </button>
        </div>
      </div>
    );
  }

  const selectedAnswer = userAnswers[currentQuestion.id];
  const isAnswered = selectedAnswer !== undefined;
  const isCurrentCorrect = answerResults[currentQuestion.id];

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-20">
      {/* Top Bar */}
      <HyperionTopBar
        title={title}
        subtitle={`Item ${currentIndex + 1} of ${filteredQuestions.length}`}
        onBack={handleBack}
        actions={
          <div className="flex items-center gap-1">
            <button
              onClick={() => toggleFavorite(currentQuestion)}
              className="p-1.5 rounded-lg hover:bg-white/10 transition-colors cursor-pointer text-slate-300"
              title="Toggle Favorite"
            >
              <Star
                className={`w-5 h-5 ${
                  currentQuestion.isFavorite ? 'text-[#D4AF37] fill-[#D4AF37]' : 'text-slate-400'
                }`}
              />
            </button>
            <button
              onClick={() => toggleFlag(currentQuestion)}
              className="p-1.5 rounded-lg hover:bg-white/10 transition-colors cursor-pointer text-slate-300"
              title="Toggle Flag"
            >
              <Flag
                className={`w-5 h-5 ${
                  currentQuestion.isFlagged ? 'text-amber-400 fill-amber-400' : 'text-slate-400'
                }`}
              />
            </button>
            <button
              onClick={() => setShowGridModal(true)}
              className="p-1.5 rounded-lg hover:bg-white/10 transition-colors cursor-pointer text-slate-300"
              title="Jump to Question"
            >
              <LayoutGrid className="w-5 h-5 text-[#2E86AB]" />
            </button>
          </div>
        }
      />

      {/* Mode & Control Sub-bar */}
      <div className="bg-[#0B192C] border-b border-[#1E3E62] px-4 py-2">
        <div className="max-w-4xl mx-auto flex items-center justify-between gap-2 overflow-x-auto">
          {/* Mode Pill Dropdown / Selector */}
          <div className="flex items-center gap-1 bg-[#0F3460] p-1 rounded-xl border border-white/10 text-xs shrink-0">
            <button
              onClick={() => setCurrentStudyMode('TUTOR')}
              className={`px-2.5 py-1 rounded-lg font-bold flex items-center gap-1.5 transition-colors cursor-pointer ${
                currentStudyMode === 'TUTOR' ? 'bg-[#D4AF37] text-[#0B192C]' : 'text-slate-300 hover:text-white'
              }`}
            >
              <BookOpen className="w-3.5 h-3.5" />
              <span>Tutor</span>
            </button>
            <button
              onClick={() => setCurrentStudyMode('DRILL_EXAM')}
              className={`px-2.5 py-1 rounded-lg font-bold flex items-center gap-1.5 transition-colors cursor-pointer ${
                currentStudyMode === 'DRILL_EXAM' ? 'bg-[#D4AF37] text-[#0B192C]' : 'text-slate-300 hover:text-white'
              }`}
            >
              <CheckSquare className="w-3.5 h-3.5" />
              <span>Drill</span>
            </button>
            <button
              onClick={() => setCurrentStudyMode('FLASHCARD')}
              className={`px-2.5 py-1 rounded-lg font-bold flex items-center gap-1.5 transition-colors cursor-pointer ${
                currentStudyMode === 'FLASHCARD' ? 'bg-[#D4AF37] text-[#0B192C]' : 'text-slate-300 hover:text-white'
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              <span>Flashcard</span>
            </button>
            <button
              onClick={() => setCurrentStudyMode('SPEED_RUN')}
              className={`px-2.5 py-1 rounded-lg font-bold flex items-center gap-1.5 transition-colors cursor-pointer ${
                currentStudyMode === 'SPEED_RUN' ? 'bg-[#D4AF37] text-[#0B192C]' : 'text-slate-300 hover:text-white'
              }`}
            >
              <Zap className="w-3.5 h-3.5" />
              <span>Speed Run</span>
            </button>
          </div>

          {/* Speed Run Countdown Badge */}
          {currentStudyMode === 'SPEED_RUN' && (
            <div
              className={`px-3 py-1 rounded-xl text-xs font-black tracking-wider flex items-center gap-1.5 ${
                speedSecondsLeft <= 5
                  ? 'bg-red-950 text-red-400 border border-red-500 animate-pulse'
                  : 'bg-[#1E3E62] text-amber-300'
              }`}
            >
              <Zap className="w-3.5 h-3.5" />
              <span>{speedSecondsLeft}s</span>
            </div>
          )}

          {/* Shuffle indicator */}
          <div className="flex items-center gap-2 text-xs text-slate-400 shrink-0">
            <button
              onClick={() => setShuffleOptions((v) => !v)}
              className={`p-1.5 rounded-lg border flex items-center gap-1 cursor-pointer transition-colors ${
                shuffleOptions
                  ? 'bg-[#1E3E62] border-emerald-500 text-emerald-300'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
              title="Shuffle Choice Letters A-D"
            >
              <Shuffle className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Shuffle Options</span>
            </button>
          </div>
        </div>
      </div>

      {/* Progress Track Bar */}
      <div className="w-full bg-[#070F1B] h-1.5">
        <div
          className="bg-gradient-to-r from-[#2E86AB] to-[#D4AF37] h-full transition-all duration-300"
          style={{ width: `${((currentIndex + 1) / filteredQuestions.length) * 100}%` }}
        />
      </div>

      {/* Main Content Area */}
      <div className="max-w-3xl mx-auto w-full px-4 sm:px-6 pt-4 flex-1 flex flex-col justify-between">
        {/* =========================================
            FLASHCARD MODE VIEW
           ========================================= */}
        {currentStudyMode === 'FLASHCARD' ? (
          <div className="flex-1 flex flex-col justify-center my-4">
            <div
              onClick={() => setIsFlipped((v) => !v)}
              className={`min-h-[280px] p-6 rounded-3xl border transition-all duration-300 cursor-pointer shadow-2xl flex flex-col justify-between ${
                isFlipped
                  ? 'bg-[#0F3460] border-[#D4AF37]'
                  : 'bg-[#0B192C] border-[#1E3E62] hover:border-[#2E86AB]'
              }`}
            >
              <div>
                <div className="flex items-center justify-between text-xs text-slate-400 mb-3">
                  <div className="flex items-center gap-2">
                    <span className="font-extrabold text-[#D4AF37]">
                      {currentQuestion.competencyCode}
                    </span>
                    {currentQuestion.partNumber && (
                      <span className="text-slate-400">Part {currentQuestion.partNumber}</span>
                    )}
                  </div>
                  <span className="text-[11px] uppercase font-bold text-slate-400">
                    {isFlipped ? 'Answer & Explanation (Tap to Flip)' : 'Question (Tap to Flip)'}
                  </span>
                </div>

                {!isFlipped ? (
                  <h2 className="text-base sm:text-lg font-bold text-white leading-relaxed">
                    {currentQuestion.questionText}
                  </h2>
                ) : (
                  <div className="space-y-4">
                    <div>
                      <div className="text-xs font-bold text-emerald-400 uppercase tracking-wider mb-1">
                        Correct Answer: Option {currentQuestion.correctAnswerLetter}
                      </div>
                      <div className="text-sm font-bold text-white bg-[#0B192C]/80 p-3 rounded-xl border border-emerald-500/30">
                        {currentQuestion.correctAnswerIndex === 0 && currentQuestion.optionA}
                        {currentQuestion.correctAnswerIndex === 1 && currentQuestion.optionB}
                        {currentQuestion.correctAnswerIndex === 2 && currentQuestion.optionC}
                        {currentQuestion.correctAnswerIndex === 3 && currentQuestion.optionD}
                        {currentQuestion.correctAnswerIndex === 4 && currentQuestion.optionE}
                        {currentQuestion.correctAnswerIndex === 5 && currentQuestion.optionF}
                      </div>
                    </div>

                    {currentQuestion.explanation && (
                      <div className="text-xs text-slate-300 leading-relaxed bg-[#070F1B]/50 p-3 rounded-xl border border-white/5">
                        <strong className="text-[#D4AF37] block mb-1">Maritime Rationale:</strong>
                        {currentQuestion.explanation}
                      </div>
                    )}
                  </div>
                )}
              </div>

              <div className="text-center text-xs text-slate-400 pt-4 border-t border-white/5">
                Tap card to {isFlipped ? 'view question' : 'reveal correct answer'}
              </div>
            </div>

            {/* Active Recall Buttons */}
            {isFlipped && (
              <div className="flex gap-3 mt-4">
                <button
                  onClick={() => {
                    handleOptionSelect(-1); // marked needs review
                    handleNext();
                  }}
                  className="flex-1 py-3 bg-[#0F3460] hover:bg-red-950/60 border border-red-500/40 text-red-300 font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                >
                  <RotateCcw className="w-4 h-4" />
                  <span>Needs Review</span>
                </button>
                <button
                  onClick={() => {
                    handleOptionSelect(currentQuestion.correctAnswerIndex); // marked mastered
                    handleNext();
                  }}
                  className="flex-1 py-3 bg-emerald-900/50 hover:bg-emerald-800/60 border border-emerald-500 text-emerald-300 font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer shadow"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  <span>I Know This</span>
                </button>
              </div>
            )}
          </div>
        ) : (
          /* =========================================
             TUTOR / DRILL / SPEED-RUN VIEW
             ========================================= */
          <div className="space-y-4 my-2">
            {/* Question Card */}
            <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 sm:p-6 shadow-xl">
              <div className="flex items-center justify-between gap-2 text-xs mb-3">
                <div className="flex items-center gap-2">
                  <span className="font-extrabold text-[#D4AF37] bg-[#0B192C] px-2 py-0.5 rounded">
                    {currentQuestion.competencyCode}
                  </span>
                  {currentQuestion.function && (
                    <span className="font-semibold text-[#2E86AB]">
                      {currentQuestion.function}
                    </span>
                  )}
                  {currentQuestion.partNumber && (
                    <span className="text-slate-400">Part {currentQuestion.partNumber}</span>
                  )}
                  {currentQuestion.section && (
                    <span className="text-slate-400 truncate max-w-[120px]">
                      • {currentQuestion.section}
                    </span>
                  )}
                </div>

                <button
                  onClick={() => setShowReportModal(true)}
                  className="text-slate-400 hover:text-amber-400 text-[11px] flex items-center gap-1 transition-colors cursor-pointer"
                  title="Report question error"
                >
                  <AlertTriangle className="w-3.5 h-3.5" />
                  <span>Report</span>
                </button>
              </div>

              <h2 className="text-base sm:text-lg font-bold text-white leading-relaxed">
                {currentQuestion.questionText}
              </h2>
            </div>

            {/* Answer Options */}
            <div className="space-y-2.5">
              {options.map((opt) => {
                const originalIndex = opt.originalIdx;
                const isSelected = selectedAnswer === originalIndex;
                const isCorrectOption = originalIndex === currentQuestion.correctAnswerIndex;

                let optClass = 'bg-[#0B192C] border-[#1E3E62] text-slate-200 hover:border-[#2E86AB]';

                if (currentStudyMode === 'TUTOR' || currentStudyMode === 'SPEED_RUN') {
                  if (isAnswered) {
                    if (isCorrectOption) {
                      optClass = 'bg-emerald-950/70 border-emerald-500 text-emerald-200 font-semibold shadow-md';
                    } else if (isSelected && !isCorrectOption) {
                      optClass = 'bg-red-950/70 border-red-500 text-red-200 font-semibold shadow-md';
                    } else {
                      optClass = 'bg-[#0B192C]/50 border-white/5 text-slate-400 opacity-60';
                    }
                  }
                } else if (currentStudyMode === 'DRILL_EXAM') {
                  if (isSelected) {
                    optClass = 'bg-[#1E3E62] border-[#D4AF37] text-white font-bold ring-1 ring-[#D4AF37] shadow';
                  }
                }

                return (
                  <button
                    key={originalIndex}
                    type="button"
                    onClick={() => handleOptionSelect(originalIndex)}
                    disabled={currentStudyMode === 'TUTOR' && isAnswered}
                    className={`w-full p-4 rounded-xl border text-left transition-all duration-150 flex items-start gap-3 cursor-pointer ${optClass}`}
                  >
                    <span className="w-6 h-6 rounded-lg bg-[#070F1B] border border-white/10 text-xs font-bold flex items-center justify-center shrink-0 text-[#D4AF37]">
                      {opt.letter}
                    </span>
                    <span className="text-xs sm:text-sm leading-snug pt-0.5 flex-1">{opt.text}</span>
                    {isAnswered && (currentStudyMode === 'TUTOR' || currentStudyMode === 'SPEED_RUN') && (
                      <span className="shrink-0 mt-0.5">
                        {isCorrectOption ? (
                          <CheckCircle2 className="w-5 h-5 text-emerald-400" />
                        ) : isSelected ? (
                          <XCircle className="w-5 h-5 text-red-400" />
                        ) : null}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>

            {/* Tutor Mode Explanation Panel (Revealed after answer) */}
            {currentStudyMode === 'TUTOR' && isAnswered && (
              <div className="bg-[#0F3460] border border-[#2E86AB]/50 rounded-2xl p-4 sm:p-5 shadow-lg animate-fade-in space-y-3">
                <div className="flex items-center gap-2">
                  {isCurrentCorrect ? (
                    <div className="flex items-center gap-2 text-emerald-400 font-bold text-sm">
                      <CheckCircle2 className="w-5 h-5" />
                      <span>Correct Answer!</span>
                    </div>
                  ) : (
                    <div className="flex items-center gap-2 text-red-400 font-bold text-sm">
                      <XCircle className="w-5 h-5" />
                      <span>Incorrect — Answer is Option {currentQuestion.correctAnswerLetter}</span>
                    </div>
                  )}
                </div>

                {currentQuestion.explanation && (
                  <div className="text-xs sm:text-sm text-slate-300 leading-relaxed bg-[#0B192C]/80 p-3.5 rounded-xl border border-white/5">
                    <strong className="text-[#D4AF37] block mb-1">Explanation & Marine Reference:</strong>
                    {currentQuestion.explanation}
                  </div>
                )}

                {currentQuestion.source && (
                  <div className="text-[11px] text-slate-400">
                    Source: <span className="text-slate-300">{currentQuestion.source}</span>
                    {currentQuestion.sourceSheet && <span> • {currentQuestion.sourceSheet}</span>}
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* Bottom Navigation Bar */}
        <div className="sticky bottom-0 bg-[#070F1B]/95 backdrop-blur border-t border-[#1E3E62] px-4 py-3 -mx-4 sm:-mx-6 mt-6 flex items-center justify-between gap-3">
          <button
            onClick={handlePrev}
            disabled={currentIndex === 0}
            className="flex-1 py-2.5 px-3 bg-[#0F3460] hover:bg-[#1E3E62] disabled:opacity-40 disabled:cursor-not-allowed text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer border border-white/10"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>PREV</span>
          </button>

          <button
            onClick={() => setShowGridModal(true)}
            className="py-2.5 px-4 bg-[#0B192C] hover:bg-[#1E3E62] text-slate-300 hover:text-white font-medium text-xs rounded-xl border border-white/10 flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
          >
            <LayoutGrid className="w-4 h-4 text-[#D4AF37]" />
            <span className="hidden sm:inline">Grid</span>
          </button>

          <button
            onClick={handleNext}
            className="flex-1 py-2.5 px-3 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer shadow"
          >
            <span>{currentIndex === filteredQuestions.length - 1 ? 'FINISH' : 'NEXT'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Grid Jump Modal */}
      {showGridModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0B192C] border border-[#1E3E62] w-full max-w-lg rounded-2xl p-5 shadow-2xl text-white max-h-[85vh] flex flex-col">
            <div className="flex items-center justify-between pb-3 border-b border-white/10">
              <h3 className="font-bold text-white text-base">Question Navigator</h3>
              <button
                onClick={() => setShowGridModal(false)}
                className="text-slate-400 hover:text-white p-1 rounded-full cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex items-center gap-4 text-xs text-slate-400 py-3 border-b border-white/5">
              <span className="flex items-center gap-1.5">
                <span className="w-3 h-3 rounded-full bg-emerald-500" /> Correct
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-3 h-3 rounded-full bg-red-500" /> Incorrect
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-3 h-3 rounded-full bg-[#1E3E62]" /> Unanswered
              </span>
            </div>

            <div className="overflow-y-auto py-4 flex-1 grid grid-cols-5 sm:grid-cols-8 gap-2 pr-1">
              {filteredQuestions.map((q, idx) => {
                const ans = userAnswers[q.id];
                const res = answerResults[q.id];
                const isCurrent = idx === currentIndex;

                let color = 'bg-[#0F3460] text-slate-300 border-[#1E3E62]';
                if (ans !== undefined) {
                  if (currentStudyMode === 'DRILL_EXAM') {
                    color = 'bg-[#2E86AB] text-white border-[#2E86AB] font-bold';
                  } else {
                    color = res
                      ? 'bg-emerald-600 text-white border-emerald-500 font-bold'
                      : 'bg-red-600 text-white border-red-500 font-bold';
                  }
                }

                return (
                  <button
                    key={q.id}
                    onClick={() => {
                      setCurrentIndex(idx);
                      setShowGridModal(false);
                    }}
                    className={`h-10 rounded-xl border text-xs flex items-center justify-center transition-colors cursor-pointer relative ${color} ${
                      isCurrent ? 'ring-2 ring-[#D4AF37]' : ''
                    }`}
                  >
                    <span>{idx + 1}</span>
                    {q.isFlagged && (
                      <span className="w-2 h-2 rounded-full bg-amber-400 absolute top-1 right-1" />
                    )}
                  </button>
                );
              })}
            </div>

            <div className="pt-3 border-t border-white/10 flex justify-end">
              <button
                onClick={() => setShowGridModal(false)}
                className="px-4 py-2 bg-[#1E3E62] text-xs font-bold rounded-xl cursor-pointer"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Report Question Modal */}
      {showReportModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0F3460] border border-[#2E86AB] w-full max-w-md rounded-2xl p-5 shadow-2xl text-white">
            <div className="flex items-center justify-between pb-3 border-b border-white/10">
              <div className="flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-amber-400" />
                <h3 className="font-bold text-white text-base">Report Question Error</h3>
              </div>
              <button
                onClick={() => setShowReportModal(false)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="py-4 space-y-3">
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Reason for Report
                </label>
                <select
                  value={reportReason}
                  onChange={(e) => setReportReason(e.target.value)}
                  className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl px-3 py-2 text-xs text-white"
                >
                  <option value="Incorrect Answer Key">Incorrect Answer Key</option>
                  <option value="Typo / Grammatical Error">Typo / Grammatical Error</option>
                  <option value="Confusing Question">Confusing Question</option>
                  <option value="Outdated MARINA Regulation">Outdated MARINA Regulation</option>
                  <option value="Other">Other Issue</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Details / Reference Note
                </label>
                <textarea
                  rows={3}
                  value={reportDetails}
                  onChange={(e) => setReportDetails(e.target.value)}
                  placeholder="Provide clarification or correct reference..."
                  className="w-full bg-[#0B192C] border border-[#1E3E62] rounded-xl p-3 text-xs text-white placeholder-slate-500 outline-none"
                />
              </div>
            </div>

            <div className="pt-3 border-t border-white/10 flex justify-end gap-2">
              <button
                onClick={() => setShowReportModal(false)}
                className="px-3 py-2 text-xs text-slate-400 hover:text-white cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handleReportSubmit}
                className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-[#0B192C] font-bold text-xs rounded-xl shadow cursor-pointer"
              >
                Submit Report
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Drill Scorecard Modal */}
      {showDrillScorecard && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0B192C] border border-[#2E86AB] w-full max-w-md rounded-2xl p-6 shadow-2xl text-white text-center">
            <h3 className="text-xl font-black text-white">Drill Session Completed!</h3>
            <p className="text-xs text-slate-400 mt-1">{title}</p>

            {(() => {
              const answered = Object.keys(userAnswers).length;
              const correct = Object.values(answerResults).filter(Boolean).length;
              const total = filteredQuestions.length;
              const pct = total > 0 ? Math.round((correct / total) * 100) : 0;
              const passed = pct >= 70;

              return (
                <div className="my-6 space-y-4">
                  <div
                    className={`inline-block px-4 py-1.5 rounded-full text-sm font-black uppercase tracking-wider ${
                      passed
                        ? 'bg-emerald-900/60 text-emerald-300 border border-emerald-500'
                        : 'bg-red-900/60 text-red-300 border border-red-500'
                    }`}
                  >
                    {passed ? 'PASSED (70% standard)' : 'NEEDS IMPROVEMENT'}
                  </div>

                  <div className="text-4xl font-black text-[#D4AF37]">{pct}%</div>
                  <div className="text-xs text-slate-300">
                    {correct} correct out of {total} total ({answered} answered)
                  </div>
                </div>
              );
            })()}

            <div className="flex flex-col gap-2 pt-2">
              <button
                onClick={() => {
                  setShowDrillScorecard(false);
                  setCurrentStudyMode('TUTOR');
                  setCurrentIndex(0);
                }}
                className="w-full py-2.5 bg-[#D4AF37] text-[#0B192C] font-extrabold text-xs rounded-xl shadow cursor-pointer"
              >
                REVIEW ALL WITH TUTOR EXPLANATIONS
              </button>
              <button
                onClick={() => {
                  setShowDrillScorecard(false);
                  handleBack();
                }}
                className="w-full py-2.5 bg-[#1E3E62] text-white font-semibold text-xs rounded-xl cursor-pointer"
              >
                RETURN TO MENU
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
