import React from 'react';
import { SavedSessionLog } from '../types';
import { Play, Trash2, History } from 'lucide-react';

interface OngoingSessionCardProps {
  session: SavedSessionLog;
  totalLogsCount: number;
  onResume: () => void;
  onDelete: () => void;
  onViewAllLogs: () => void;
}

export const OngoingSessionCard: React.FC<OngoingSessionCardProps> = ({
  session,
  totalLogsCount,
  onResume,
  onDelete,
  onViewAllLogs
}) => {
  const percent = session.totalQuestions > 0
    ? Math.round((session.currentIndex / session.totalQuestions) * 100)
    : 0;

  return (
    <div className="bg-[#0F3460] border border-[#2E86AB]/50 rounded-2xl p-4 shadow-lg text-white">
      <div className="flex items-start justify-between gap-3 mb-2">
        <div>
          <div className="flex items-center gap-2">
            <span className="inline-block w-2.5 h-2.5 rounded-full bg-[#D4AF37] animate-pulse" />
            <span className="text-xs font-bold text-[#D4AF37] tracking-wider uppercase">
              {session.sessionType === 'EXAM' ? 'EXAM IN PROGRESS' : 'PRACTICE IN PROGRESS'}
            </span>
          </div>
          <h3 className="text-base font-bold text-white mt-0.5">{session.title}</h3>
          <p className="text-xs text-slate-300">{session.subtitle}</p>
        </div>

        <button
          onClick={onDelete}
          className="text-slate-400 hover:text-red-400 p-1.5 rounded-lg hover:bg-white/5 transition-colors cursor-pointer"
          title="Discard session"
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>

      {/* Progress Bar */}
      <div className="w-full bg-[#0B192C] h-2 rounded-full overflow-hidden my-3">
        <div
          className="bg-[#2E86AB] h-full rounded-full transition-all duration-300"
          style={{ width: `${percent}%` }}
        />
      </div>

      <div className="flex items-center justify-between gap-2 mt-3 pt-1 border-t border-white/10">
        {totalLogsCount > 1 ? (
          <button
            onClick={onViewAllLogs}
            className="flex items-center gap-1.5 text-xs text-slate-300 hover:text-white font-medium cursor-pointer"
          >
            <History className="w-3.5 h-3.5 text-[#D4AF37]" />
            <span>All Saved Sessions ({totalLogsCount})</span>
          </button>
        ) : (
          <div className="text-xs text-slate-400">Auto-saved offline</div>
        )}

        <button
          onClick={onResume}
          className="flex items-center gap-1.5 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-bold text-xs px-4 py-2 rounded-xl transition-colors shadow cursor-pointer"
        >
          <Play className="w-3.5 h-3.5 fill-current" />
          <span>RESUME</span>
        </button>
      </div>
    </div>
  );
};
