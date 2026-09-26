import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { StudySession } from '../types';
import * as db from '../db/indexedDb';
import { Play, Pause, Square, Timer, Target, CheckCircle2, History } from 'lucide-react';

export const StudyTimerScreen: React.FC = () => {
  const {
    currentTrack,
    navigateBack,
    isStopwatchRunning,
    stopwatchSeconds,
    startStopwatch,
    pauseStopwatch,
    stopAndSaveStopwatch,
    todayGoal,
    updateGoal
  } = useApp();

  const [studySessions, setStudySessions] = useState<StudySession[]>([]);

  useEffect(() => {
    (async () => {
      const list = await db.getStudySessions(currentTrack);
      setStudySessions(list);
    })();
  }, [currentTrack, stopwatchSeconds]);

  const hours = Math.floor(stopwatchSeconds / 3600);
  const minutes = Math.floor((stopwatchSeconds % 3600) / 60);
  const seconds = stopwatchSeconds % 60;
  const formattedTime = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(
    seconds
  ).padStart(2, '0')}`;

  const goalStudySecs = todayGoal?.targetStudySeconds || 3600;
  const completedStudySecs = todayGoal?.completedStudySeconds || 0;
  const studyProgress = Math.min(100, Math.round((completedStudySecs / goalStudySecs) * 100));

  const goalQuestions = todayGoal?.targetQuestions || 100;
  const completedQuestions = todayGoal?.completedQuestions || 0;
  const questionProgress = Math.min(100, Math.round((completedQuestions / goalQuestions) * 100));

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="Study Timer & Goals"
        subtitle={`${currentTrack} Active Session`}
        onBack={navigateBack}
      />

      <div className="max-w-2xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-5">
        {/* Stopwatch Display Card */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-3xl p-8 shadow-2xl text-center space-y-6">
          <div className="flex items-center justify-center gap-2 text-xs font-bold text-[#D4AF37] uppercase tracking-wider">
            <Timer className="w-4 h-4" />
            <span>Active Maritime Study Clock</span>
          </div>

          {/* Big Time Display */}
          <div className="text-5xl sm:text-6xl font-black text-white font-mono tracking-widest my-4">
            {formattedTime}
          </div>

          {/* Timer Controls */}
          <div className="flex justify-center gap-3">
            {!isStopwatchRunning ? (
              <button
                onClick={startStopwatch}
                className="py-3 px-8 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold text-sm rounded-xl flex items-center gap-2 shadow-lg transition-colors cursor-pointer uppercase tracking-wider"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>START TIMER</span>
              </button>
            ) : (
              <button
                onClick={pauseStopwatch}
                className="py-3 px-8 bg-amber-600 hover:bg-amber-500 text-white font-extrabold text-sm rounded-xl flex items-center gap-2 shadow-lg transition-colors cursor-pointer uppercase tracking-wider"
              >
                <Pause className="w-4 h-4" />
                <span>PAUSE</span>
              </button>
            )}

            <button
              onClick={stopAndSaveStopwatch}
              disabled={stopwatchSeconds === 0}
              className="py-3 px-6 bg-[#1E3E62] hover:bg-[#2E86AB] disabled:opacity-40 text-white font-bold text-sm rounded-xl flex items-center gap-2 border border-white/10 transition-colors cursor-pointer uppercase"
            >
              <Square className="w-4 h-4" />
              <span>SAVE SESSION</span>
            </button>
          </div>

          <p className="text-xs text-slate-400">
            Recorded study time is added to your daily licensure goals and track statistics.
          </p>
        </div>

        {/* Daily Goals Card */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Target className="w-5 h-5 text-[#D4AF37]" />
              <h3 className="font-bold text-sm text-white">Today's Daily Target</h3>
            </div>
            <span className="text-xs text-slate-400">
              {todayGoal?.dateKey || new Date().toISOString().split('T')[0]}
            </span>
          </div>

          {/* Questions Progress */}
          <div className="bg-[#0B192C] p-4 rounded-xl border border-white/5 space-y-2">
            <div className="flex justify-between items-center text-xs">
              <span className="text-slate-300">Questions Answered Today:</span>
              <strong className="text-white">
                {completedQuestions} / {goalQuestions} items ({questionProgress}%)
              </strong>
            </div>
            <div className="w-full bg-[#070F1B] h-2 rounded-full overflow-hidden">
              <div
                className="bg-[#2E86AB] h-full rounded-full transition-all"
                style={{ width: `${questionProgress}%` }}
              />
            </div>
          </div>

          {/* Study Time Progress */}
          <div className="bg-[#0B192C] p-4 rounded-xl border border-white/5 space-y-2">
            <div className="flex justify-between items-center text-xs">
              <span className="text-slate-300">Study Time Today:</span>
              <strong className="text-white">
                {Math.round(completedStudySecs / 60)}m / {Math.round(goalStudySecs / 60)}m ({studyProgress}%)
              </strong>
            </div>
            <div className="w-full bg-[#070F1B] h-2 rounded-full overflow-hidden">
              <div
                className="bg-emerald-400 h-full rounded-full transition-all"
                style={{ width: `${studyProgress}%` }}
              />
            </div>
          </div>
        </div>

        {/* Recent Study Sessions Log */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
          <div className="flex items-center gap-2">
            <History className="w-4 h-4 text-[#D4AF37]" />
            <h3 className="font-bold text-xs text-[#D4AF37] tracking-wider uppercase">
              Recent Recorded Sessions
            </h3>
          </div>

          {studySessions.length === 0 ? (
            <p className="text-xs text-slate-400 py-3 text-center">
              No recorded sessions for {currentTrack} yet. Use the timer above to log study hours!
            </p>
          ) : (
            <div className="space-y-2">
              {studySessions.slice(0, 10).map((s, idx) => (
                <div
                  key={s.id || idx}
                  className="bg-[#0B192C] p-3 rounded-xl border border-white/5 flex items-center justify-between text-xs"
                >
                  <span className="text-slate-300 font-medium">
                    {new Date(s.timestamp).toLocaleDateString()} •{' '}
                    {new Date(s.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                  <strong className="text-[#D4AF37]">
                    {Math.round(s.durationSeconds / 60)} minutes
                  </strong>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
