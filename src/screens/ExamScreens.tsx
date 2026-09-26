import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { ExamRecord, ExamAnswer, Question } from '../types';
import * as db from '../db/indexedDb';
import { OIC_NW_COMPETENCIES, GMDSS_COMPETENCIES, getCompetencyTitle } from '../data/competencyMetadata';
import {
  Timer,
  Flag,
  LayoutGrid,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  RotateCcw,
  Home,
  Check,
  ChevronRight,
  ChevronLeft,
  X,
  FileCheck
} from 'lucide-react';
import confetti from 'canvas-confetti';

// ==========================================
// 1. EXAM SETUP SCREEN
// ==========================================
export const ExamSetupScreen: React.FC = () => {
  const { currentTrack, navigateBack, requestStartOfficialExam, requestStartCustomExam } = useApp();
  const [selectedTab, setSelectedTab] = useState<'OFFICIAL' | 'CUSTOM'>('OFFICIAL');

  // Custom exam setup state
  const competencies = currentTrack === 'OIC-NW' ? OIC_NW_COMPETENCIES : GMDSS_COMPETENCIES;
  const [selectedComps, setSelectedComps] = useState<string[]>(competencies.map((c) => c.code));
  const [questionCount, setQuestionCount] = useState<number>(50);
  const [timeLimitMinutes, setTimeLimitMinutes] = useState<number>(60);

  const toggleComp = (code: string) => {
    setSelectedComps((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code]
    );
  };

  const selectAllComps = () => setSelectedComps(competencies.map((c) => c.code));
  const clearAllComps = () => setSelectedComps([]);

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="Mock Examination"
        subtitle={`${currentTrack} Simulation Mode`}
        onBack={navigateBack}
      />

      <div className="max-w-2xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-5">
        {/* Tab Toggle */}
        <div className="flex bg-[#0F3460] p-1 rounded-xl border border-[#1E3E62]">
          <button
            onClick={() => setSelectedTab('OFFICIAL')}
            className={`flex-1 py-2.5 rounded-lg text-xs font-bold transition-colors cursor-pointer ${
              selectedTab === 'OFFICIAL' ? 'bg-[#D4AF37] text-[#0B192C] shadow' : 'text-slate-300 hover:text-white'
            }`}
          >
            Official Simulation
          </button>
          <button
            onClick={() => setSelectedTab('CUSTOM')}
            className={`flex-1 py-2.5 rounded-lg text-xs font-bold transition-colors cursor-pointer ${
              selectedTab === 'CUSTOM' ? 'bg-[#D4AF37] text-[#0B192C] shadow' : 'text-slate-300 hover:text-white'
            }`}
          >
            Custom Mock Exam
          </button>
        </div>

        {selectedTab === 'OFFICIAL' ? (
          /* Official Simulation Tab */
          <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-6 shadow-xl space-y-4">
            <div>
              <div className="flex items-center gap-2 text-xs font-bold text-[#D4AF37] tracking-wider uppercase">
                <FileCheck className="w-4 h-4" />
                <span>Philippine MARINA Regulatory Standard</span>
              </div>
              <h2 className="text-xl font-black text-white mt-1">
                {currentTrack} Official Board Exam Simulation
              </h2>
              <p className="text-xs text-slate-300 mt-1">
                Strict timed conditions mirroring actual MARINA licensure examination quotas.
              </p>
            </div>

            <div className="bg-[#0B192C] border border-[#1E3E62] rounded-xl p-4 space-y-2.5 text-xs text-slate-300">
              <div className="flex justify-between">
                <span>Total Questions:</span>
                <strong className="text-white">
                  {currentTrack === 'OIC-NW' ? '185 Items' : '100 Items'}
                </strong>
              </div>
              <div className="flex justify-between">
                <span>Time Allotment:</span>
                <strong className="text-white">
                  {currentTrack === 'OIC-NW' ? '180 Minutes (3 Hours)' : '90 Minutes (1.5 Hours)'}
                </strong>
              </div>
              <div className="flex justify-between">
                <span>Passing Grade:</span>
                <strong className="text-emerald-400">70.0% Minimum</strong>
              </div>
              <div className="flex justify-between">
                <span>Format:</span>
                <span className="text-slate-400">Multiple Choice, No immediate hints</span>
              </div>
            </div>

            <div className="text-[11px] text-slate-400 leading-relaxed">
              Questions are algorithmically sampled across all official competencies per regulatory quota.
              You may flag items for review and submit at any time.
            </div>

            <button
              onClick={requestStartOfficialExam}
              className="w-full py-3.5 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-black text-sm rounded-xl tracking-wider shadow-lg transition-colors cursor-pointer uppercase flex items-center justify-center gap-2"
            >
              <span>START OFFICIAL EXAM SIMULATION</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        ) : (
          /* Custom Mock Exam Tab */
          <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-6 shadow-xl space-y-5">
            <div>
              <h2 className="text-lg font-bold text-white">Configure Custom Mock Exam</h2>
              <p className="text-xs text-slate-300 mt-0.5">
                Customize target competencies, item volume, and timing.
              </p>
            </div>

            {/* Competencies multi-select */}
            <div>
              <div className="flex items-center justify-between text-xs mb-2">
                <span className="font-bold text-[#D4AF37] uppercase">Select Competencies:</span>
                <div className="flex gap-2">
                  <button
                    onClick={selectAllComps}
                    className="text-[11px] text-[#2E86AB] hover:underline cursor-pointer"
                  >
                    Select All
                  </button>
                  <span className="text-slate-500">•</span>
                  <button
                    onClick={clearAllComps}
                    className="text-[11px] text-slate-400 hover:underline cursor-pointer"
                  >
                    Clear
                  </button>
                </div>
              </div>

              <div className="max-h-48 overflow-y-auto bg-[#0B192C] border border-[#1E3E62] rounded-xl p-3 space-y-2 pr-1">
                {competencies.map((comp) => {
                  const checked = selectedComps.includes(comp.code);
                  return (
                    <label
                      key={comp.code}
                      className="flex items-center gap-2.5 text-xs text-slate-200 cursor-pointer hover:text-white"
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => toggleComp(comp.code)}
                        className="w-4 h-4 rounded text-[#D4AF37] accent-[#D4AF37] cursor-pointer"
                      />
                      <span className="font-bold text-[#D4AF37]">{comp.code}:</span>
                      <span className="truncate">{comp.title}</span>
                    </label>
                  );
                })}
              </div>
            </div>

            {/* Question Count */}
            <div>
              <div className="flex justify-between text-xs mb-2">
                <span className="font-bold text-[#D4AF37] uppercase">Question Count:</span>
                <strong className="text-white">{questionCount} Items</strong>
              </div>
              <div className="flex gap-2">
                {[20, 35, 50, 75, 100].map((num) => (
                  <button
                    key={num}
                    onClick={() => setQuestionCount(num)}
                    className={`flex-1 py-1.5 rounded-lg text-xs font-bold border transition-colors cursor-pointer ${
                      questionCount === num
                        ? 'bg-[#2E86AB] text-white border-[#2E86AB]'
                        : 'bg-[#0B192C] text-slate-300 border-[#1E3E62] hover:border-slate-500'
                    }`}
                  >
                    {num}
                  </button>
                ))}
              </div>
            </div>

            {/* Time Limit */}
            <div>
              <div className="flex justify-between text-xs mb-2">
                <span className="font-bold text-[#D4AF37] uppercase">Time Limit:</span>
                <strong className="text-white">{timeLimitMinutes} Minutes</strong>
              </div>
              <div className="flex gap-2">
                {[30, 45, 60, 90, 120].map((mins) => (
                  <button
                    key={mins}
                    onClick={() => setTimeLimitMinutes(mins)}
                    className={`flex-1 py-1.5 rounded-lg text-xs font-bold border transition-colors cursor-pointer ${
                      timeLimitMinutes === mins
                        ? 'bg-[#2E86AB] text-white border-[#2E86AB]'
                        : 'bg-[#0B192C] text-slate-300 border-[#1E3E62] hover:border-slate-500'
                    }`}
                  >
                    {mins}m
                  </button>
                ))}
              </div>
            </div>

            <button
              disabled={selectedComps.length === 0}
              onClick={() => requestStartCustomExam(selectedComps, questionCount, timeLimitMinutes)}
              className="w-full py-3.5 bg-[#D4AF37] hover:bg-[#F3C64F] disabled:opacity-40 text-[#0B192C] font-black text-sm rounded-xl tracking-wider shadow-lg transition-colors cursor-pointer uppercase flex items-center justify-center gap-2"
            >
              <span>START CUSTOM MOCK EXAM</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

// ==========================================
// 2. EXAM RUNNER SCREEN
// ==========================================
export const ExamRunnerScreen: React.FC = () => {
  const {
    activeExam,
    selectExamAnswer,
    toggleExamQuestionFlag,
    setExamCurrentIndex,
    submitExam,
    saveExamSession,
    navigateBack
  } = useApp();

  const [showGridModal, setShowGridModal] = useState(false);
  const [showSubmitConfirm, setShowSubmitConfirm] = useState(false);

  if (!activeExam || activeExam.questions.length === 0) {
    return (
      <div className="min-h-screen bg-[#070F1B] text-white flex flex-col items-center justify-center p-6 text-center">
        <h2 className="text-lg font-bold">No Active Exam Session</h2>
        <button
          onClick={navigateBack}
          className="mt-4 px-4 py-2 bg-[#D4AF37] text-[#0B192C] font-bold text-xs rounded-xl"
        >
          Return to Menu
        </button>
      </div>
    );
  }

  const { questions, currentIndex, userAnswers, flaggedQuestionIds, timeRemainingSeconds } = activeExam;
  const currentQuestion = questions[currentIndex];

  const mins = Math.floor(timeRemainingSeconds / 60);
  const secs = timeRemainingSeconds % 60;
  const formattedTime = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  const isUrgentTime = timeRemainingSeconds <= 300; // < 5 mins

  const answeredCount = Object.keys(userAnswers).length;
  const totalCount = questions.length;
  const isCurrentFlagged = flaggedQuestionIds.includes(currentQuestion.id);

  const options = [
    { text: currentQuestion.optionA, idx: 0, letter: 'A' },
    { text: currentQuestion.optionB, idx: 1, letter: 'B' },
    currentQuestion.optionC ? { text: currentQuestion.optionC, idx: 2, letter: 'C' } : null,
    currentQuestion.optionD ? { text: currentQuestion.optionD, idx: 3, letter: 'D' } : null,
    currentQuestion.optionE ? { text: currentQuestion.optionE, idx: 4, letter: 'E' } : null,
    currentQuestion.optionF ? { text: currentQuestion.optionF, idx: 5, letter: 'F' } : null
  ].filter(Boolean) as { text: string; idx: number; letter: string }[];

  const handleNext = () => {
    if (currentIndex < questions.length - 1) {
      setExamCurrentIndex(currentIndex + 1);
    } else {
      setShowSubmitConfirm(true);
    }
  };

  const handlePrev = () => {
    if (currentIndex > 0) {
      setExamCurrentIndex(currentIndex - 1);
    }
  };

  const handleExitAndSave = () => {
    saveExamSession();
    navigateBack();
  };

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-20 select-none">
      {/* Top Header */}
      <header className="sticky top-0 z-40 bg-[#0B192C]/95 backdrop-blur border-b border-[#1E3E62] px-4 py-2.5 shadow-md">
        <div className="max-w-3xl mx-auto flex items-center justify-between gap-3">
          <div className="flex items-center gap-2 min-w-0">
            <button
              onClick={handleExitAndSave}
              className="text-xs text-slate-400 hover:text-white px-2 py-1 rounded-lg hover:bg-white/10 transition-colors cursor-pointer"
            >
              Exit & Save
            </button>
            <span className="text-xs text-slate-500">|</span>
            <span className="text-xs font-bold text-white truncate">
              Item {currentIndex + 1} / {totalCount}
            </span>
          </div>

          <div className="flex items-center gap-2">
            {/* Timer Badge */}
            <div
              className={`flex items-center gap-1.5 px-3 py-1 rounded-xl text-xs font-black tracking-wider ${
                isUrgentTime
                  ? 'bg-red-950 text-red-400 border border-red-500 animate-pulse'
                  : 'bg-[#0F3460] text-[#D4AF37] border border-[#2E86AB]/50'
              }`}
            >
              <Timer className="w-3.5 h-3.5" />
              <span>{formattedTime}</span>
            </div>

            <button
              onClick={() => toggleExamQuestionFlag(currentQuestion.id)}
              className={`p-1.5 rounded-lg border transition-colors cursor-pointer ${
                isCurrentFlagged
                  ? 'bg-amber-500/20 border-amber-500 text-amber-400'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
              title="Flag for review"
            >
              <Flag className={`w-4 h-4 ${isCurrentFlagged ? 'fill-current' : ''}`} />
            </button>

            <button
              onClick={() => setShowGridModal(true)}
              className="p-1.5 rounded-lg text-slate-300 hover:text-white hover:bg-white/10 transition-colors cursor-pointer"
              title="Overview Grid"
            >
              <LayoutGrid className="w-4 h-4 text-[#2E86AB]" />
            </button>
          </div>
        </div>
      </header>

      {/* Progress Line */}
      <div className="w-full bg-[#070F1B] h-1.5">
        <div
          className="bg-gradient-to-r from-[#2E86AB] to-[#D4AF37] h-full transition-all duration-300"
          style={{ width: `${((currentIndex + 1) / totalCount) * 100}%` }}
        />
      </div>

      {/* Question Body */}
      <div className="max-w-3xl mx-auto w-full px-4 sm:px-6 pt-4 flex-1 flex flex-col justify-between">
        <div className="space-y-4 my-2">
          {/* Question Card */}
          <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 sm:p-6 shadow-xl">
            <div className="flex items-center justify-between text-xs text-slate-400 mb-3">
              <span className="font-extrabold text-[#D4AF37] bg-[#0B192C] px-2 py-0.5 rounded">
                {currentQuestion.competencyCode}
              </span>
              <span>{currentQuestion.competencyDescription}</span>
            </div>

            <h2 className="text-base sm:text-lg font-bold text-white leading-relaxed">
              {currentQuestion.questionText}
            </h2>
          </div>

          {/* Options */}
          <div className="space-y-2.5">
            {options.map((opt) => {
              const isSelected = userAnswers[currentQuestion.id] === opt.idx;
              return (
                <button
                  key={opt.idx}
                  onClick={() => selectExamAnswer(currentQuestion.id, opt.idx)}
                  className={`w-full p-4 rounded-xl border text-left transition-all duration-150 flex items-start gap-3 cursor-pointer ${
                    isSelected
                      ? 'bg-[#1E3E62] border-[#D4AF37] text-white font-bold ring-1 ring-[#D4AF37] shadow'
                      : 'bg-[#0B192C] border-[#1E3E62] text-slate-200 hover:border-[#2E86AB]'
                  }`}
                >
                  <span className="w-6 h-6 rounded-lg bg-[#070F1B] border border-white/10 text-xs font-bold flex items-center justify-center shrink-0 text-[#D4AF37]">
                    {opt.letter}
                  </span>
                  <span className="text-xs sm:text-sm leading-snug pt-0.5 flex-1">{opt.text}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="sticky bottom-0 bg-[#070F1B]/95 backdrop-blur border-t border-[#1E3E62] px-4 py-3 -mx-4 sm:-mx-6 mt-6 flex items-center justify-between gap-3">
          <button
            onClick={handlePrev}
            disabled={currentIndex === 0}
            className="flex-1 py-2.5 px-3 bg-[#0F3460] hover:bg-[#1E3E62] disabled:opacity-40 disabled:cursor-not-allowed text-white font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer border border-white/10"
          >
            <ChevronLeft className="w-4 h-4" />
            <span>PREV</span>
          </button>

          <button
            onClick={() => setShowSubmitConfirm(true)}
            className="py-2.5 px-4 bg-[#1E3E62] hover:bg-[#2E86AB] text-white font-bold text-xs rounded-xl border border-white/10 transition-colors cursor-pointer"
          >
            SUBMIT ({answeredCount}/{totalCount})
          </button>

          <button
            onClick={handleNext}
            className="flex-1 py-2.5 px-3 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer shadow"
          >
            <span>{currentIndex === questions.length - 1 ? 'FINISH' : 'NEXT'}</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Overview Grid Modal */}
      {showGridModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0B192C] border border-[#1E3E62] w-full max-w-lg rounded-2xl p-5 shadow-2xl text-white max-h-[85vh] flex flex-col">
            <div className="flex items-center justify-between pb-3 border-b border-white/10">
              <h3 className="font-bold text-white text-base">Exam Questions Map</h3>
              <button onClick={() => setShowGridModal(false)} className="text-slate-400 p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex items-center gap-4 text-xs text-slate-400 py-2.5 border-b border-white/5">
              <span className="flex items-center gap-1.5">
                <span className="w-3 h-3 rounded-full bg-[#2E86AB]" /> Answered ({answeredCount})
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-3 h-3 rounded-full bg-[#0F3460] border border-white/20" /> Unanswered
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-3 h-3 rounded-full bg-amber-400" /> Flagged ({flaggedQuestionIds.length})
              </span>
            </div>

            <div className="overflow-y-auto py-4 flex-1 grid grid-cols-6 sm:grid-cols-8 gap-2 pr-1">
              {questions.map((q, idx) => {
                const isAns = userAnswers[q.id] !== undefined;
                const isFlag = flaggedQuestionIds.includes(q.id);
                const isCurrent = idx === currentIndex;

                let cls = isAns
                  ? 'bg-[#2E86AB] text-white border-[#2E86AB] font-bold'
                  : 'bg-[#0F3460] text-slate-300 border-[#1E3E62]';

                return (
                  <button
                    key={q.id}
                    onClick={() => {
                      setExamCurrentIndex(idx);
                      setShowGridModal(false);
                    }}
                    className={`h-10 rounded-xl border text-xs flex items-center justify-center transition-colors cursor-pointer relative ${cls} ${
                      isCurrent ? 'ring-2 ring-[#D4AF37]' : ''
                    }`}
                  >
                    <span>{idx + 1}</span>
                    {isFlag && (
                      <span className="w-2 h-2 rounded-full bg-amber-400 absolute top-1 right-1" />
                    )}
                  </button>
                );
              })}
            </div>

            <div className="pt-3 border-t border-white/10 flex justify-between items-center">
              <button
                onClick={() => {
                  setShowGridModal(false);
                  setShowSubmitConfirm(true);
                }}
                className="px-4 py-2 bg-[#D4AF37] text-[#0B192C] text-xs font-bold rounded-xl"
              >
                Submit Exam
              </button>
              <button
                onClick={() => setShowGridModal(false)}
                className="px-4 py-2 bg-[#1E3E62] text-xs font-bold rounded-xl"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Submit Confirmation Dialog */}
      {showSubmitConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-[#0F3460] border border-[#2E86AB] w-full max-w-md rounded-2xl p-6 shadow-2xl text-white text-center">
            <AlertTriangle className="w-12 h-12 text-[#D4AF37] mx-auto mb-3" />
            <h3 className="text-xl font-black text-white">Submit Examination?</h3>
            <p className="text-xs text-slate-300 mt-1">
              You have answered <strong className="text-white">{answeredCount}</strong> of{' '}
              <strong className="text-white">{totalCount}</strong> questions.
            </p>

            {answeredCount < totalCount && (
              <div className="my-3 p-3 bg-amber-950/60 border border-amber-600/40 rounded-xl text-xs text-amber-300">
                Warning: You still have {totalCount - answeredCount} unanswered questions!
              </div>
            )}

            <div className="flex gap-2.5 mt-5">
              <button
                onClick={() => setShowSubmitConfirm(false)}
                className="flex-1 py-2.5 rounded-xl border border-white/20 text-slate-300 text-xs font-bold"
              >
                Continue Exam
              </button>
              <button
                onClick={() => {
                  setShowSubmitConfirm(false);
                  submitExam();
                }}
                className="flex-1 py-2.5 rounded-xl bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] text-xs font-black shadow"
              >
                CONFIRM SUBMISSION
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// ==========================================
// 3. EXAM RESULT SCREEN
// ==========================================
export const ExamResultScreen: React.FC<{ recordId?: number | null }> = ({ recordId }) => {
  const { currentTrack, navigateTo, popToHome, setActiveRecordId } = useApp();
  const [record, setRecord] = useState<ExamRecord | null>(null);

  useEffect(() => {
    (async () => {
      if (recordId) {
        const rec = await db.getExamRecordById(recordId);
        setRecord(rec);
        if (rec?.passed) {
          confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 } });
        }
      }
    })();
  }, [recordId]);

  if (!record) {
    return (
      <div className="min-h-screen bg-[#070F1B] text-white flex flex-col items-center justify-center p-6 text-center">
        <h2 className="text-lg font-bold">Loading Exam Results...</h2>
      </div>
    );
  }

  const breakdown: Record<string, { total: number; correct: number }> = JSON.parse(
    record.competencyBreakdownJson || '{}'
  );

  const mins = Math.floor(record.timeUsedSeconds / 60);
  const secs = record.timeUsedSeconds % 60;

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="Examination Results"
        subtitle={record.title}
        onBack={popToHome}
      />

      <div className="max-w-2xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-5">
        {/* Pass / Fail Score Card */}
        <div
          className={`border rounded-2xl p-6 text-center shadow-2xl relative overflow-hidden ${
            record.passed
              ? 'bg-gradient-to-b from-[#0F3460] to-emerald-950/40 border-emerald-500/50'
              : 'bg-gradient-to-b from-[#0F3460] to-red-950/40 border-red-500/50'
          }`}
        >
          <div
            className={`inline-block px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider mb-3 ${
              record.passed
                ? 'bg-emerald-500 text-[#0B192C]'
                : 'bg-red-600 text-white'
            }`}
          >
            {record.passed ? 'PASSED (70% MARINA STANDARD)' : 'DID NOT PASS (70% STANDARD)'}
          </div>

          <div className="text-5xl font-black text-[#D4AF37] my-2">{record.percentage}%</div>
          <p className="text-sm font-bold text-white">
            Score: {record.score} / {record.totalQuestions} Questions Correct
          </p>
          <p className="text-xs text-slate-400 mt-1">
            Time Taken: {mins}m {secs}s
          </p>

          <div className="grid grid-cols-2 gap-3 mt-6 pt-4 border-t border-white/10 text-xs">
            <button
              onClick={() => {
                setActiveRecordId(record.id);
                navigateTo('ExamReview');
              }}
              className="py-2.5 px-4 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold rounded-xl shadow cursor-pointer uppercase tracking-wider"
            >
              Review All Answers
            </button>

            <button
              onClick={popToHome}
              className="py-2.5 px-4 bg-[#1E3E62] hover:bg-[#2E86AB] text-white font-bold rounded-xl cursor-pointer"
            >
              Back to Home
            </button>
          </div>
        </div>

        {/* Competency Breakdown Table */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
          <h3 className="text-xs font-bold text-[#D4AF37] tracking-wider uppercase">
            Competency Performance Breakdown
          </h3>

          <div className="space-y-3">
            {Object.entries(breakdown).map(([code, data]) => {
              const compPercent = data.total > 0 ? Math.round((data.correct / data.total) * 100) : 0;
              const isPass = compPercent >= 70;

              return (
                <div key={code} className="bg-[#0B192C] p-3 rounded-xl border border-white/5 space-y-1.5">
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-bold text-white">
                      {code} • {getCompetencyTitle(currentTrack, code)}
                    </span>
                    <span className={isPass ? 'text-emerald-400 font-bold' : 'text-red-400 font-bold'}>
                      {data.correct} / {data.total} ({compPercent}%)
                    </span>
                  </div>
                  <div className="w-full bg-[#070F1B] h-1.5 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full ${isPass ? 'bg-emerald-400' : 'bg-red-400'}`}
                      style={{ width: `${compPercent}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

// ==========================================
// 4. EXAM REVIEW SCREEN
// ==========================================
export const ExamReviewScreen: React.FC<{ recordId?: number | null }> = ({ recordId }) => {
  const { navigateBack, currentTrack } = useApp();
  const [record, setRecord] = useState<ExamRecord | null>(null);
  const [answers, setAnswers] = useState<ExamAnswer[]>([]);
  const [questionsMap, setQuestionsMap] = useState<Record<number, Question>>({});
  const [filterType, setFilterType] = useState<'ALL' | 'INCORRECT' | 'CORRECT'>('ALL');

  useEffect(() => {
    (async () => {
      if (recordId) {
        const rec = await db.getExamRecordById(recordId);
        setRecord(rec);
        const ans = await db.getExamAnswersByExamId(recordId);
        setAnswers(ans);

        const qIds = ans.map((a) => a.questionId);
        const qList = await db.getQuestionsByIds(qIds);
        const map: Record<number, Question> = {};
        qList.forEach((q) => (map[q.id] = q));
        setQuestionsMap(map);
      }
    })();
  }, [recordId]);

  const filteredAnswers = answers.filter((a) => {
    if (filterType === 'INCORRECT') return !a.isCorrect;
    if (filterType === 'CORRECT') return a.isCorrect;
    return true;
  });

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="Exam Answers Review"
        subtitle={record?.title || 'Review Answers'}
        onBack={navigateBack}
      />

      <div className="max-w-3xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-4">
        {/* Filters */}
        <div className="flex gap-2">
          {(['ALL', 'INCORRECT', 'CORRECT'] as const).map((t) => (
            <button
              key={t}
              onClick={() => setFilterType(t)}
              className={`flex-1 py-2 text-xs font-bold rounded-xl border transition-colors cursor-pointer ${
                filterType === t
                  ? 'bg-[#D4AF37] text-[#0B192C] border-[#D4AF37]'
                  : 'bg-[#0F3460] text-slate-300 border-[#1E3E62]'
              }`}
            >
              {t === 'ALL' && `All (${answers.length})`}
              {t === 'INCORRECT' && `Incorrect Only (${answers.filter((a) => !a.isCorrect).length})`}
              {t === 'CORRECT' && `Correct Only (${answers.filter((a) => a.isCorrect).length})`}
            </button>
          ))}
        </div>

        {/* Answers List */}
        <div className="space-y-4">
          {filteredAnswers.map((ans, idx) => {
            const q = questionsMap[ans.questionId];

            return (
              <div
                key={ans.id || idx}
                className={`bg-[#0F3460] border rounded-2xl p-5 shadow space-y-3 ${
                  ans.isCorrect ? 'border-emerald-500/30' : 'border-red-500/30'
                }`}
              >
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <span className="font-extrabold text-[#D4AF37] bg-[#0B192C] px-2 py-0.5 rounded">
                      {ans.competencyCode}
                    </span>
                    <span className="text-slate-400">Item #{idx + 1}</span>
                  </div>

                  <span
                    className={`font-bold flex items-center gap-1 ${
                      ans.isCorrect ? 'text-emerald-400' : 'text-red-400'
                    }`}
                  >
                    {ans.isCorrect ? (
                      <>
                        <CheckCircle2 className="w-4 h-4" /> Correct
                      </>
                    ) : (
                      <>
                        <XCircle className="w-4 h-4" /> Incorrect
                      </>
                    )}
                  </span>
                </div>

                <h4 className="text-sm font-bold text-white leading-relaxed">{ans.questionText}</h4>

                {/* Option summary */}
                {q && (
                  <div className="space-y-1.5 text-xs">
                    {/* User Selected */}
                    <div
                      className={`p-2.5 rounded-lg border flex items-center justify-between ${
                        ans.isCorrect
                          ? 'bg-emerald-950/60 border-emerald-500 text-emerald-200'
                          : 'bg-red-950/60 border-red-500 text-red-200'
                      }`}
                    >
                      <span>
                        <strong>Your Answer:</strong>{' '}
                        {ans.selectedIndex === -1
                          ? 'Skipped / Unanswered'
                          : `Option ${String.fromCharCode(65 + ans.selectedIndex)}`}
                      </span>
                    </div>

                    {!ans.isCorrect && (
                      <div className="p-2.5 rounded-lg bg-emerald-950/40 border border-emerald-600/40 text-emerald-300">
                        <span>
                          <strong>Correct Key:</strong> Option {q.correctAnswerLetter}
                        </span>
                      </div>
                    )}

                    {q.explanation && (
                      <div className="p-3 rounded-lg bg-[#0B192C] border border-white/5 text-slate-300 leading-relaxed mt-2">
                        <strong className="text-[#D4AF37] block mb-1">Explanation:</strong>
                        {q.explanation}
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
