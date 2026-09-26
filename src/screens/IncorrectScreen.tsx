import React from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { XCircle, Play, Check, Trash2 } from 'lucide-react';

export const IncorrectScreen: React.FC = () => {
  const { currentTrack, incorrect, navigateBack, removeFromIncorrect, requestStartPractice } = useApp();

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="My Incorrect"
        subtitle={`${currentTrack} • ${incorrect.length} Questions to Retry`}
        onBack={navigateBack}
      />

      <div className="max-w-3xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-4">
        {incorrect.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <Check className="w-16 h-16 text-emerald-400 mb-3" />
            <h3 className="text-lg font-bold">No Incorrect Questions!</h3>
            <p className="text-xs text-slate-400 mt-1 max-w-sm">
              Great job! Any questions you miss during study or mock exams will automatically appear here for focused retraining.
            </p>
          </div>
        ) : (
          <>
            {/* Top Action */}
            <div className="flex items-center justify-between bg-[#0F3460] p-4 rounded-2xl border border-red-500/30 shadow">
              <div>
                <h3 className="text-sm font-bold text-white">Targeted Remediation</h3>
                <p className="text-xs text-slate-300">
                  {incorrect.length} missed questions need reinforcement
                </p>
              </div>
              <button
                onClick={() =>
                  requestStartPractice({
                    title: `${currentTrack} - Retrying Missed Questions`,
                    filterMode: 'INCORRECT'
                  })
                }
                className="py-2.5 px-4 bg-red-600 hover:bg-red-500 text-white font-bold text-xs rounded-xl flex items-center gap-2 shadow cursor-pointer uppercase tracking-wider"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>RETRY ALL ({incorrect.length})</span>
              </button>
            </div>

            {/* List */}
            <div className="space-y-3">
              {incorrect.map((q) => (
                <div
                  key={q.id}
                  className="bg-[#0F3460] border border-[#1E3E62] hover:border-red-500/40 rounded-2xl p-4 shadow space-y-2 relative"
                >
                  <div className="flex items-center justify-between text-xs">
                    <div className="flex items-center gap-2">
                      <span className="font-extrabold text-[#D4AF37] bg-[#0B192C] px-2 py-0.5 rounded">
                        {q.competencyCode}
                      </span>
                      <span className="bg-red-950 text-red-300 border border-red-500/40 font-bold px-2 py-0.5 rounded">
                        Missed {q.timesIncorrect}x
                      </span>
                    </div>

                    <button
                      onClick={() => removeFromIncorrect(q.id)}
                      className="text-xs text-slate-400 hover:text-emerald-400 flex items-center gap-1 cursor-pointer p-1"
                      title="Mark as resolved / remove from list"
                    >
                      <Check className="w-3.5 h-3.5" />
                      <span>Mark Resolved</span>
                    </button>
                  </div>

                  <h4 className="text-sm font-bold text-white leading-relaxed">{q.questionText}</h4>

                  <div className="text-xs text-slate-300 pt-1 flex items-center justify-between">
                    <span>
                      Correct Answer: <strong className="text-emerald-400">Option {q.correctAnswerLetter}</strong>
                    </span>
                  </div>

                  {q.explanation && (
                    <div className="text-xs text-slate-400 leading-relaxed bg-[#0B192C] p-2.5 rounded-lg border border-white/5 mt-1">
                      {q.explanation}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
};
