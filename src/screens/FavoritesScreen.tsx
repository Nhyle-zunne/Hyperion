import React from 'react';
import { useApp } from '../context/AppContext';
import { HyperionTopBar } from '../components/HyperionTopBar';
import { Star, Play, StarOff } from 'lucide-react';

export const FavoritesScreen: React.FC = () => {
  const { currentTrack, favorites, navigateBack, toggleFavorite, requestStartPractice } = useApp();

  return (
    <div className="min-h-screen bg-[#070F1B] text-white flex flex-col pb-12">
      <HyperionTopBar
        title="My Favorites"
        subtitle={`${currentTrack} • ${favorites.length} Starred`}
        onBack={navigateBack}
      />

      <div className="max-w-3xl mx-auto w-full px-4 sm:px-6 pt-4 space-y-4">
        {favorites.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <Star className="w-16 h-16 text-slate-600 mb-3" />
            <h3 className="text-lg font-bold">No Starred Questions</h3>
            <p className="text-xs text-slate-400 mt-1 max-w-sm">
              Star important questions during practice or review to bookmark them here for quick study.
            </p>
          </div>
        ) : (
          <>
            {/* Top Action */}
            <div className="flex items-center justify-between bg-[#0F3460] p-4 rounded-2xl border border-[#1E3E62] shadow">
              <div>
                <h3 className="text-sm font-bold text-white">Review Starred Collection</h3>
                <p className="text-xs text-slate-300">{favorites.length} questions saved</p>
              </div>
              <button
                onClick={() =>
                  requestStartPractice({
                    title: `${currentTrack} - Starred Favorites`,
                    filterMode: 'FAVORITES'
                  })
                }
                className="py-2.5 px-4 bg-[#D4AF37] hover:bg-[#F3C64F] text-[#0B192C] font-bold text-xs rounded-xl flex items-center gap-2 shadow cursor-pointer uppercase tracking-wider"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>PRACTICE ALL</span>
              </button>
            </div>

            {/* List */}
            <div className="space-y-3">
              {favorites.map((q, idx) => (
                <div
                  key={q.id}
                  className="bg-[#0F3460] border border-[#1E3E62] rounded-2xl p-4 shadow space-y-2 relative"
                >
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-extrabold text-[#D4AF37] bg-[#0B192C] px-2 py-0.5 rounded">
                      {q.competencyCode}
                    </span>
                    <button
                      onClick={() => toggleFavorite(q)}
                      className="text-[#D4AF37] hover:text-slate-400 p-1 rounded-lg cursor-pointer"
                      title="Remove from favorites"
                    >
                      <Star className="w-5 h-5 fill-current" />
                    </button>
                  </div>

                  <h4 className="text-sm font-bold text-white leading-relaxed">{q.questionText}</h4>

                  <div className="text-xs text-slate-400">
                    Correct Answer: <strong className="text-emerald-400">Option {q.correctAnswerLetter}</strong>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
};
