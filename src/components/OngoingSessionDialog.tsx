import React from 'react';
import { SavedSessionLog } from '../types';
import { AlertCircle, Play, PlusCircle, X } from 'lucide-react';

interface OngoingSessionDialogProps {
  prompt: {
    existingSession: SavedSessionLog;
    newSessionTitle: string;
    onStartNew: () => void;
  };
  onDismiss: () => void;
  onResume: (log: SavedSessionLog) => void;
  onStartNew: () => void;
  onDeleteLog: (id: number) => void;
}

export const OngoingSessionDialog: React.FC<OngoingSessionDialogProps> = ({
  prompt,
  onDismiss,
  onResume,
  onStartNew
}) => {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
      <div className="bg-[#0F3460] border border-[#2E86AB] w-full max-w-md rounded-2xl p-6 shadow-2xl text-white">
        <div className="flex items-center gap-3 mb-4">
          <div className="p-2.5 rounded-full bg-[#D4AF37]/20 text-[#D4AF37]">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-white">Ongoing Session Found</h3>
            <p className="text-xs text-slate-300">You have an unfinished session in progress.</p>
          </div>
        </div>

        <div className="bg-[#0B192C]/80 border border-[#1E3E62] rounded-xl p-3.5 my-4">
          <div className="text-xs font-bold text-[#D4AF37] uppercase">Current Active Session</div>
          <div className="text-sm font-bold text-white mt-0.5">{prompt.existingSession.title}</div>
          <div className="text-xs text-slate-400 mt-0.5">{prompt.existingSession.subtitle}</div>
        </div>

        <div className="text-xs text-slate-300 mb-5">
          Would you like to resume this session, or start fresh with <span className="font-semibold text-white">"{prompt.newSessionTitle}"</span>?
        </div>

        <div className="flex flex-col gap-2.5">
          <button
            onClick={() => onResume(prompt.existingSession)}
            className="w-full flex items-center justify-center gap-2 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-bold py-2.5 px-4 rounded-xl transition-colors shadow cursor-pointer text-sm"
          >
            <Play className="w-4 h-4 fill-current" />
            <span>RESUME PREVIOUS SESSION</span>
          </button>

          <button
            onClick={onStartNew}
            className="w-full flex items-center justify-center gap-2 bg-[#1E3E62] hover:bg-[#2E86AB] text-white font-semibold py-2.5 px-4 rounded-xl transition-colors cursor-pointer text-sm"
          >
            <PlusCircle className="w-4 h-4" />
            <span>START NEW (OVERWRITE)</span>
          </button>

          <button
            onClick={onDismiss}
            className="w-full flex items-center justify-center gap-2 text-slate-400 hover:text-white py-2 px-4 rounded-xl transition-colors cursor-pointer text-xs"
          >
            <X className="w-3.5 h-3.5" />
            <span>CANCEL</span>
          </button>
        </div>
      </div>
    </div>
  );
};
