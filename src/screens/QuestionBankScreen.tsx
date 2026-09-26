import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { StudyConfigDialog } from '../components/StudyConfigDialog';
import { OIC_NW_COMPETENCIES, GMDSS_COMPETENCIES, getFunctionTitle } from '../data/competencyMetadata';
import { SlidersHorizontal, Search, X, Play, Shuffle } from 'lucide-react';
import { PracticeArgs } from '../types';

export const QuestionBankScreen: React.FC = () => {
  const {
    currentTrack,
    allQuestions,
    competencyStats,
    navigateBack,
    requestStartPractice
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilterCategory, setSelectedFilterCategory] = useState('ALL');
  const [configDialogState, setConfigDialogState] = useState<{
    title: string;
    subtitle: string;
    totalCount: number;
    competencyCode?: string | null;
    partNumber?: number | null;
  } | null>(null);

  const competencies = useMemo(() => {
    return currentTrack === 'OIC-NW' ? OIC_NW_COMPETENCIES : GMDSS_COMPETENCIES;
  }, [currentTrack]);

  const statsMap = useMemo(() => {
    const map: Record<string, { total: number; attempted: number; mastered: number; accuracy: number }> = {};
    for (const s of competencyStats) {
      const acc = s.attempted > 0 ? Math.round((s.correct / s.attempted) * 100) : 0;
      map[s.competencyCode] = {
        total: s.total,
        attempted: s.attempted,
        mastered: s.mastered,
        accuracy: acc
      };
    }
    return map;
  }, [competencyStats]);

  // Questions grouped by competency
  const questionsByComp = useMemo(() => {
    const map: Record<string, typeof allQuestions> = {};
    allQuestions.forEach((q) => {
      if (!map[q.competencyCode]) map[q.competencyCode] = [];
      map[q.competencyCode].push(q);
    });
    return map;
  }, [allQuestions]);

  // Category filter chips
  const categories = useMemo(() => {
    if (currentTrack === 'OIC-NW') {
      return ['ALL', 'F1', 'F2', 'F3'];
    } else {
      return ['ALL', 'C1', 'C2'];
    }
  }, [currentTrack]);

  // Filter competencies based on category and search query
  const filteredCompetencies = useMemo(() => {
    return competencies.filter((comp) => {
      // Category check
      if (selectedFilterCategory !== 'ALL') {
        if (currentTrack === 'OIC-NW') {
          if (comp.functionCode !== selectedFilterCategory) return false;
        } else {
          if (comp.code !== selectedFilterCategory) return false;
        }
      }

      // Search query check
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase().trim();
        const codeMatch = comp.code.toLowerCase().includes(q);
        const titleMatch = comp.title.toLowerCase().includes(q);
        const funcMatch = comp.functionTitle?.toLowerCase().includes(q) ?? false;
        return codeMatch || titleMatch || funcMatch;
      }

      return true;
    });
  }, [competencies, selectedFilterCategory, searchQuery, currentTrack]);

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title={`${currentTrack} Question Bank`}
        subtitle={`Total: ${allQuestions.length} Questions`}
        onBack={navigateBack}
        actions={
          <button
            onClick={() =>
              setConfigDialogState({
                title: `${currentTrack} Complete Bank`,
                subtitle: 'Practice questions across all competencies',
                totalCount: allQuestions.length
              })
            }
            className="p-2 text-[#D4AF37] hover:text-white rounded-lg hover:bg-white/10 transition-colors cursor-pointer"
            title="Configure Study"
          >
            <SlidersHorizontal className="w-5 h-5" />
          </button>
        }
      />

      <div className="max-w-4xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-4">
        {/* Search Bar */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search competencies or topics..."
            className="w-full bg-[#0F3460] border border-[#1E3E62] focus:border-[#2E86AB] rounded-xl pl-10 pr-10 py-2.5 text-sm text-white placeholder-slate-400 outline-none transition-colors"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white p-1"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Filter Categories Chips */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedFilterCategory(cat)}
              className={`text-xs px-3.5 py-1.5 rounded-full border transition-colors cursor-pointer font-bold shrink-0 ${
                selectedFilterCategory === cat
                  ? 'bg-[#D4AF37] text-[#0B192C] border-[#D4AF37] shadow'
                  : 'bg-[#0F3460] text-slate-300 border-[#1E3E62] hover:border-slate-500'
              }`}
            >
              {cat === 'ALL' ? 'All Functions' : getFunctionTitle(cat)}
            </button>
          ))}
        </div>

        {/* Competencies List */}
        <div className="space-y-4 pt-2">
          {filteredCompetencies.map((comp) => {
            const compQuestions = questionsByComp[comp.code] || [];
            const count = compQuestions.length;
            const stat = statsMap[comp.code] || {
              total: count,
              attempted: 0,
              mastered: 0,
              accuracy: 0
            };
            const progress = count > 0 ? Math.round((stat.attempted / count) * 100) : 0;

            // Find unique parts in this competency
            const uniqueParts = Array.from(
              new Set(compQuestions.map((q) => q.partNumber).filter((p): p is number => p != null))
            ).sort((a, b) => a - b);

            return (
              <div
                key={comp.code}
                className="bg-[#0F3460] border border-[#1E3E62] hover:border-[#2E86AB] rounded-2xl p-5 shadow transition-colors"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-black bg-[#D4AF37] text-[#0B192C] px-2 py-0.5 rounded-md">
                        {comp.code}
                      </span>
                      {comp.functionCode && (
                        <span className="text-xs font-semibold text-[#2E86AB]">
                          {comp.functionCode}
                        </span>
                      )}
                      <span className="text-xs text-slate-400">
                        {count} questions (Official quota: {comp.examQuota})
                      </span>
                    </div>

                    <h3 className="text-base font-bold text-white mt-1.5 leading-snug">
                      {comp.title}
                    </h3>
                  </div>

                  <div className="text-right shrink-0">
                    <span
                      className={`text-xs font-bold px-2 py-0.5 rounded-lg ${
                        stat.accuracy >= 70
                          ? 'bg-emerald-900/60 text-emerald-300 border border-emerald-600/40'
                          : 'bg-[#0B192C] text-slate-400'
                      }`}
                    >
                      {stat.accuracy}% ACC
                    </span>
                  </div>
                </div>

                {/* Progress bar */}
                <div className="space-y-1.5 my-3">
                  <div className="flex justify-between text-xs text-slate-400">
                    <span>
                      Reviewed: <strong className="text-slate-200">{stat.attempted}/{count}</strong> • Mastered: <strong className="text-emerald-400">{stat.mastered}</strong>
                    </span>
                    <span>{progress}%</span>
                  </div>
                  <div className="w-full bg-[#0B192C] h-2 rounded-full overflow-hidden">
                    <div
                      className="bg-[#2E86AB] h-full rounded-full transition-all duration-300"
                      style={{ width: `${progress}%` }}
                    />
                  </div>
                </div>

                {/* Sub-Parts Drilldown (if questions have parts) */}
                {uniqueParts.length > 0 && (
                  <div className="my-3 pt-2 border-t border-white/5">
                    <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-1.5">
                      Sub-Parts:
                    </span>
                    <div className="flex flex-wrap gap-1.5">
                      {uniqueParts.map((p) => {
                        const partCount = compQuestions.filter((q) => q.partNumber === p).length;
                        return (
                          <button
                            key={p}
                            onClick={() =>
                              setConfigDialogState({
                                title: `${comp.code} Part ${p}`,
                                subtitle: comp.title,
                                totalCount: partCount,
                                competencyCode: comp.code,
                                partNumber: p
                              })
                            }
                            className="text-xs bg-[#0B192C] hover:bg-[#1E3E62] border border-[#1E3E62] text-slate-300 px-2.5 py-1 rounded-lg transition-colors cursor-pointer"
                          >
                            Part {p} ({partCount})
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Action Buttons */}
                <div className="flex items-center gap-2 mt-4 pt-3 border-t border-white/10">
                  <button
                    onClick={() =>
                      setConfigDialogState({
                        title: `${comp.code} - ${comp.title}`,
                        subtitle: `${currentTrack} Competency Review`,
                        totalCount: count,
                        competencyCode: comp.code
                      })
                    }
                    className="flex-1 py-2 px-3 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer shadow"
                  >
                    <Play className="w-3.5 h-3.5 fill-current" />
                    <span>STUDY ({count})</span>
                  </button>

                  <button
                    onClick={() =>
                      requestStartPractice({
                        title: `${comp.code} (Blitz 25)`,
                        filterMode: 'ALL',
                        competencyCode: comp.code,
                        itemLimit: Math.min(25, count > 0 ? count : 25),
                        isRandomized: true,
                        shuffleOptions: true
                      })
                    }
                    className="py-2 px-3 bg-[#1E3E62] hover:bg-[#2E86AB] text-white font-semibold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                  >
                    <Shuffle className="w-3.5 h-3.5 text-[#D4AF37]" />
                    <span>RANDOM 25</span>
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Study Configuration Dialog */}
      {configDialogState && (
        <StudyConfigDialog
          title={configDialogState.title}
          subtitle={configDialogState.subtitle}
          totalAvailable={configDialogState.totalCount}
          competencyCode={configDialogState.competencyCode}
          partNumber={configDialogState.partNumber}
          onDismiss={() => setConfigDialogState(null)}
          onStart={(args) => {
            setConfigDialogState(null);
            requestStartPractice(args);
          }}
        />
      )}
    </div>
  );
};
