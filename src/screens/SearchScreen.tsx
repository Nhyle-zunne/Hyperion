import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { Search, X, Play } from 'lucide-react';

export const SearchScreen: React.FC = () => {
  const { currentTrack, allQuestions, navigateBack, requestStartPractice } = useApp();
  const [query, setQuery] = useState('');
  const [selectedComp, setSelectedComp] = useState<string>('ALL');

  const competencies = useMemo(() => {
    return Array.from(new Set(allQuestions.map((q) => q.competencyCode))).sort();
  }, [allQuestions]);

  const searchResults = useMemo(() => {
    if (!query.trim() && selectedComp === 'ALL') {
      return allQuestions.slice(0, 30); // show initial preview
    }

    const q = query.toLowerCase().trim();
    return allQuestions.filter((item) => {
      if (selectedComp !== 'ALL' && item.competencyCode !== selectedComp) {
        return false;
      }

      if (!q) return true;

      return (
        item.questionText.toLowerCase().includes(q) ||
        item.optionA.toLowerCase().includes(q) ||
        item.optionB.toLowerCase().includes(q) ||
        (item.optionC && item.optionC.toLowerCase().includes(q)) ||
        (item.optionD && item.optionD.toLowerCase().includes(q)) ||
        (item.explanation && item.explanation.toLowerCase().includes(q)) ||
        item.competencyCode.toLowerCase().includes(q) ||
        (item.section && item.section.toLowerCase().includes(q))
      );
    });
  }, [allQuestions, query, selectedComp]);

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="Search Master Bank"
        subtitle={`${currentTrack} • Offline Lookup`}
        onBack={navigateBack}
      />

      <div className="max-w-3xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-4">
        {/* Search Input */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type keywords (e.g., radar, COLREGS, NAVTEX, hydrostatic, stowage)..."
            className="w-full bg-[#0F3460] border border-[#1E3E62] focus:border-[#2E86AB] rounded-xl pl-10 pr-10 py-3 text-sm text-white placeholder-slate-400 outline-none transition-colors"
          />
          {query && (
            <button
              onClick={() => setQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white p-1"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Competency Filter Chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
          <button
            onClick={() => setSelectedComp('ALL')}
            className={`text-xs px-3 py-1.5 rounded-full border transition-colors cursor-pointer font-bold shrink-0 ${
              selectedComp === 'ALL'
                ? 'bg-[#D4AF37] text-[#0B192C] border-[#D4AF37]'
                : 'bg-[#0F3460] text-slate-300 border-[#1E3E62]'
            }`}
          >
            All Competencies
          </button>
          {competencies.map((c) => (
            <button
              key={c}
              onClick={() => setSelectedComp(c)}
              className={`text-xs px-3 py-1.5 rounded-full border transition-colors cursor-pointer font-bold shrink-0 ${
                selectedComp === c
                  ? 'bg-[#D4AF37] text-[#0B192C] border-[#D4AF37]'
                  : 'bg-[#0F3460] text-slate-300 border-[#1E3E62]'
              }`}
            >
              {c}
            </button>
          ))}
        </div>

        {/* Results Header */}
        <div className="flex items-center justify-between pt-1">
          <span className="text-xs text-slate-400">
            Found <strong className="text-white">{searchResults.length}</strong> matching questions
          </span>

          {searchResults.length > 0 && (
            <button
              onClick={() =>
                requestStartPractice({
                  title: query ? `Search: "${query}"` : `${currentTrack} Filtered Practice`,
                  customQuestionIds: searchResults.map((q) => q.id)
                })
              }
              className="py-1.5 px-3 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-bold text-xs rounded-xl flex items-center gap-1.5 shadow cursor-pointer uppercase"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>Practice Results ({searchResults.length})</span>
            </button>
          )}
        </div>

        {/* Results List */}
        <div className="space-y-3">
          {searchResults.map((q) => (
            <div
              key={q.id}
              className="bg-[#0F3460] border border-[#1E3E62] hover:border-[#2E86AB] rounded-2xl p-4 shadow space-y-2"
            >
              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span className="font-extrabold text-[#D4AF37] bg-[#0B192C] px-2 py-0.5 rounded">
                    {q.competencyCode}
                  </span>
                  {q.partNumber && <span className="text-slate-400">Part {q.partNumber}</span>}
                </div>
                <span className="text-xs text-slate-400">
                  Key: <strong className="text-emerald-400">Option {q.correctAnswerLetter}</strong>
                </span>
              </div>

              <h4 className="text-sm font-bold text-white leading-relaxed">{q.questionText}</h4>

              {q.explanation && (
                <p className="text-xs text-slate-300 leading-relaxed bg-[#0B192C] p-2.5 rounded-lg border border-white/5">
                  {q.explanation}
                </p>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
