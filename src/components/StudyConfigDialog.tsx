import React, { useState } from 'react';
import { PracticeFilterMode, StudyMode, PracticeArgs } from '../types';
import { X, Play, BookOpen, CheckSquare, Layers, Zap, Shuffle } from 'lucide-react';

interface StudyConfigDialogProps {
  title: string;
  subtitle: string;
  totalAvailable: number;
  competencyCode?: string | null;
  partNumber?: number | null;
  onDismiss: () => void;
  onStart: (args: PracticeArgs) => void;
}

export const StudyConfigDialog: React.FC<StudyConfigDialogProps> = ({
  title,
  subtitle,
  totalAvailable,
  competencyCode,
  partNumber,
  onDismiss,
  onStart
}) => {
  const [studyMode, setStudyMode] = useState<StudyMode>('TUTOR');
  const [filterMode, setFilterMode] = useState<PracticeFilterMode>('ALL');
  const [itemLimit, setItemLimit] = useState<number | null>(null); // null = ALL
  const [isRandomized, setIsRandomized] = useState(false);
  const [shuffleOptions, setShuffleOptions] = useState(false);
  const [speedRunSeconds, setSpeedRunSeconds] = useState(30);

  const handleStart = () => {
    onStart({
      title,
      filterMode,
      competencyCode,
      partNumber,
      itemLimit,
      isRandomized,
      shuffleOptions,
      studyMode,
      timeLimitSecondsPerItem: studyMode === 'SPEED_RUN' ? speedRunSeconds : null,
      randomSeed: Math.floor(Math.random() * 10000)
    });
  };

  const modeDescriptions: Record<StudyMode, { label: string; desc: string; icon: any }> = {
    TUTOR: {
      label: 'Tutor Mode',
      desc: 'Immediate feedback with complete maritime explanations on every item.',
      icon: BookOpen
    },
    DRILL_EXAM: {
      label: 'Drill Exam',
      desc: 'Answer without hints or immediate answers, review full score at the end.',
      icon: CheckSquare
    },
    FLASHCARD: {
      label: 'Flashcards',
      desc: 'Tap card to flip between Question and Answer for active memory recall.',
      icon: Layers
    },
    SPEED_RUN: {
      label: 'Speed Run',
      desc: 'Rapid-fire timed challenge to test instant reflex recall.',
      icon: Zap
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fade-in overflow-y-auto">
      <div className="bg-[#0F3460] border border-[#2E86AB] w-full max-w-lg rounded-2xl p-5 sm:p-6 shadow-2xl text-white my-8 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-start justify-between gap-3 pb-3 border-b border-white/10">
          <div>
            <h3 className="text-lg font-bold text-white">{title}</h3>
            <p className="text-xs text-slate-300 mt-0.5">{subtitle} • {totalAvailable} total items</p>
          </div>
          <button
            onClick={onDismiss}
            className="text-slate-400 hover:text-white p-1 rounded-full hover:bg-white/10 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="overflow-y-auto py-4 space-y-5 flex-1 pr-1">
          {/* Study Mode Selector */}
          <div>
            <label className="block text-xs font-bold text-[#D4AF37] tracking-wider uppercase mb-2">
              Select Study Mode
            </label>
            <div className="grid grid-cols-2 gap-2">
              {(Object.keys(modeDescriptions) as StudyMode[]).map((mode) => {
                const info = modeDescriptions[mode];
                const Icon = info.icon;
                const active = studyMode === mode;
                return (
                  <button
                    key={mode}
                    type="button"
                    onClick={() => setStudyMode(mode)}
                    className={`flex flex-col items-start p-3 rounded-xl border text-left transition-all cursor-pointer ${
                      active
                        ? 'bg-[#1E3E62] border-[#D4AF37] text-white shadow-md ring-1 ring-[#D4AF37]'
                        : 'bg-[#0B192C]/80 border-[#1E3E62] text-slate-300 hover:border-slate-500'
                    }`}
                  >
                    <div className="flex items-center gap-2 mb-1">
                      <Icon className={`w-4 h-4 ${active ? 'text-[#D4AF37]' : 'text-slate-400'}`} />
                      <span className="font-bold text-xs">{info.label}</span>
                    </div>
                    <span className="text-[11px] text-slate-400 leading-tight">
                      {info.desc}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Speed run timer if selected */}
          {studyMode === 'SPEED_RUN' && (
            <div className="bg-[#0B192C] p-3.5 rounded-xl border border-[#1E3E62]">
              <div className="flex justify-between items-center text-xs mb-2">
                <span className="font-bold text-slate-200">Timer per Question:</span>
                <span className="font-extrabold text-[#D4AF37]">{speedRunSeconds} seconds</span>
              </div>
              <div className="flex gap-2">
                {[15, 30, 45, 60].map((sec) => (
                  <button
                    key={sec}
                    type="button"
                    onClick={() => setSpeedRunSeconds(sec)}
                    className={`flex-1 py-1.5 rounded-lg text-xs font-bold border transition-colors cursor-pointer ${
                      speedRunSeconds === sec
                        ? 'bg-[#D4AF37] text-[#0B192C] border-[#D4AF37]'
                        : 'bg-[#1E3E62] text-slate-300 border-transparent hover:bg-[#2E86AB]'
                    }`}
                  >
                    {sec}s
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Question Filter Mode */}
          <div>
            <label className="block text-xs font-bold text-[#D4AF37] tracking-wider uppercase mb-2">
              Filter Questions
            </label>
            <div className="flex flex-wrap gap-1.5">
              {[
                { id: 'ALL', label: 'All Items' },
                { id: 'UNANSWERED', label: 'Unanswered' },
                { id: 'INCORRECT', label: 'My Incorrect' },
                { id: 'FAVORITES', label: 'Starred' },
                { id: 'FLAGGED', label: 'Flagged' },
                { id: 'NEEDS_REVIEW', label: 'Needs Review' },
                { id: 'MASTERED', label: 'Mastered' },
                { id: 'SMART_REVIEW', label: 'Smart Priority' }
              ].map((f) => (
                <button
                  key={f.id}
                  type="button"
                  onClick={() => setFilterMode(f.id as PracticeFilterMode)}
                  className={`text-xs px-3 py-1.5 rounded-full border transition-colors cursor-pointer font-medium ${
                    filterMode === f.id
                      ? 'bg-[#D4AF37] text-[#0B192C] border-[#D4AF37] font-bold shadow'
                      : 'bg-[#0B192C] text-slate-300 border-[#1E3E62] hover:border-slate-500'
                  }`}
                >
                  {f.label}
                </button>
              ))}
            </div>
          </div>

          {/* Item Count Limit */}
          <div>
            <label className="block text-xs font-bold text-[#D4AF37] tracking-wider uppercase mb-2">
              Question Limit
            </label>
            <div className="flex gap-2">
              {[
                { label: 'All', value: null },
                { label: '10', value: 10 },
                { label: '25', value: 25 },
                { label: '50', value: 50 },
                { label: '100', value: 100 }
              ].map((opt) => (
                <button
                  key={opt.label}
                  type="button"
                  onClick={() => setItemLimit(opt.value)}
                  className={`flex-1 py-1.5 rounded-xl text-xs font-bold border transition-colors cursor-pointer ${
                    itemLimit === opt.value
                      ? 'bg-[#2E86AB] text-white border-[#2E86AB] shadow'
                      : 'bg-[#0B192C] text-slate-300 border-[#1E3E62] hover:border-slate-500'
                  }`}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {/* Toggles: Shuffle */}
          <div className="bg-[#0B192C]/70 rounded-xl p-3.5 space-y-3 border border-[#1E3E62]">
            <label className="flex items-center justify-between cursor-pointer">
              <span className="text-xs font-medium text-slate-200 flex items-center gap-2">
                <Shuffle className="w-4 h-4 text-[#D4AF37]" />
                <span>Randomize Question Order</span>
              </span>
              <input
                type="checkbox"
                checked={isRandomized}
                onChange={(e) => setIsRandomized(e.target.checked)}
                className="w-4 h-4 rounded text-[#D4AF37] focus:ring-0 cursor-pointer accent-[#D4AF37]"
              />
            </label>

            <label className="flex items-center justify-between cursor-pointer pt-2 border-t border-white/5">
              <span className="text-xs font-medium text-slate-200 flex items-center gap-2">
                <Shuffle className="w-4 h-4 text-emerald-400" />
                <span>Shuffle Answer Choices (A-D)</span>
              </span>
              <input
                type="checkbox"
                checked={shuffleOptions}
                onChange={(e) => setShuffleOptions(e.target.checked)}
                className="w-4 h-4 rounded text-[#D4AF37] focus:ring-0 cursor-pointer accent-[#D4AF37]"
              />
            </label>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="pt-4 border-t border-white/10 flex items-center gap-3">
          <button
            onClick={onDismiss}
            className="flex-1 py-2.5 rounded-xl border border-white/20 text-slate-300 hover:text-white font-semibold text-xs transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={handleStart}
            className="flex-2 py-2.5 rounded-xl bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-black text-xs flex items-center justify-center gap-2 shadow-lg transition-colors cursor-pointer tracking-wider"
          >
            <Play className="w-4 h-4 fill-current" />
            <span>START STUDY SESSION</span>
          </button>
        </div>
      </div>
    </div>
  );
};
