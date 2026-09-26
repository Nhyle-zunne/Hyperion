import React, { useEffect, useState } from 'react';
import { useApp } from '../context/AppContext';
import { ShieldCheck, Settings, Anchor, Radio, ChevronRight, Download, Navigation, Sparkles } from 'lucide-react';
import * as db from '../db/indexedDb';

export const TrackSelectScreen: React.FC = () => {
  const { selectTrack, navigateTo } = useApp();

  const [oicStats, setOicStats] = useState({ total: 0, attempted: 0, accuracy: 0 });
  const [gmdssStats, setGmdssStats] = useState({ total: 0, attempted: 0, accuracy: 0 });

  useEffect(() => {
    (async () => {
      const oic = await db.getAllQuestions('OIC-NW');
      const oicAttempted = oic.filter((q) => q.timesAttempted > 0).length;
      const oicTotAttempted = oic.reduce((acc, q) => acc + q.timesAttempted, 0);
      const oicTotCorrect = oic.reduce((acc, q) => acc + q.timesCorrect, 0);
      const oicAcc = oicTotAttempted > 0 ? Math.round((oicTotCorrect / oicTotAttempted) * 100) : 0;
      setOicStats({ total: oic.length, attempted: oicAttempted, accuracy: oicAcc });

      const gmdss = await db.getAllQuestions('GMDSS');
      const gmdssAttempted = gmdss.filter((q) => q.timesAttempted > 0).length;
      const gmdssTotAttempted = gmdss.reduce((acc, q) => acc + q.timesAttempted, 0);
      const gmdssTotCorrect = gmdss.reduce((acc, q) => acc + q.timesCorrect, 0);
      const gmdssAcc = gmdssTotAttempted > 0 ? Math.round((gmdssTotCorrect / gmdssTotAttempted) * 100) : 0;
      setGmdssStats({ total: gmdss.length, attempted: gmdssAttempted, accuracy: gmdssAcc });
    })();
  }, []);

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col justify-between p-4 sm:p-6 max-w-2xl mx-auto">
      {/* Top Bar with Admin */}
      <div className="flex justify-between items-center w-full pt-2">
        <div className="flex items-center gap-2 text-xs font-bold text-[#D4AF37] tracking-wider uppercase">
          <Sparkles className="w-3.5 h-3.5 text-[#D4AF37]" />
          <span>MARINA Licensure Reviewer</span>
        </div>
        <div className="flex items-center gap-2">
          {/* Direct APK Download Link */}
          <a
            href="/app-debug.apk"
            download="hyperion-marina-reviewer.apk"
            className="flex items-center gap-1.5 text-[11px] bg-[#1E3E62] hover:bg-[#2E86AB] text-[#D4AF37] hover:text-white font-bold px-3 py-1.5 rounded-full border border-[#D4AF37]/40 shadow transition-colors cursor-pointer"
            title="Download Android APK"
          >
            <Download className="w-3.5 h-3.5" />
            <span>APK (17 MB)</span>
          </a>

          <button
            onClick={() => navigateTo('Admin')}
            className="p-2 text-slate-400 hover:text-[#D4AF37] hover:bg-white/5 rounded-full transition-colors cursor-pointer"
            title="Admin Panel"
          >
            <Settings className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Hero Branding with Polished Logo */}
      <div className="text-center my-6 flex flex-col items-center">
        <div className="relative mb-3 group">
          <div className="w-24 h-24 rounded-full bg-gradient-to-tr from-[#0F3460] to-[#1E3E62] border-2 border-[#D4AF37] flex items-center justify-center shadow-2xl shadow-[#D4AF37]/20 overflow-hidden">
            <img
              src="/logo.png"
              alt="Hyperion Maritime Emblem"
              className="w-full h-full object-cover"
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).src = '/compass.svg';
              }}
            />
          </div>
          <div className="absolute -inset-1.5 rounded-full border border-[#D4AF37]/30 blur-sm pointer-events-none animate-pulse" />
        </div>

        <h1 className="text-3xl sm:text-4xl font-black tracking-widest text-white flex items-center justify-center gap-2">
          <span>HYPERION</span>
        </h1>
        <p className="text-xs sm:text-sm font-bold text-[#D4AF37] tracking-wider uppercase mt-1">
          Philippine MARINA Licensure Examination Reviewer
        </p>
        <p className="text-xs text-slate-400 max-w-md mx-auto mt-2 leading-relaxed">
          Select your examination track to begin. Progress, statistics, and question banks are maintained independently.
        </p>
      </div>

      {/* Tracks Selection with Polished Symbols */}
      <div className="space-y-4 my-2">
        {/* OIC-NW Card */}
        <div
          onClick={() => selectTrack('OIC-NW')}
          className="group bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-[#D4AF37]/70 rounded-2xl p-5 shadow-lg transition-all duration-200 cursor-pointer relative overflow-hidden"
        >
          {/* Subtle background compass watermark */}
          <div className="absolute -right-6 -bottom-6 w-32 h-32 opacity-5 pointer-events-none">
            <Navigation className="w-full h-full text-white" />
          </div>

          <div className="flex items-start justify-between gap-4 mb-3">
            <div className="flex items-center gap-3.5">
              <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#0B192C] to-[#1E3E62] border border-[#D4AF37]/50 flex items-center justify-center text-[#D4AF37] shadow-md group-hover:scale-105 transition-transform">
                <Anchor className="w-7 h-7" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="text-xl font-black text-white group-hover:text-[#D4AF37] transition-colors">
                    OIC-NW
                  </h2>
                  <span className="text-[10px] font-extrabold px-2 py-0.5 rounded-full bg-[#D4AF37]/20 text-[#D4AF37] border border-[#D4AF37]/30">
                    DECK WATCH
                  </span>
                </div>
                <p className="text-xs text-slate-300 mt-0.5">
                  Officer in Charge of a Navigational Watch
                </p>
              </div>
            </div>
            <ChevronRight className="w-5 h-5 text-slate-400 group-hover:text-[#D4AF37] group-hover:translate-x-1 transition-all mt-2" />
          </div>

          <div className="text-xs font-medium text-[#2E86AB] bg-[#0B192C]/70 px-3 py-1.5 rounded-lg mb-3 border border-white/5">
            Functions: F1 (Navigation), F2 (Cargo Handling), F3 (Ship Ops)
          </div>

          {/* Stats Bar */}
          <div className="flex items-center justify-between text-xs text-slate-300 pt-2 border-t border-white/10">
            <span>Bank: <strong className="text-white">{oicStats.total} items</strong></span>
            <span>Reviewed: <strong className="text-white">{oicStats.attempted}/{oicStats.total}</strong></span>
            <span>Accuracy: <strong className="text-emerald-400">{oicStats.accuracy}%</strong></span>
          </div>

          <div className="w-full bg-[#0B192C] h-1.5 rounded-full overflow-hidden mt-2">
            <div
              className="bg-[#D4AF37] h-full rounded-full transition-all duration-300"
              style={{
                width: `${oicStats.total > 0 ? (oicStats.attempted / oicStats.total) * 100 : 0}%`
              }}
            />
          </div>

          <button
            onClick={(e) => {
              e.stopPropagation();
              selectTrack('OIC-NW');
            }}
            className="w-full mt-4 py-2.5 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-extrabold rounded-xl text-xs tracking-wider transition-colors shadow cursor-pointer uppercase flex items-center justify-center gap-2"
          >
            <span>ENTER OIC-NW REVIEWER</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>

        {/* GMDSS Card */}
        <div
          onClick={() => selectTrack('GMDSS')}
          className="group bg-[#0F3460] hover:bg-[#153e74] border border-[#1E3E62] hover:border-[#D4AF37]/70 rounded-2xl p-5 shadow-lg transition-all duration-200 cursor-pointer relative overflow-hidden"
        >
          {/* Subtle background radio watermark */}
          <div className="absolute -right-6 -bottom-6 w-32 h-32 opacity-5 pointer-events-none">
            <Radio className="w-full h-full text-white" />
          </div>

          <div className="flex items-start justify-between gap-4 mb-3">
            <div className="flex items-center gap-3.5">
              <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#0B192C] to-[#1E3E62] border border-[#2E86AB]/60 flex items-center justify-center text-[#2E86AB] shadow-md group-hover:scale-105 transition-transform">
                <Radio className="w-7 h-7" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="text-xl font-black text-white group-hover:text-[#D4AF37] transition-colors">
                    GMDSS
                  </h2>
                  <span className="text-[10px] font-extrabold px-2 py-0.5 rounded-full bg-[#2E86AB]/20 text-[#2E86AB] border border-[#2E86AB]/30">
                    RADIO SAFETY
                  </span>
                </div>
                <p className="text-xs text-slate-300 mt-0.5">
                  Global Maritime Distress and Safety System
                </p>
              </div>
            </div>
            <ChevronRight className="w-5 h-5 text-slate-400 group-hover:text-[#D4AF37] group-hover:translate-x-1 transition-all mt-2" />
          </div>

          <div className="text-xs font-medium text-[#2E86AB] bg-[#0B192C]/70 px-3 py-1.5 rounded-lg mb-3 border border-white/5">
            Subsystems & Radio Services in Emergencies (C1 & C2)
          </div>

          {/* Stats Bar */}
          <div className="flex items-center justify-between text-xs text-slate-300 pt-2 border-t border-white/10">
            <span>Bank: <strong className="text-white">{gmdssStats.total} items</strong></span>
            <span>Reviewed: <strong className="text-white">{gmdssStats.attempted}/{gmdssStats.total}</strong></span>
            <span>Accuracy: <strong className="text-emerald-400">{gmdssStats.accuracy}%</strong></span>
          </div>

          <div className="w-full bg-[#0B192C] h-1.5 rounded-full overflow-hidden mt-2">
            <div
              className="bg-[#2E86AB] h-full rounded-full transition-all duration-300"
              style={{
                width: `${gmdssStats.total > 0 ? (gmdssStats.attempted / gmdssStats.total) * 100 : 0}%`
              }}
            />
          </div>

          <button
            onClick={(e) => {
              e.stopPropagation();
              selectTrack('GMDSS');
            }}
            className="w-full mt-4 py-2.5 bg-[#2E86AB] hover:bg-[#3ca0cb] text-white font-extrabold rounded-xl text-xs tracking-wider transition-colors shadow cursor-pointer uppercase flex items-center justify-center gap-2"
          >
            <span>ENTER GMDSS REVIEWER</span>
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* APK & Offline Guarantee Card */}
      <div className="bg-[#0B192C] border border-[#1E3E62] rounded-2xl p-4 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-400 mt-4 mb-2">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-emerald-950/60 border border-emerald-500/40 flex items-center justify-center text-emerald-400 shrink-0">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <span className="font-bold text-slate-200">100% Offline Standalone Application</span>
            <p className="text-[11px] text-slate-400">
              Install as Android APK or use directly in browser with persistent offline storage.
            </p>
          </div>
        </div>

        <a
          href="/app-debug.apk"
          download="hyperion-marina-reviewer.apk"
          className="shrink-0 flex items-center gap-2 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-black px-4 py-2 rounded-xl text-xs shadow transition-colors cursor-pointer"
        >
          <Download className="w-4 h-4" />
          <span>DOWNLOAD APK</span>
        </a>
      </div>
    </div>
  );
};
