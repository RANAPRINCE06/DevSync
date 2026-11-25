import React from 'react';
import {
  CheckCircle2,
  Flame,
  Github,
  Activity,
  ShieldCheck,
  AlertTriangle,
  Clock,
  Sparkles,
} from 'lucide-react';
import { CodingAnalyticsOverview, PlatformAccount } from '@/types/codingIntelligence';

interface OverviewSectionProps {
  overview?: CodingAnalyticsOverview;
  accounts: PlatformAccount[];
  isLoading: boolean;
}

export const OverviewSection: React.FC<OverviewSectionProps> = ({
  overview,
  accounts,
  isLoading,
}) => {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 animate-pulse">
        {[1, 2, 3, 4].map((i) => (
          <div
            key={i}
            className="h-24 rounded-[3px] bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 space-y-2 shadow-xs"
          >
            <div className="h-3 bg-slate-200 dark:bg-slate-800 rounded-[2px] w-1/3"></div>
            <div className="h-6 bg-slate-200 dark:bg-slate-800 rounded-[2px] w-1/2"></div>
          </div>
        ))}
      </div>
    );
  }

  const activeAutoSyncAccounts = accounts.filter(
    (a) => a.active && (a.platform === 'LEETCODE' || a.platform === 'CODEFORCES' || a.platform === 'GITHUB' || a.platform === 'ATCODER' || a.platform === 'GEEKSFORGEEKS')
  );

  const syncHealth = overview?.syncHealthStatus || 'NORMAL';
  const hasAttention = syncHealth === 'ATTENTION_NEEDED';

  return (
    <div className="space-y-4">
      {/* Top Banner Notice: Platform Solved Definitions */}
      <div className="p-3 rounded-[3px] bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 text-xs text-slate-600 dark:text-slate-300 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 shadow-xs">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-blue-600 dark:text-blue-400 shrink-0" />
          <span>
            <strong className="text-slate-900 dark:text-slate-100">Coding Intelligence Overview:</strong> Aggregated from verified external platform snapshots. Solved counts represent platform-reported totals.
          </span>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <span
            className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-[2px] text-[11px] font-semibold border ${
              hasAttention
                ? 'bg-amber-50 dark:bg-amber-950/40 text-amber-700 dark:text-amber-400 border-amber-200 dark:border-amber-800'
                : 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800'
            }`}
          >
            {hasAttention ? (
              <AlertTriangle className="w-3 h-3" />
            ) : (
              <ShieldCheck className="w-3 h-3" />
            )}
            {hasAttention ? 'Sync Attention Needed' : 'Sync Engine Healthy'}
          </span>
        </div>
      </div>

      {/* Primary KPI Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        {/* Total Solved Card */}
        <div className="p-3.5 rounded-[3px] bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 shadow-xs relative overflow-hidden">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">Platform-Reported Solved</span>
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 flex items-baseline gap-1.5">
            {overview?.totalProblemsSolved ?? '—'}
            <span className="text-xs font-normal text-slate-400">problems</span>
          </div>
          <p className="text-[10px] text-slate-400 mt-1" title="Aggregated from LeetCode, Codeforces, AtCoder, and GeeksforGeeks snapshots">
            Sum of platform-reported solutions across active accounts
          </p>
        </div>

        {/* Peak CP Rating Card */}
        <div className="p-3.5 rounded-[3px] bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 shadow-xs relative overflow-hidden">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">Peak Contest Rating</span>
            <Flame className="w-3.5 h-3.5 text-amber-500" />
          </div>
          <div className="text-xl font-bold text-blue-600 dark:text-blue-400 flex items-baseline gap-1.5">
            {overview?.highestRating ? Math.round(overview.highestRating) : '—'}
            {overview?.highestRatingPlatform && (
              <span className="text-[10px] font-semibold px-1.5 py-0.2 rounded-[2px] bg-blue-50 dark:bg-blue-950 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800 uppercase">
                {overview.highestRatingPlatform}
              </span>
            )}
          </div>
          <p className="text-[10px] text-slate-400 mt-1" title="Competitive ratings (Elo, Glicko-2) are platform-specific and not cross-comparable">
            Platform-specific best (Codeforces / LeetCode scale)
          </p>
        </div>

        {/* GitHub Repos & Stars */}
        <div className="p-3.5 rounded-[3px] bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 shadow-xs relative overflow-hidden">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">GitHub Ecosystem</span>
            <Github className="w-3.5 h-3.5 text-purple-500" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <span title="Public repositories">
              {overview?.totalGitHubRepositories != null ? overview.totalGitHubRepositories : '—'}
              <span className="text-xs font-normal text-slate-400 ml-1">repos</span>
            </span>
            <span className="text-slate-300 dark:text-slate-700">|</span>
            <span className="text-purple-600 dark:text-purple-400 flex items-center gap-1 text-sm font-bold" title="Total repository stars">
              ★ {overview?.totalGitHubStars != null ? overview.totalGitHubStars : '0'}
            </span>
          </div>
          <p className="text-[10px] text-slate-400 mt-1">
            Public repositories and total stargazer recognition
          </p>
        </div>

        {/* Sync & Activity Summary */}
        <div className="p-3.5 rounded-[3px] bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 shadow-xs relative overflow-hidden">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">Sync & Coverage</span>
            <Activity className="w-3.5 h-3.5 text-blue-500" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 flex items-baseline gap-1.5">
            {activeAutoSyncAccounts.length} / {accounts.length}
            <span className="text-xs font-normal text-slate-400">auto-synced</span>
          </div>
          <div className="flex items-center gap-1.5 text-[10px] text-slate-400 mt-1">
            <Clock className="w-3 h-3" />
            <span>
              Latest:{' '}
              {overview?.latestSuccessfulSyncAt
                ? new Date(overview.latestSuccessfulSyncAt).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                  })
                : 'Pending initial sync'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
