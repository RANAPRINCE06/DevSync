import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
  Code2,
  ExternalLink,
  RefreshCw,
  Trophy,
  CheckCircle2,
  AlertTriangle,
  ShieldCheck,
  Star,
  Layers,
  ArrowUpRight,
  Plus,
  Activity,
  ChevronRight,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import {
  useConnectedAccounts,
  useCodingAnalyticsOverview,
  useCodingActivity,
  useSyncPlatform,
} from '@/hooks/useCodingIntelligence';
import { CodingPlatform, PlatformAccount } from '@/types/codingIntelligence';

interface PlatformDisplayMeta {
  name: string;
  color: string;
  badgeBg: string;
  urlPrefix: string;
  isAutomated: boolean;
}

const PLATFORM_META: Record<CodingPlatform, PlatformDisplayMeta> = {
  LEETCODE: {
    name: 'LeetCode',
    color: '#FFA116',
    badgeBg: 'bg-amber-500/10 text-amber-500 border-amber-500/20',
    urlPrefix: 'https://leetcode.com/u/',
    isAutomated: true,
  },
  CODEFORCES: {
    name: 'Codeforces',
    color: '#1F8ACB',
    badgeBg: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
    urlPrefix: 'https://codeforces.com/profile/',
    isAutomated: true,
  },
  GITHUB: {
    name: 'GitHub',
    color: '#8b5cf6',
    badgeBg: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
    urlPrefix: 'https://github.com/',
    isAutomated: true,
  },
  ATCODER: {
    name: 'AtCoder',
    color: '#06b6d4',
    badgeBg: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
    urlPrefix: 'https://atcoder.jp/users/',
    isAutomated: true,
  },
  GEEKSFORGEEKS: {
    name: 'GeeksforGeeks',
    color: '#10b981',
    badgeBg: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    urlPrefix: 'https://www.geeksforgeeks.org/user/',
    isAutomated: true,
  },
  CODECHEF: {
    name: 'CodeChef',
    color: '#d97706',
    badgeBg: 'bg-orange-500/10 text-orange-400 border-orange-500/20',
    urlPrefix: 'https://www.codechef.com/users/',
    isAutomated: false,
  },
  HACKERRANK: {
    name: 'HackerRank',
    color: '#22c55e',
    badgeBg: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    urlPrefix: 'https://www.hackerrank.com/profile/',
    isAutomated: false,
  },
};

const SEVEN_PLATFORMS: CodingPlatform[] = [
  'LEETCODE',
  'CODEFORCES',
  'GITHUB',
  'ATCODER',
  'GEEKSFORGEEKS',
  'CODECHEF',
  'HACKERRANK',
];

export const DashboardCodingOverview: React.FC = () => {
  const { data: accounts = [] } = useConnectedAccounts();
  const { data: overview } = useCodingAnalyticsOverview();
  const { data: activity } = useCodingActivity({ period: '30d' });
  const syncMutation = useSyncPlatform();

  const [syncingId, setSyncingId] = useState<string | null>(null);

  const activeAccounts = accounts.filter((a) => a.active);
  const accountMap = new Map<CodingPlatform, PlatformAccount>(
    activeAccounts.map((a) => [a.platform, a])
  );

  const syncHealth = overview?.syncHealthStatus || 'NORMAL';
  const hasAttention = syncHealth === 'ATTENTION_NEEDED';

  const handleManualSyncAll = async () => {
    const automatable = activeAccounts.filter(
      (a) => a.platform !== 'CODECHEF' && a.platform !== 'HACKERRANK'
    );
    for (const acc of automatable) {
      setSyncingId(acc.id);
      try {
        await syncMutation.mutateAsync(acc.id);
      } catch {
        // Handled by toast
      }
    }
    setSyncingId(null);
  };

  return (
    <div className="space-y-4 pt-2">
      {/* 1. Header Banner & Quick Actions */}
      <div className="bg-gradient-to-r from-slate-900/95 via-indigo-950/70 to-slate-900/95 border border-indigo-500/30 p-4 rounded-xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shrink-0 shadow-inner">
            <Code2 className="w-5 h-5 text-indigo-400" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-slate-100 uppercase tracking-wider">
                Coding Performance Overview
              </h2>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 font-semibold border border-indigo-500/30">
                7 Platforms
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Live competitive programming ratings, verified problem counts, and developer activity across connected profiles
            </p>
          </div>
        </div>

        {/* Quick Actions Navigation */}
        <div className="flex items-center flex-wrap gap-2 text-xs">
          <Button
            variant="outline"
            size="sm"
            onClick={handleManualSyncAll}
            isLoading={Boolean(syncingId)}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${syncingId ? 'animate-spin' : ''}`} />}
            className="border-indigo-500/30 text-indigo-300 hover:bg-indigo-500/10"
          >
            Sync All
          </Button>
          <NavLink
            to="/coding-profiles"
            className="px-3 py-1.5 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 font-medium transition-colors flex items-center gap-1"
          >
            Manage Profiles <ArrowUpRight className="w-3.5 h-3.5" />
          </NavLink>
          <NavLink
            to="/coding"
            className="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-semibold shadow-xs transition-colors flex items-center gap-1"
          >
            Coding Intelligence <ChevronRight className="w-3.5 h-3.5" />
          </NavLink>
        </div>
      </div>

      {/* 2. Developer KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-3">
        {/* KPI 1: Platform-Reported Solved */}
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">Platform-Reported Solved</span>
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100">
            {overview?.totalProblemsSolved ?? '—'}
          </div>
          <span className="text-[10px] text-slate-400 mt-0.5 block" title="Sum of platform-reported totals; not global unique problems">
            Across active accounts
          </span>
        </div>

        {/* KPI 2: Connected Platforms */}
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">Connected Accounts</span>
            <Layers className="w-3.5 h-3.5 text-indigo-500" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100">
            {activeAccounts.length} <span className="text-xs font-normal text-slate-400">/ 7</span>
          </div>
          <span className="text-[10px] text-slate-400 mt-0.5 block">
            {overview?.activeSyncPlatformsCount ?? 0} automated sync
          </span>
        </div>

        {/* KPI 3: Highest CP Rating */}
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">Peak CP Rating</span>
            <Trophy className="w-3.5 h-3.5 text-amber-500" />
          </div>
          <div className="text-xl font-bold text-blue-600 dark:text-blue-400">
            {overview?.highestRating != null ? Math.round(overview.highestRating) : '—'}
          </div>
          <span className="text-[10px] text-slate-400 mt-0.5 block truncate" title="Ratings are platform-specific (never averaged)">
            {overview?.highestRatingPlatform ? `Platform: ${overview.highestRatingPlatform}` : 'No CP rating'}
          </span>
        </div>

        {/* KPI 4: GitHub Repositories & Stars */}
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">GitHub Ecosystem</span>
            <Star className="w-3.5 h-3.5 text-purple-400" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100">
            {overview?.totalGitHubRepositories != null ? `${overview.totalGitHubRepositories} repos` : '—'}
          </div>
          <span className="text-[10px] text-purple-500 dark:text-purple-400 mt-0.5 block">
            ★ {overview?.totalGitHubStars != null ? `${overview.totalGitHubStars} stars` : '0 stars'}
          </span>
        </div>

        {/* KPI 5: Sync Engine Health */}
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs col-span-2 lg:col-span-1">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider">Sync Status</span>
            {hasAttention ? (
              <AlertTriangle className="w-3.5 h-3.5 text-amber-500" />
            ) : (
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
            )}
          </div>
          <div className="flex items-center gap-1.5 mt-0.5">
            <span
              className={`px-2 py-0.5 rounded text-[11px] font-semibold border ${
                hasAttention
                  ? 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20'
                  : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20'
              }`}
            >
              {hasAttention ? 'Attention' : 'Healthy'}
            </span>
          </div>
          <span className="text-[10px] text-slate-400 mt-1 block truncate">
            {overview?.latestSuccessfulSyncAt
              ? `Synced ${new Date(overview.latestSuccessfulSyncAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
              : 'No sync yet'}
          </span>
        </div>
      </div>

      {/* 3. Seven-Platform Individual Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        {SEVEN_PLATFORMS.map((platform) => {
          const meta = PLATFORM_META[platform];
          const account = accountMap.get(platform);
          const snap = account?.latestSnapshot;
          const additional = snap?.additionalStats as Record<string, unknown> | undefined;

          if (!account) {
            return (
              <div
                key={platform}
                className="bg-slate-900/40 border border-dashed border-slate-700/80 p-3 rounded-[3px] flex flex-col justify-between opacity-75 hover:opacity-100 transition-opacity"
              >
                <div>
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-slate-300">{meta.name}</span>
                    <span className="text-[9px] px-1.5 py-0.2 rounded bg-slate-800 text-slate-400">
                      Unlinked
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-500 mt-1.5">
                    {meta.isAutomated
                      ? 'Automated statistics synchronization supported'
                      : 'Public profile linking supported'}
                  </p>
                </div>
                <NavLink
                  to="/coding-profiles"
                  className="mt-3 text-[11px] font-semibold text-indigo-400 hover:text-indigo-300 flex items-center gap-1 self-start"
                >
                  <Plus className="w-3 h-3" /> Connect Account
                </NavLink>
              </div>
            );
          }

          return (
            <div
              key={platform}
              className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px] shadow-xs flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    <span className="text-xs font-bold text-slate-900 dark:text-slate-100">
                      {meta.name}
                    </span>
                    <span
                      className={`text-[9px] px-1.5 py-0.2 rounded font-semibold ${
                        !meta.isAutomated
                          ? 'bg-slate-800 text-slate-400'
                          : account.syncStatus === 'SUCCESS'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : account.syncStatus === 'FAILED'
                          ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                          : 'bg-slate-800 text-slate-400'
                      }`}
                    >
                      {!meta.isAutomated ? 'LINKED' : account.syncStatus}
                    </span>
                  </div>
                  <a
                    href={account.profileUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-slate-400 hover:text-indigo-400 p-0.5"
                    title="Open official profile"
                  >
                    <ExternalLink className="w-3 h-3" />
                  </a>
                </div>

                <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 truncate">
                  @{account.username}
                </div>

                {/* Platform Specific Telemetry */}
                <div className="mt-2.5 pt-2 border-t border-slate-100 dark:border-slate-800 text-xs space-y-1">
                  {/* LeetCode */}
                  {platform === 'LEETCODE' && (
                    <>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Contest Rating:</span>
                        <span className="font-bold text-amber-500">
                          {snap?.currentRating != null ? Math.round(snap.currentRating) : '—'}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Solved Problems:</span>
                        <span className="font-bold text-slate-900 dark:text-slate-100">
                          {snap?.problemsSolved ?? '—'}
                        </span>
                      </div>
                      {snap?.easySolved != null && (
                        <div className="text-[10px] text-slate-400 text-right font-mono">
                          {snap.easySolved}E / {snap.mediumSolved}M / {snap.hardSolved}H
                        </div>
                      )}
                    </>
                  )}

                  {/* Codeforces */}
                  {platform === 'CODEFORCES' && (
                    <>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Rating:</span>
                        <span className="font-bold text-blue-400">
                          {snap?.currentRating != null ? Math.round(snap.currentRating) : '—'}
                          {additional?.rank ? ` (${additional.rank})` : ''}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Unique Solved:</span>
                        <span className="font-bold text-slate-900 dark:text-slate-100">
                          {snap?.problemsSolved ?? '—'}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Contests:</span>
                        <span className="text-slate-300 font-semibold">
                          {snap?.contestsParticipated ?? '—'} rounds
                        </span>
                      </div>
                    </>
                  )}

                  {/* GitHub */}
                  {platform === 'GITHUB' && (
                    <>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Public Repos:</span>
                        <span className="font-bold text-slate-900 dark:text-slate-100">
                          {additional?.publicRepos != null ? String(additional.publicRepos) : '—'}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Total Stars:</span>
                        <span className="font-bold text-purple-400">
                          ★ {additional?.totalStars != null ? String(additional.totalStars) : 0}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Followers:</span>
                        <span className="text-slate-300">
                          {additional?.followers != null ? String(additional.followers) : '—'}
                        </span>
                      </div>
                    </>
                  )}

                  {/* AtCoder */}
                  {platform === 'ATCODER' && (
                    <>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Accepted (AC):</span>
                        <span className="font-bold text-cyan-400">
                          {snap?.problemsSolved ?? '—'}
                        </span>
                      </div>
                      {additional?.communityAcRank != null && (
                        <div className="flex justify-between">
                          <span className="text-slate-400 text-[11px]">Community Rank:</span>
                          <span className="text-slate-300 font-medium">
                            #{Number(additional.communityAcRank).toLocaleString()}
                          </span>
                        </div>
                      )}
                      {additional?.longestStreakDays != null && (
                        <div className="flex justify-between">
                          <span className="text-slate-400 text-[11px]">Streak:</span>
                          <span className="text-slate-300 font-medium">
                            {String(additional.longestStreakDays)} days
                          </span>
                        </div>
                      )}
                    </>
                  )}

                  {/* GeeksforGeeks */}
                  {platform === 'GEEKSFORGEEKS' && (
                    <>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Coding Score:</span>
                        <span className="font-bold text-emerald-400">
                          {additional?.codingScore != null ? Number(additional.codingScore).toLocaleString() : '—'}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400 text-[11px]">Problems Solved:</span>
                        <span className="font-bold text-slate-900 dark:text-slate-100">
                          {snap?.problemsSolved ?? '—'}
                        </span>
                      </div>
                      {additional?.instituteRank != null && (
                        <div className="flex justify-between">
                          <span className="text-slate-400 text-[11px]">Institute Rank:</span>
                          <span className="text-slate-300">
                            #{String(additional.instituteRank)}
                          </span>
                        </div>
                      )}
                    </>
                  )}

                  {/* CodeChef */}
                  {platform === 'CODECHEF' && (
                    <div className="text-[11px] text-amber-500/90 py-1">
                      <span className="font-semibold block">Profile Linked</span>
                      <span className="text-[10px] text-slate-400">Direct sync requires OAuth2 partner API credentials</span>
                    </div>
                  )}

                  {/* HackerRank */}
                  {platform === 'HACKERRANK' && (
                    <div className="text-[11px] text-emerald-500/90 py-1">
                      <span className="font-semibold block">Profile Linked</span>
                      <span className="text-[10px] text-slate-400">Direct sync restricted to enterprise assessment API</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="mt-2 pt-2 border-t border-slate-100 dark:border-slate-800 text-[10px] text-slate-400 flex justify-between items-center">
                <span>
                  {account.lastSyncedAt
                    ? `Synced ${new Date(account.lastSyncedAt).toLocaleDateString([], { month: 'short', day: 'numeric' })}`
                    : 'Not synced'}
                </span>
                {meta.isAutomated && (
                  <button
                    onClick={() => {
                      setSyncingId(account.id);
                      syncMutation.mutate(account.id, {
                        onSettled: () => setSyncingId(null),
                      });
                    }}
                    disabled={Boolean(syncingId)}
                    className="text-indigo-400 hover:text-indigo-300 font-semibold"
                  >
                    Sync
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* 4. Recent Coding Activity & Snapshot Milestones */}
      {activity && activity.snapshotTimeline && activity.snapshotTimeline.length > 0 && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <CardTitle className="flex items-center gap-2">
                <Activity className="w-3.5 h-3.5 text-indigo-400" />
                Recent Platform Synchronization Milestones
              </CardTitle>
              <span className="text-[11px] text-slate-400">
                Audit trail of persisted state transitions (trailing 30 days)
              </span>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="classic-table text-xs">
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>Platform</th>
                    <th>Handle</th>
                    <th>Problems Solved</th>
                    <th>Solved Change</th>
                    <th>Contest Rating</th>
                    <th>Rating Change</th>
                  </tr>
                </thead>
                <tbody>
                  {activity.snapshotTimeline.slice(0, 6).map((item, idx) => (
                    <tr key={`${item.platform}-${item.timestamp}-${idx}`}>
                      <td className="text-slate-400 font-mono text-[11px]">
                        {new Date(item.timestamp).toLocaleString([], {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </td>
                      <td>
                        <span className="font-semibold text-slate-200">
                          {item.platform}
                        </span>
                      </td>
                      <td className="font-medium text-slate-300">@{item.username}</td>
                      <td className="font-bold text-slate-100">
                        {item.problemsSolved != null ? item.problemsSolved : '—'}
                      </td>
                      <td>
                        {item.solvedChange != null && item.solvedChange !== 0 ? (
                          <span
                            className={`font-semibold text-[11px] ${
                              item.solvedChange > 0 ? 'text-emerald-400' : 'text-rose-400'
                            }`}
                          >
                            {item.solvedChange > 0 ? `+${item.solvedChange}` : item.solvedChange}
                          </span>
                        ) : (
                          <span className="text-slate-500">—</span>
                        )}
                      </td>
                      <td className="font-bold text-blue-400">
                        {item.currentRating != null ? Math.round(item.currentRating) : '—'}
                      </td>
                      <td>
                        {item.ratingChange != null && item.ratingChange !== 0 ? (
                          <span
                            className={`font-semibold text-[11px] ${
                              item.ratingChange > 0 ? 'text-emerald-400' : 'text-rose-400'
                            }`}
                          >
                            {item.ratingChange > 0 ? `+${item.ratingChange}` : item.ratingChange}
                          </span>
                        ) : (
                          <span className="text-slate-500">—</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};
