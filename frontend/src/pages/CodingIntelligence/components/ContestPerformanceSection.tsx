import React from 'react';
import { ContestPerformanceResponse } from '@/types/codingIntelligence';
import { Award, Trophy, Users } from 'lucide-react';

interface ContestPerformanceSectionProps {
  contestData?: ContestPerformanceResponse;
  isLoading: boolean;
}

export const ContestPerformanceSection: React.FC<ContestPerformanceSectionProps> = ({
  contestData,
  isLoading,
}) => {
  if (isLoading) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm animate-pulse h-56 flex items-center justify-center">
        <div className="flex items-center gap-2 text-surface-500">
          <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Loading contest statistics...</span>
        </div>
      </div>
    );
  }

  const contestPlatforms = (contestData?.platformContests || []).filter((p) => p.contestsSupported);

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
            <Award className="w-5 h-5 text-amber-500" />
            Contest Performance & Competitive Rounds
          </h3>
          <p className="text-xs text-surface-500">
            Rated rounds participated across Codeforces, LeetCode, CodeChef, and AtCoder
          </p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20 flex items-center gap-1.5">
            <Trophy className="w-3.5 h-3.5" />
            {contestData?.totalContestsParticipated ?? 0} Total Rated Contests
          </span>
        </div>
      </div>

      {contestPlatforms.length === 0 ? (
        <div className="py-8 text-center bg-surface-50 dark:bg-surface-900/30 rounded-xl border border-dashed border-surface-200 dark:border-surface-800">
          <Users className="w-8 h-8 text-surface-400 mx-auto mb-2 opacity-50" />
          <p className="text-xs text-surface-500">
            No connected platforms support live rated contests, or no contest participation has been recorded yet.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {contestPlatforms.map((c) => (
            <div
              key={c.platform}
              className="p-4 rounded-xl border border-surface-200/80 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/30 space-y-2"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold font-mono text-surface-900 dark:text-white uppercase">
                  {c.platform}
                </span>
                <span className="text-[10px] text-surface-500">@{c.username}</span>
              </div>

              <div className="pt-1 grid grid-cols-2 gap-2 text-xs">
                <div>
                  <span className="text-[10px] text-surface-500 uppercase font-semibold block">Contests</span>
                  <span className="text-lg font-bold text-surface-900 dark:text-white">
                    {c.contestsParticipated ?? '—'}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-surface-500 uppercase font-semibold block">Peak Rating</span>
                  <span className="text-lg font-bold text-indigo-600 dark:text-indigo-400">
                    {c.bestRating !== null && c.bestRating !== undefined ? Math.round(c.bestRating) : '—'}
                  </span>
                </div>
              </div>

              {c.ratingChange !== null && c.ratingChange !== undefined && (
                <div className="pt-2 border-t border-surface-200/50 dark:border-surface-800/80 flex items-center justify-between text-[11px]">
                  <span className="text-surface-500">Period Trend:</span>
                  <span
                    className={`font-semibold ${
                      c.ratingChange > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-500'
                    }`}
                  >
                    {c.ratingChange > 0 ? `+${c.ratingChange}` : c.ratingChange} pts
                  </span>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
