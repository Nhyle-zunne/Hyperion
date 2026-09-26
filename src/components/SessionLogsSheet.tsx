import React from 'react';
import { SavedSessionLog } from '../types';
import { X, Play, Trash2, History } from 'lucide-react';

interface SessionLogsSheetProps {
  logs: SavedSessionLog[];
  onDismiss: () => void;
  onResume: (log: SavedSessionLog) => void;
  onDeleteLog: (id: number) => void;
  onClearAllLogs: () => void;
}

export const SessionLogsSheet: React.FC<SessionLogsSheetProps> = ({
  logs,
  onDismiss,
  onResume,
  onDeleteLog,
  onClearAllLogs
}) => {
  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/75 backdrop-blur-sm animate-fade-in">
      <div className="bg-[#0B192C] border-t sm:border border-[#1E3E62] w-full max-w-xl max-h-[85vh] rounded-t-2xl sm:rounded-2xl flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[#1E3E62]">
          <div className="flex items-center gap-2">
            <History className="w-5 h-5 text-[#D4AF37]" />
            <h3 className="font-bold text-white text-base">Saved Study Sessions</h3>
            <span className="text-xs bg-[#1E3E62] text-slate-300 px-2 py-0.5 rounded-full">
              {logs.length}
            </span>
          </div>

          <button
            onClick={onDismiss}
            className="text-slate-400 hover:text-white p-1 rounded-full hover:bg-white/10 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-4 overflow-y-auto space-y-3 flex-1">
          {logs.length === 0 ? (
            <div className="py-12 text-center text-slate-400 text-sm">
              No saved sessions found.
            </div>
          ) : (
            logs.map((log) => (
              <div
                key={log.id}
                className="bg-[#0F3460] border border-[#1E3E62] rounded-xl p-3.5 flex items-center justify-between gap-3 hover:border-[#2E86AB] transition-colors"
              >
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <span
                      className={`text-[10px] font-bold px-1.5 py-0.5 rounded uppercase ${
                        log.sessionType === 'EXAM'
                          ? 'bg-purple-900/60 text-purple-200 border border-purple-700/50'
                          : 'bg-emerald-900/60 text-emerald-200 border border-emerald-700/50'
                      }`}
                    >
                      {log.sessionType}
                    </span>
                    <span className="text-xs text-slate-400">
                      {new Date(log.lastActiveTimestamp).toLocaleDateString()} • {new Date(log.lastActiveTimestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <h4 className="font-bold text-white text-sm mt-1 truncate">{log.title}</h4>
                  <p className="text-xs text-slate-300 mt-0.5 truncate">{log.subtitle}</p>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() => onDeleteLog(log.id)}
                    className="p-2 text-slate-400 hover:text-red-400 hover:bg-white/5 rounded-lg transition-colors cursor-pointer"
                    title="Delete session"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>

                  <button
                    onClick={() => onResume(log)}
                    className="flex items-center gap-1.5 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-bold text-xs px-3.5 py-2 rounded-xl transition-colors cursor-pointer shadow"
                  >
                    <Play className="w-3.5 h-3.5 fill-current" />
                    <span>RESUME</span>
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        {logs.length > 0 && (
          <div className="p-4 border-t border-[#1E3E62] flex justify-between items-center bg-[#070F1B]">
            <button
              onClick={onClearAllLogs}
              className="text-xs text-red-400 hover:text-red-300 flex items-center gap-1.5 cursor-pointer font-medium"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>Clear All Sessions</span>
            </button>
            <button
              onClick={onDismiss}
              className="text-xs text-slate-300 hover:text-white px-4 py-2 bg-[#1E3E62] rounded-lg cursor-pointer font-medium"
            >
              Close
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
