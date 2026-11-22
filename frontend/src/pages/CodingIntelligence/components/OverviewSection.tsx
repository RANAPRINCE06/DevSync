import React from 'react';
import {
  CheckCircle2,
  Flame,
  Github,
  Star,
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
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
        {[1, 2, 3, 4].map((i) => (
          <div
            key={i}
            className="h-28 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 p-5 space-y-3"
          >
            <div className="h-4 bg-surface-200 dark:bg-surface-800 rounded w-1/3"></div>
            <div className="h-7 bg-surface-200 dark:bg-surface-800 rounded w-1/2"></div>
          </div>
        ))}
      </div>
    );
  }

  const activeAutoSyncAccounts = accounts.filter(
    (a) => a.active && (a.platform === 'LEETCODE' || a.platform === 'CODEFORCES' || a.platform === 'GITHUB' || a.platform === 'ATCODER')
  );

  const syncHealth = overview?.syncHealthStatus || 'NORMAL';
  const hasAttention = syncHealth === 'ATTENTION_NEEDED';

  return (
    <div className="space-y-4">
      {/* Top Banner Notice: Platform Solved Definitions */}
      <div className="p-3.5 rounded-xl bg-gradient-to-r from-primary-500/10 via-surface-100 dark:via-surface-900 to-indigo-500/10 border border-primary-500/20 text-xs text-surface-600 dark:text-surface-300 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-primary-500 shrink-0" />
          <span>
            <strong>Coding Intelligence Overview:</strong> Aggregated from verified external platform snapshots. Solved counts represent platform-reported totals.
          </span>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <span
            className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold border ${
              hasAttention
                ? 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20'
                : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20'
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
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Solved Card */}
        <div className="p-5 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm relative overflow-hidden group hover:border-emerald-500/30 transition-all">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Platform-Reported Solved</span>
            <div className="p-2 rounded-xl bg-emerald-500/10 text-emerald-500">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white flex items-baseline gap-2">
            {overview?.totalProblemsSolved ?? '—'}
            <span className="text-xs font-normal text-surface-400">problems</span>
          </div>
          <p className="text-[11px] text-surface-500 mt-1.5" title="Aggregated from LeetCode, Codeforces, and AtCoder snapshots">
            Sum of platform-reported solutions across active accounts
          </p>
        </div>

        {/* Peak CP Rating Card */}
        <div className="p-5 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm relative overflow-hidden group hover:border-indigo-500/30 transition-all">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Peak Contest Rating</span>
            <div className="p-2 rounded-xl bg-indigo-500/10 text-indigo-500">
              <Flame className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white flex items-baseline gap-2">
            {overview?.highestRating ? Math.round(overview.highestRating) : '—'}
            {overview?.highestRatingPlatform && (
              <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-500 uppercase">
                {overview.highestRatingPlatform}
              </span>
            )}
          </div>
          <p className="text-[11px] text-surface-500 mt-1.5" title="Competitive ratings (Elo, Glicko-2) are platform-specific and not cross-comparable">
            Platform-specific best (Codeforces / LeetCode scale)
          </p>
        </div>

        {/* GitHub Repos & Stars */}
        <div className="p-5 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm relative overflow-hidden group hover:border-purple-500/30 transition-all">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">GitHub Ecosystem</span>
            <div className="p-2 rounded-xl bg-purple-500/10 text-purple-400">
              <Github className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white flex items-center gap-3">
            <span title="Public repositories">
              {overview?.totalGitHubRepositories != null ? overview.totalGitHubRepositories : '—'}
              <span className="text-xs font-normal text-surface-400 ml-1">repos</span>
            </span>
            <span className="text-surface-300 dark:text-surface-700 font-light">|</span>
            <span className="text-amber-500 flex items-center gap-1 text-xl font-bold" title="Total repository stars">
              <Star className="w-4 h-4 fill-amber-500" />
              {overview?.totalGitHubStars != null ? overview.totalGitHubStars : '0'}
            </span>
          </div>
          <p className="text-[11px] text-surface-500 mt-1.5">
            Public repositories and total stargazer recognition
          </p>
        </div>

        {/* Sync & Activity Summary */}
        <div className="p-5 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm relative overflow-hidden group hover:border-blue-500/30 transition-all">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Sync & Coverage</span>
            <div className="p-2 rounded-xl bg-blue-500/10 text-blue-500">
              <Activity className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white flex items-baseline gap-2">
            {activeAutoSyncAccounts.length} / {accounts.length}
            <span className="text-xs font-normal text-surface-400">auto-synced</span>
          </div>
          <div className="flex items-center gap-1.5 text-[11px] text-surface-500 mt-1.5">
            <Clock className="w-3 h-3 text-surface-400" />
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
