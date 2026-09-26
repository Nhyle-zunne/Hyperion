import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { ExamRecord } from '../types';
import * as db from '../db/indexedDb';
import { getCompetencyTitle } from '../data/competencyMetadata';
import { BarChart3, Award, FileCheck, CheckCircle2, ChevronRight } from 'lucide-react';

export const StatsScreen: React.FC = () => {
  const { currentTrack, allQuestions, competencyStats, navigateBack, navigateTo, setActiveRecordId } = useApp();

  const [examRecords, setExamRecords] = useState<ExamRecord[]>([]);

  useEffect(() => {
    (async () => {
      const records = await db.getExamRecords(currentTrack);
      setExamRecords(records);
    })();
  }, [currentTrack]);

  const total = allQuestions.length;
  const attempted = allQuestions.filter((q) => q.timesAttempted > 0).length;
  const mastered = allQuestions.filter((q) => q.masteryStatus === 'MASTERED').length;
  const improving = allQuestions.filter((q) => q.masteryStatus === 'IMPROVING').length;
  const needsReview = allQuestions.filter((q) => q.masteryStatus === 'NEEDS_REVIEW' || q.timesIncorrect > 0).length;
  const notAttempted = allQuestions.filter((q) => q.timesAttempted === 0).length;

  const sumAttempted = allQuestions.reduce((acc, q) => acc + q.timesAttempted, 0);
  const sumCorrect = allQuestions.reduce((acc, q) => acc + q.timesCorrect, 0);
  const accuracy = sumAttempted > 0 ? Math.round((sumCorrect / sumAttempted) * 100) : 0;

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="Analytics & Mastery"
        subtitle={`${currentTrack} Performance`}
        onBack={navigateBack}
      />

      <div className="max-w-3xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-5">
        {/* Overview Mastery Card */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 sm:p-6 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-xs font-bold text-[#D4AF37] uppercase tracking-wider">
                Overall Progress
              </div>
              <h2 className="text-lg font-bold text-white mt-0.5">Mastery Breakdown</h2>
            </div>
            <div className="text-right">
              <span className="text-2xl font-black text-[#D4AF37]">{accuracy}%</span>
              <span className="text-xs text-slate-400 block">Overall Accuracy</span>
            </div>
          </div>

          {/* Grid Stats */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-2">
            <div className="bg-[#0B192C] p-3 rounded-xl border border-white/5">
              <div className="text-lg font-black text-white">{attempted} / {total}</div>
              <div className="text-[11px] text-slate-400">Total Reviewed</div>
            </div>
            <div className="bg-[#0B192C] p-3 rounded-xl border border-white/5">
              <div className="text-lg font-black text-emerald-400">{mastered}</div>
              <div className="text-[11px] text-slate-400">Mastered (3+ Correct)</div>
            </div>
            <div className="bg-[#0B192C] p-3 rounded-xl border border-white/5">
              <div className="text-lg font-black text-[#2E86AB]">{improving}</div>
              <div className="text-[11px] text-slate-400">Improving</div>
            </div>
            <div className="bg-[#0B192C] p-3 rounded-xl border border-white/5">
              <div className="text-lg font-black text-red-400">{needsReview}</div>
              <div className="text-[11px] text-slate-400">Needs Review</div>
            </div>
          </div>

          <div className="pt-2 text-xs text-slate-400">
            Total Answer Submissions: <strong className="text-white">{sumAttempted}</strong> ({sumCorrect} correct)
          </div>
        </div>

        {/* Competency Mastery List */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
          <h3 className="text-xs font-bold text-[#D4AF37] tracking-wider uppercase">
            Competency Mastery Levels
          </h3>

          <div className="space-y-3">
            {competencyStats.map((comp) => {
              const compAcc = comp.attempted > 0 ? Math.round((comp.correct / comp.attempted) * 100) : 0;
              const coverage = comp.total > 0 ? Math.round((comp.attempted / comp.total) * 100) : 0;

              return (
                <div key={comp.competencyCode} className="bg-[#0B192C] p-3.5 rounded-xl border border-white/5 space-y-2">
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-bold text-white">
                      <strong className="text-[#D4AF37] mr-1.5">{comp.competencyCode}</strong>
                      {getCompetencyTitle(currentTrack, comp.competencyCode)}
                    </span>
                    <span className="text-slate-300 font-semibold shrink-0 ml-2">
                      {compAcc}% Acc ({comp.mastered} Mastered)
                    </span>
                  </div>

                  <div className="w-full bg-[#070F1B] h-1.5 rounded-full overflow-hidden">
                    <div
                      className="bg-gradient-to-r from-[#2E86AB] to-emerald-400 h-full rounded-full"
                      style={{ width: `${coverage}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Past Mock Exam Records */}
        <div className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-5 shadow space-y-3">
          <h3 className="text-xs font-bold text-[#D4AF37] tracking-wider uppercase">
            Simulated Exam History
          </h3>

          {examRecords.length === 0 ? (
            <p className="text-xs text-slate-400 py-4 text-center">
              No completed exam records yet. Take a mock exam to record your history!
            </p>
          ) : (
            <div className="space-y-2.5">
              {examRecords.map((rec) => (
                <div
                  key={rec.id}
                  onClick={() => {
                    setActiveRecordId(rec.id);
                    navigateTo('ExamReview');
                  }}
                  className="bg-[#0B192C] hover:bg-[#1E3E62] border border-white/5 hover:border-[#2E86AB] rounded-xl p-3.5 flex items-center justify-between gap-3 transition-colors cursor-pointer"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase ${
                          rec.passed
                            ? 'bg-emerald-900/60 text-emerald-300 border border-emerald-500/40'
                            : 'bg-red-900/60 text-red-300 border border-red-500/40'
                        }`}
                      >
                        {rec.passed ? 'PASSED' : 'FAILED'}
                      </span>
                      <span className="text-xs text-slate-400">
                        {new Date(rec.timestamp).toLocaleDateString()}
                      </span>
                    </div>

                    <h4 className="font-bold text-sm text-white mt-1 truncate">{rec.title}</h4>
                    <p className="text-xs text-slate-400">
                      Score: {rec.score} / {rec.totalQuestions} ({rec.percentage}%) • Time: {Math.round(rec.timeUsedSeconds / 60)}m
                    </p>
                  </div>

                  <ChevronRight className="w-5 h-5 text-slate-400 shrink-0" />
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
