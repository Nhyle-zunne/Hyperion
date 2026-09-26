import React from 'react';
import { ArrowLeft } from 'lucide-react';

interface HyperionTopBarProps {
  title: string;
  subtitle?: string;
  onBack?: () => void;
  actions?: React.ReactNode;
}

export const HyperionTopBar: React.FC<HyperionTopBarProps> = ({
  title,
  subtitle,
  onBack,
  actions
}) => {
  return (
    <header className="sticky top-0 z-40 bg-[#0B192C]/95 backdrop-blur border-b border-[#1E3E62] px-4 py-3 shadow-md">
      <div className="max-w-4xl mx-auto flex items-center justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          {onBack ? (
            <button
              onClick={onBack}
              className="p-2 -ml-1 text-slate-300 hover:text-white hover:bg-[#1E3E62] rounded-full transition-colors cursor-pointer"
              aria-label="Back"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
          ) : (
            <img
              src="/logo.png"
              alt="Hyperion Logo"
              className="w-8 h-8 rounded-full border border-[#D4AF37]/50 shadow object-cover shrink-0"
              onError={(e) => {
                // fallback to compass.svg if image error
                (e.currentTarget as HTMLImageElement).src = '/compass.svg';
              }}
            />
          )}
          <div className="min-w-0">
            <h1 className="text-base sm:text-lg font-black tracking-wide text-white truncate flex items-center gap-2">
              <span className="text-[#D4AF37] font-extrabold">{title}</span>
            </h1>
            {subtitle && (
              <p className="text-xs text-slate-400 truncate font-medium">
                {subtitle}
              </p>
            )}
          </div>
        </div>

        {actions && <div className="flex items-center gap-1.5 shrink-0">{actions}</div>}
      </div>
    </header>
  );
};
