import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { OngoingSessionCard } from '../components/OngoingSessionCard';
import { SessionLogsSheet } from '../components/SessionLogsSheet';
import {
  ArrowLeftRight,
  Settings,
  Play,
  Shuffle,
  UploadCloud,
  BookOpen,
  FileCheck,
  XCircle,
  Brain,
  Star,
  Flag,
  Search,
  BarChart3,
  Timer,
  ChevronRight,
  Smartphone
} from 'lucide-react';

export const HomeScreen: React.FC = () => {
  const {
    currentTrack,
    allQuestions,
    favorites,
    flagged,
    incorrect,
    sessionLogs,
    navigateTo,
    resumeSession,
    deleteSessionLog,
    clearAllSessionLogs,
    requestStartPractice
  } = useApp();

  const [showLogsSheet, setShowLogsSheet] = useState(false);

  const totalCount = allQuestions.length;
  const attemptedCount = allQuestions.filter((q) => q.timesAttempted > 0).length;
  const masteredCount = allQuestions.filter((q) => q.masteryStatus === 'MASTERED').length;
  const totalTimesAttempted = allQuestions.reduce((acc, q) => acc + q.timesAttempted, 0);
  const totalTimesCorrect = allQuestions.reduce((acc, q) => acc + q.timesCorrect, 0);
  const accuracy = totalTimesAttempted > 0 ? Math.round((totalTimesCorrect / totalTimesAttempted) * 100) : 0;
  const progressPercent = totalCount > 0 ? Math.round((attemptedCount / totalCount) * 100) : 0;

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      {/* Top Bar */}
      <HyperionTopBar
        title="HYPERION"
        subtitle={`MARINA Reviewer • ${currentTrack}`}
        actions={
          <>
            <button
              onClick={() => navigateTo('PlayStore')}
              className="flex items-center gap-1.5 text-xs text-[#D4AF37] hover:text-white px-2.5 py-1.5 rounded-lg bg-[#0F3460]/80 hover:bg-[#1E3E62] border border-[#D4AF37]/40 transition-colors cursor-pointer"
              title="Google Play Store & Android Hub"
            >
              <Smartphone className="w-4 h-4 text-[#D4AF37]" />
              <span className="font-bold">Play Store</span>
            </button>
            <button
              onClick={() => navigateTo('TrackSelect')}
              className="flex items-center gap-1 text-xs text-slate-300 hover:text-white px-2.5 py-1.5 rounded-lg hover:bg-white/10 transition-colors cursor-pointer"
              title="Switch Track"
            >
              <ArrowLeftRight className="w-4 h-4 text-[#D4AF37]" />
              <span className="hidden sm:inline font-bold">Track</span>
            </button>
            <button
              onClick={() => navigateTo('Admin')}
              className="p-1.5 text-slate-300 hover:text-white rounded-lg hover:bg-white/10 transition-colors cursor-pointer"
              title="Admin Panel"
            >
              <Settings className="w-5 h-5" />
            </button>
          </>
        }
      />

      <div className="max-w-4xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-4">
        {/* Track Switcher Pill */}
        <div className="flex items-center justify-between">
          <button
            onClick={() => navigateTo('TrackSelect')}
            className="flex items-center gap-2 bg-[#0F3460] hover:bg-[#1E3E62] border border-[#2E86AB]/50 text-white text-xs font-bold px-3 py-1.5 rounded-full transition-colors cursor-pointer shadow-sm"
          >
            <span>{currentTrack === 'OIC-NW' ? '⚓ OIC-NW Track' : '📡 GMDSS Track'}</span>
            <ArrowLeftRight className="w-3.5 h-3.5 text-[#D4AF37]" />
          </button>

          <span className="text-xs text-slate-400 font-medium">
            {totalCount} Questions in Bank
          </span>
        </div>

        {/* Android & Google Play Store Banner */}
        <div
          onClick={() => navigateTo('PlayStore')}
          className="bg-gradient-to-r from-[#0F3460] via-[#153a66] to-[#0F3460] border border-[#D4AF37]/60 hover:border-[#D4AF37] rounded-2xl p-4 transition-all cursor-pointer shadow-lg flex items-center justify-between gap-3 group"
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#0B192C] border border-[#D4AF37] flex items-center justify-center text-[#D4AF37] group-hover:scale-105 transition-transform shrink-0">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h4 className="font-extrabold text-sm text-white">Google Play Store & Android App</h4>
                <span className="text-[10px] font-black bg-emerald-950/80 border border-emerald-500/50 text-emerald-300 px-2 py-0.5 rounded-full">
                  READY
                </span>
              </div>
              <p className="text-xs text-slate-300 mt-0.5">
                Target API 35 • Download 17MB APK, export Android Studio project (.zip), or install PWA
              </p>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-[#D4AF37] group-hover:translate-x-1 transition-transform shrink-0" />
        </div>

        {/* Ongoing Session in Progress (Resumable Session Log) */}
        {sessionLogs.length > 0 && (
          <OngoingSessionCard
            session={sessionLogs[0]}
            totalLogsCount={sessionLogs.length}
            onResume={() => resumeSession(sessionLogs[0])}
            onDelete={() => deleteSessionLog(sessionLogs[0].id)}
            onViewAllLogs={() => setShowLogsSheet(true)}
          />
        )}

        {/* Primary Overview Card */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow-xl">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h2 className="text-lg sm:text-xl font-black text-white tracking-wide">
                {currentTrack === 'OIC-NW' ? 'OIC-NW Navigation Watch' : 'GMDSS Radio Safety'}
              </h2>
              <p className="text-xs text-slate-300">
                Philippine MARINA Licensure Master Bank
              </p>
            </div>

            <div
              className={`px-2.5 py-1 rounded-xl text-xs font-extrabold tracking-wider ${
                accuracy >= 70
                  ? 'bg-emerald-900/60 text-emerald-300 border border-emerald-600/40'
                  : 'bg-[#1E3E62] text-slate-300'
              }`}
            >
              {accuracy}% ACCURACY
            </div>
          </div>

          {/* Stats Metrics Row */}
          <div className="grid grid-cols-4 gap-2 my-4 pt-3 border-t border-white/10 text-center">
            <div className="bg-[#0B192C]/60 p-2.5 rounded-xl border border-white/5">
              <div className="text-base sm:text-lg font-black text-white">{totalCount}</div>
              <div className="text-[10px] text-slate-400 font-bold uppercase">Bank Total</div>
            </div>
            <div className="bg-[#0B192C]/60 p-2.5 rounded-xl border border-white/5">
              <div className="text-base sm:text-lg font-black text-white">{attemptedCount}</div>
              <div className="text-[10px] text-slate-400 font-bold uppercase">Attempted</div>
            </div>
            <div className="bg-[#0B192C]/60 p-2.5 rounded-xl border border-white/5">
              <div className="text-base sm:text-lg font-black text-emerald-400">{masteredCount}</div>
              <div className="text-[10px] text-slate-400 font-bold uppercase">Mastered</div>
            </div>
            <div className="bg-[#0B192C]/60 p-2.5 rounded-xl border border-white/5">
              <div
                className={`text-base sm:text-lg font-black ${
                  incorrect.length > 0 ? 'text-red-400' : 'text-slate-400'
                }`}
              >
                {incorrect.length}
              </div>
              <div className="text-[10px] text-slate-400 font-bold uppercase">Incorrect</div>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="space-y-1.5 my-3">
            <div className="flex justify-between text-xs text-slate-300">
              <span>Overall Bank Coverage</span>
              <span className="font-bold text-[#D4AF37]">{progressPercent}%</span>
            </div>
            <div className="w-full bg-[#0B192C] h-2.5 rounded-full overflow-hidden">
              <div
                className="bg-gradient-to-r from-[#2E86AB] to-[#D4AF37] h-full rounded-full transition-all duration-300"
                style={{ width: `${progressPercent}%` }}
              />
            </div>
          </div>

          {/* Actions */}
          <div className="flex flex-col sm:flex-row gap-2.5 mt-5">
            <button
              onClick={() =>
                requestStartPractice({
                  title: `${currentTrack} Review (All)`,
                  filterMode: 'ALL'
                })
              }
              className="flex-1 py-3 px-4 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold text-xs sm:text-sm rounded-xl flex items-center justify-center gap-2 shadow-lg transition-colors cursor-pointer uppercase tracking-wider"
            >
              <Play className="w-4 h-4 fill-current" />
              <span>STUDY ALL ({totalCount})</span>
            </button>

            <button
              onClick={() =>
                requestStartPractice({
                  title: `${currentTrack} (Random 25 Blitz)`,
                  filterMode: 'ALL',
                  itemLimit: Math.min(25, totalCount > 0 ? totalCount : 25),
                  isRandomized: true,
                  shuffleOptions: true
                })
              }
              className="py-3 px-4 bg-[#1E3E62] hover:bg-[#2E86AB] text-white font-bold text-xs sm:text-sm rounded-xl flex items-center justify-center gap-2 border border-white/10 transition-colors cursor-pointer tracking-wide"
            >
              <Shuffle className="w-4 h-4 text-[#D4AF37]" />
              <span>RANDOM 25</span>
            </button>
          </div>
        </div>

        {/* 8,000+ Question Bank Loading Hub (Shows when bank count is under 500) */}
        {totalCount < 500 && (
          <div
            onClick={() => navigateTo('Admin')}
            className="bg-gradient-to-r from-[#1E3E62] to-[#0F3460] border border-[#2E86AB]/50 rounded-2xl p-4 flex items-center justify-between gap-3 shadow cursor-pointer hover:border-[#D4AF37] transition-all"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-[#D4AF37]/20 border border-[#D4AF37] flex items-center justify-center text-[#D4AF37] shrink-0">
                <UploadCloud className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-1.5">
                  <span>Ready to Load 8,000+ Questions</span>
                </h3>
                <p className="text-xs text-slate-300">
                  Tap to import your MARINA Excel reviewer (.xlsx) or bundled CSVs.
                </p>
              </div>
            </div>
            <ChevronRight className="w-5 h-5 text-slate-400 shrink-0" />
          </div>
        )}

        {/* Quick Actions Title */}
        <div className="pt-2">
          <h3 className="text-xs font-bold text-[#D4AF37] tracking-wider uppercase">
            Quick Actions
          </h3>
        </div>

        {/* 2-Column Action Cards */}
        <div className="grid grid-cols-2 gap-3">
          {/* Question Bank */}
          <div
            onClick={() => navigateTo('QuestionBank')}
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-[#2E86AB] rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow"
          >
            <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-[#2E86AB] flex items-center justify-center mb-3">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Question Bank</h4>
              <p className="text-xs text-slate-400 mt-0.5">Browse by Competency</p>
            </div>
          </div>

          {/* Mock Exam */}
          <div
            onClick={() => navigateTo('ExamSetup')}
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-[#D4AF37] rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow"
          >
            <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-[#D4AF37] flex items-center justify-center mb-3">
              <FileCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Mock Exam</h4>
              <p className="text-xs text-slate-400 mt-0.5">Official Simulation</p>
            </div>
          </div>

          {/* My Incorrect */}
          <div
            onClick={() => navigateTo('Incorrect')}
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-red-500/50 rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow relative"
          >
            <div className="flex items-center justify-between">
              <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-red-400 flex items-center justify-center mb-3">
                <XCircle className="w-5 h-5" />
              </div>
              {incorrect.length > 0 && (
                <span className="text-[11px] font-black bg-red-600 text-white px-2 py-0.5 rounded-full">
                  {incorrect.length}
                </span>
              )}
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">My Incorrect</h4>
              <p className="text-xs text-slate-400 mt-0.5">{incorrect.length} items to retry</p>
            </div>
          </div>

          {/* Smart Review */}
          <div
            onClick={() =>
              requestStartPractice({
                title: 'Smart Review',
                filterMode: 'SMART_REVIEW'
              })
            }
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-purple-400/50 rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow"
          >
            <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-purple-400 flex items-center justify-center mb-3">
              <Brain className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Smart Review</h4>
              <p className="text-xs text-slate-400 mt-0.5">Weak areas & priority</p>
            </div>
          </div>

          {/* Favorites */}
          <div
            onClick={() => navigateTo('Favorites')}
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-[#D4AF37] rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow"
          >
            <div className="flex items-center justify-between">
              <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-[#D4AF37] flex items-center justify-center mb-3">
                <Star className="w-5 h-5 fill-current" />
              </div>
              {favorites.length > 0 && (
                <span className="text-[11px] font-bold text-slate-300">
                  {favorites.length}
                </span>
              )}
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Favorites</h4>
              <p className="text-xs text-slate-400 mt-0.5">{favorites.length} starred</p>
            </div>
          </div>

          {/* Flagged */}
          <div
            onClick={() => navigateTo('Flagged')}
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-amber-400/50 rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow"
          >
            <div className="flex items-center justify-between">
              <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-amber-400 flex items-center justify-center mb-3">
                <Flag className="w-5 h-5 fill-current" />
              </div>
              {flagged.length > 0 && (
                <span className="text-[11px] font-bold text-slate-300">
                  {flagged.length}
                </span>
              )}
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Flagged</h4>
              <p className="text-xs text-slate-400 mt-0.5">{flagged.length} flagged items</p>
            </div>
          </div>

          {/* Search Bank */}
          <div
            onClick={() => navigateTo('Search')}
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-[#2E86AB] rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow"
          >
            <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-[#2E86AB] flex items-center justify-center mb-3">
              <Search className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Search Bank</h4>
              <p className="text-xs text-slate-400 mt-0.5">Offline keyword lookup</p>
            </div>
          </div>

          {/* Statistics */}
          <div
            onClick={() => navigateTo('Stats')}
            className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-emerald-400/50 rounded-2xl p-4 transition-colors cursor-pointer flex flex-col justify-between shadow"
          >
            <div className="w-9 h-9 rounded-xl bg-[#0B192C] text-emerald-400 flex items-center justify-center mb-3">
              <BarChart3 className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Statistics</h4>
              <p className="text-xs text-slate-400 mt-0.5">History & Mastery</p>
            </div>
          </div>
        </div>

        {/* Study Timer Row */}
        <div
          onClick={() => navigateTo('StudyTimer')}
          className="bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-[#D4AF37] rounded-2xl p-4 transition-colors cursor-pointer flex items-center justify-between gap-3 shadow"
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#0B192C] text-[#D4AF37] flex items-center justify-center shrink-0">
              <Timer className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-sm text-white">Study Timer & Goals</h4>
              <p className="text-xs text-slate-400">Track active study hours and daily targets</p>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-slate-400 shrink-0" />
        </div>
      </div>

      {/* Session Logs Sheet */}
      {showLogsSheet && (
        <SessionLogsSheet
          logs={sessionLogs}
          onDismiss={() => setShowLogsSheet(false)}
          onResume={(log) => {
            setShowLogsSheet(false);
            resumeSession(log);
          }}
          onDeleteLog={(id) => deleteSessionLog(id)}
          onClearAllLogs={() => {
            setShowLogsSheet(false);
            clearAllSessionLogs(true);
          }}
        />
      )}
    </div>
  );
};
