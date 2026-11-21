import React from 'react';
import { ProblemDistributionResponse } from '@/types/codingIntelligence';
import { CheckCircle, Info, TrendingUp } from 'lucide-react';

interface ProblemDistributionSectionProps {
  problemData?: ProblemDistributionResponse;
  isLoading: boolean;
}

export const ProblemDistributionSection: React.FC<ProblemDistributionSectionProps> = ({
  problemData,
  isLoading,
}) => {
  if (isLoading) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm animate-pulse h-64 flex items-center justify-center">
        <div className="flex items-center gap-2 text-surface-500">
          <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Loading problem distributions...</span>
        </div>
      </div>
    );
  }

  const total = problemData?.totalPlatformReportedSolved || 0;
  const easy = problemData?.easySolved || 0;
  const medium = problemData?.mediumSolved || 0;
  const hard = problemData?.hardSolved || 0;
  const unclassified = problemData?.unclassifiedSolved || 0;
  const categorizedTotal = easy + medium + hard;

  const easyPct = categorizedTotal > 0 ? (easy / categorizedTotal) * 100 : 0;
  const medPct = categorizedTotal > 0 ? (medium / categorizedTotal) * 100 : 0;
  const hardPct = categorizedTotal > 0 ? (hard / categorizedTotal) * 100 : 0;

  const delta = problemData?.solvedDeltaInPeriod;

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-emerald-500" />
            Problem-Solving Distribution & Difficulty
          </h3>
          <p className="text-xs text-surface-500">
            Categorized solved counts based on latest platform-reported statistics
          </p>
        </div>

        {delta !== undefined && delta !== null && delta > 0 && (
          <span className="self-start sm:self-auto flex items-center gap-1.5 text-xs font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-3 py-1 rounded-xl">
            <TrendingUp className="w-4 h-4" />
            +{delta} solved in period
          </span>
        )}
      </div>

      {/* Difficulty Stacked Bar & Metric Cards */}
      <div className="space-y-3">
        <div className="h-4 rounded-full bg-surface-100 dark:bg-surface-800 flex overflow-hidden p-0.5 gap-0.5">
          {easyPct > 0 && (
            <div
              style={{ width: `${easyPct}%` }}
              className="bg-emerald-500 rounded-l-full transition-all duration-500"
              title={`Easy: ${easy} (${easyPct.toFixed(1)}%)`}
            />
          )}
          {medPct > 0 && (
            <div
              style={{ width: `${medPct}%` }}
              className="bg-amber-500 transition-all duration-500"
              title={`Medium: ${medium} (${medPct.toFixed(1)}%)`}
            />
          )}
          {hardPct > 0 && (
            <div
              style={{ width: `${hardPct}%` }}
              className="bg-red-500 rounded-r-full transition-all duration-500"
              title={`Hard: ${hard} (${hardPct.toFixed(1)}%)`}
            />
          )}
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
            <span className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400 uppercase">
              Easy Solved
            </span>
            <div className="flex items-baseline gap-2 mt-1">
              <span className="text-xl font-bold text-surface-900 dark:text-white">{easy}</span>
              <span className="text-xs text-surface-500">({easyPct.toFixed(0)}%)</span>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
            <span className="text-[11px] font-semibold text-amber-600 dark:text-amber-400 uppercase">
              Medium Solved
            </span>
            <div className="flex items-baseline gap-2 mt-1">
              <span className="text-xl font-bold text-surface-900 dark:text-white">{medium}</span>
              <span className="text-xs text-surface-500">({medPct.toFixed(0)}%)</span>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
            <span className="text-[11px] font-semibold text-red-600 dark:text-red-400 uppercase">
              Hard Solved
            </span>
            <div className="flex items-baseline gap-2 mt-1">
              <span className="text-xl font-bold text-surface-900 dark:text-white">{hard}</span>
              <span className="text-xs text-surface-500">({hardPct.toFixed(0)}%)</span>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
            <span className="text-[11px] font-semibold text-surface-500 uppercase">
              Total Reported
            </span>
            <div className="flex items-baseline gap-2 mt-1">
              <span className="text-xl font-bold text-surface-900 dark:text-white">{total}</span>
              {unclassified > 0 && (
                <span className="text-[10px] text-surface-400" title="Solved problems without tier breakdown (e.g., Codeforces)">
                  +{unclassified} unclassified
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Platform Breakdown Table */}
      <div className="space-y-2 pt-2 border-t border-surface-100 dark:border-surface-800">
        <h4 className="text-xs font-semibold text-surface-700 dark:text-surface-300 uppercase tracking-wider">
          Platform-Wise Breakdown
        </h4>
        <div className="divide-y divide-surface-100 dark:divide-surface-800 border border-surface-200/60 dark:border-surface-800 rounded-xl overflow-hidden">
          {(problemData?.platformBreakdowns || []).map((p) => (
            <div
              key={p.platform}
              className="p-3 bg-surface-50/50 dark:bg-surface-900/30 flex items-center justify-between text-xs"
            >
              <div className="flex items-center gap-3">
                <span className="font-mono font-bold text-surface-900 dark:text-white uppercase">
                  {p.platform}
                </span>
                <span className="text-surface-500 text-[11px]">@{p.username}</span>
              </div>

              <div className="flex items-center gap-6">
                {p.easySolved !== null && (
                  <span className="text-emerald-600 dark:text-emerald-400 hidden sm:inline">
                    E: {p.easySolved}
                  </span>
                )}
                {p.mediumSolved !== null && (
                  <span className="text-amber-600 dark:text-amber-400 hidden sm:inline">
                    M: {p.mediumSolved}
                  </span>
                )}
                {p.hardSolved !== null && (
                  <span className="text-red-600 dark:text-red-400 hidden sm:inline">
                    H: {p.hardSolved}
                  </span>
                )}
                <div className="text-right">
                  <span className="font-bold text-surface-900 dark:text-white">
                    {p.totalSolved} solved
                  </span>
                  <span className="text-[11px] text-surface-400 ml-1">
                    ({p.percentageOfTotal}%)
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Deduplication Invariant Notice */}
      <div className="p-3 rounded-xl bg-blue-50/60 dark:bg-blue-950/20 border border-blue-200/60 dark:border-blue-900/40 flex items-start gap-2 text-xs text-blue-700 dark:text-blue-300">
        <Info className="w-4 h-4 shrink-0 mt-0.5" />
        <span className="text-[11px] leading-relaxed">
          {problemData?.deduplicationNote ||
            'Cross-platform totals represent the sum of individual platform-reported counts. Problem-level deduplication across platforms requires problem-specific identifier tracking.'}
        </span>
      </div>
    </div>
  );
};
