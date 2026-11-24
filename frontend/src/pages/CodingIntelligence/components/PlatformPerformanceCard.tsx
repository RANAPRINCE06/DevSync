import React from 'react';
import {
  ExternalLink,
  RefreshCw,
  Trash2,
  BookOpen,
  Plus,
  AlertCircle,
  Globe,
  Star,
} from 'lucide-react';
import {
  CodingPlatform,
  PlatformConfig,
  PlatformAccount,
} from '@/types/codingIntelligence';

interface PlatformPerformanceCardProps {
  platformKey: CodingPlatform;
  config: PlatformConfig;
  account?: PlatformAccount;
  isSyncing: boolean;
  isDisconnecting: boolean;
  isHistoryOpen: boolean;
  onConnect: (platform: CodingPlatform) => void;
  onDisconnect: (accountId: string) => void;
  onSync: (accountId: string) => void;
  onToggleHistory: (accountId: string) => void;
}

export const PlatformPerformanceCard: React.FC<PlatformPerformanceCardProps> = ({
  platformKey,
  config,
  account,
  isSyncing,
  isDisconnecting,
  isHistoryOpen,
  onConnect,
  onDisconnect,
  onSync,
  onToggleHistory,
}) => {
  const isConnected = Boolean(account && account.active);
  const snapshot = account?.latestSnapshot;
  const additional = snapshot?.additionalStats;

  return (
    <div
      className={`rounded-2xl bg-white dark:bg-surface-900 border transition-all duration-200 flex flex-col justify-between p-5 shadow-sm hover:shadow-md ${
        isConnected
          ? 'border-surface-200 dark:border-surface-800'
          : 'border-dashed border-surface-300 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/40'
      }`}
    >
      <div>
        {/* Card Header: Platform Tag & Status */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <div className="flex items-center gap-2 flex-wrap">
            <div className={`px-2.5 py-1 rounded-lg text-xs font-bold border ${config.bgLight}`}>
              {config.displayName}
            </div>

            {config.communityApi && (
              <span
                className="text-[10px] px-2 py-0.5 rounded-full font-semibold bg-sky-500/10 text-sky-600 dark:text-sky-400 border border-sky-500/20"
                title="Sourced from AtCoder Problems community API (kenkoooo.com)"
              >
                Community API
              </span>
            )}

            {config.requiresAuth && (
              <span
                className="text-[10px] px-2 py-0.5 rounded-full font-semibold bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20"
                title={config.syncNote}
              >
                OAuth2 Required
              </span>
            )}

            {config.enterpriseOnly && (
              <span
                className="text-[10px] px-2 py-0.5 rounded-full font-semibold bg-purple-500/10 text-purple-600 dark:text-purple-400 border border-purple-500/20"
                title={config.syncNote}
              >
                Enterprise Only
              </span>
            )}
          </div>

          {isConnected ? (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
              Connected
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-surface-100 dark:bg-surface-800 text-surface-500">
              <span className="w-1.5 h-1.5 rounded-full bg-surface-400" />
              Not Linked
            </span>
          )}
        </div>

        {/* Short Platform Description */}
        <p className="text-xs text-surface-500 dark:text-surface-400 mb-3 line-clamp-2">
          {config.description}
        </p>

        {/* Platform-Specific Integration Disclaimers */}
        {config.communityApi && (
          <div className="mb-3 p-2.5 rounded-xl bg-sky-500/10 border border-sky-500/20 text-[11px] text-sky-700 dark:text-sky-300 flex items-start gap-1.5">
            <Globe className="w-3.5 h-3.5 shrink-0 mt-0.5" />
            <span>Community Data: Synchronized via AtCoder Problems API. Contest ratings require official API.</span>
          </div>
        )}

        {!config.syncSupported && (
          <div className="mb-3 p-2.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-[11px] text-amber-700 dark:text-amber-300 flex items-start gap-1.5">
            <AlertCircle className="w-3.5 h-3.5 shrink-0 mt-0.5" />
            <span>{config.syncNote}</span>
          </div>
        )}

        {/* Connected Profile Details */}
        {isConnected && account && (
          <div className="space-y-2 mb-4 p-3.5 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800">
            {/* Handle & External Profile Link */}
            <div className="flex items-center justify-between text-xs">
              <span className="text-surface-500">Public Profile:</span>
              <a
                href={account.profileUrl}
                target="_blank"
                rel="noreferrer"
                className="font-semibold text-primary-600 dark:text-primary-400 hover:underline flex items-center gap-1"
              >
                @{account.username}
                <ExternalLink className="w-3 h-3" />
              </a>
            </div>

            {/* Synchronization Status */}
            <div className="flex items-center justify-between text-xs">
              <span className="text-surface-500">Sync Status:</span>
              <span
                className={`font-semibold px-2 py-0.5 rounded text-[11px] flex items-center gap-1.5 ${
                  account.syncStatus === 'SUCCESS'
                    ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
                    : account.syncStatus === 'FAILED'
                    ? 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'
                    : account.syncStatus === 'PENDING'
                    ? 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400'
                    : 'bg-surface-100 text-surface-700 dark:bg-surface-800 dark:text-surface-300'
                }`}
              >
                {account.syncStatus === 'PENDING' && (
                  <RefreshCw className="w-3 h-3 animate-spin" />
                )}
                {account.syncStatus}
              </span>
            </div>

            {/* Last Synced & Next Auto-Sync */}
            <div className="flex items-center justify-between text-xs">
              <span className="text-surface-500">Last Synced:</span>
              <span className="text-surface-600 dark:text-surface-400">
                {account.lastSyncedAt
                  ? new Date(account.lastSyncedAt).toLocaleString([], {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })
                  : 'Never synced'}
              </span>
            </div>

            {/* Error Message Alert */}
            {account.syncStatus === 'FAILED' && account.lastSyncError && (
              <div
                className={`p-2.5 mt-2 rounded-lg border flex items-start gap-1.5 text-[11px] ${
                  account.lastSyncError.toLowerCase().includes('rate limit') ||
                  account.lastSyncError.includes('429')
                    ? 'bg-amber-50/80 dark:bg-amber-950/30 border-amber-200 dark:border-amber-900/50 text-amber-700 dark:text-amber-400'
                    : 'bg-red-50/80 dark:bg-red-950/30 border-red-200 dark:border-red-900/50 text-red-600 dark:text-red-400'
                }`}
              >
                <AlertCircle className="w-3.5 h-3.5 shrink-0 mt-0.5" />
                <span className="break-all">{account.lastSyncError}</span>
              </div>
            )}

            {/* Platform-Specific Metrics Tiles */}
            {snapshot && (
              <div className="pt-2.5 mt-2.5 border-t border-surface-200/60 dark:border-surface-700/60 grid grid-cols-2 gap-2 text-xs">
                {/* 1. Solved Problems */}
                {snapshot.problemsSolved != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Problems Solved</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100 text-sm">
                      {snapshot.problemsSolved}
                    </span>
                  </div>
                )}

                {/* 2. Rating & Rank for CP platforms */}
                {snapshot.currentRating != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Current Rating</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100 text-sm">
                      {Math.round(snapshot.currentRating)}
                      {snapshot.maximumRating != null && (
                        <span className="text-[10px] font-normal text-surface-400 ml-1">
                          (max: {Math.round(snapshot.maximumRating)})
                        </span>
                      )}
                    </span>
                  </div>
                )}

                {snapshot.globalRank != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Global Rank</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      #{snapshot.globalRank.toLocaleString()}
                    </span>
                  </div>
                )}

                {snapshot.contestsParticipated != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Contests</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      {snapshot.contestsParticipated} rounds
                    </span>
                  </div>
                )}

                {/* LeetCode Difficulty Breakdown */}
                {account.platform === 'LEETCODE' && snapshot.easySolved != null && (
                  <div className="col-span-2 pt-1">
                    <span className="text-[10px] text-surface-400 block font-medium mb-1">
                      Difficulty Tiers
                    </span>
                    <div className="flex items-center gap-1.5 text-[11px] font-semibold">
                      <span className="text-emerald-500">{snapshot.easySolved} E</span>
                      <span className="text-surface-300 dark:text-surface-700">•</span>
                      <span className="text-amber-500">{snapshot.mediumSolved ?? 0} M</span>
                      <span className="text-surface-300 dark:text-surface-700">•</span>
                      <span className="text-red-500">{snapshot.hardSolved ?? 0} H</span>
                    </div>
                  </div>
                )}

                {/* GitHub Specific Metrics */}
                {account.platform === 'GITHUB' && additional?.publicRepos != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Public Repos</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      {String(additional.publicRepos)}
                    </span>
                  </div>
                )}

                {account.platform === 'GITHUB' && additional?.totalStars != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Total Stars</span>
                    <span className="font-bold text-amber-500 flex items-center gap-1">
                      <Star className="w-3 h-3 fill-amber-500" />
                      {String(additional.totalStars)}
                    </span>
                  </div>
                )}

                {account.platform === 'GITHUB' && additional?.followers != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Followers</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      {String(additional.followers)}
                    </span>
                  </div>
                )}

                {/* AtCoder Community Metrics */}
                {account.platform === 'ATCODER' && additional?.communityAcRank != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Community AC Rank</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      #{Number(additional.communityAcRank).toLocaleString()}
                    </span>
                  </div>
                )}

                {account.platform === 'ATCODER' && additional?.ratedPointSum != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Rated Points</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      {Number(additional.ratedPointSum).toLocaleString()}
                    </span>
                  </div>
                )}

                {account.platform === 'ATCODER' && additional?.longestStreakDays != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Solve Streak</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      {Number(additional.longestStreakDays)} days
                    </span>
                  </div>
                )}

                {/* GeeksforGeeks Community Metrics */}
                {account.platform === 'GEEKSFORGEEKS' && additional?.codingScore != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Coding Score</span>
                    <span className="font-bold text-emerald-600 dark:text-emerald-400">
                      {Number(additional.codingScore).toLocaleString()}
                    </span>
                  </div>
                )}

                {account.platform === 'GEEKSFORGEEKS' && additional?.instituteRank != null && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Institute Rank</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      #{String(additional.instituteRank)}
                    </span>
                  </div>
                )}

                {account.platform === 'GEEKSFORGEEKS' && (additional?.currentStreak != null || additional?.maxStreak != null) && (
                  <div>
                    <span className="text-[10px] text-surface-400 block font-medium">Streak (Curr / Max)</span>
                    <span className="font-bold text-surface-900 dark:text-surface-100">
                      {additional?.currentStreak != null ? String(additional.currentStreak) : '0'} / {additional?.maxStreak != null ? String(additional.maxStreak) : '0'}d
                    </span>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Card Actions Footer */}
      <div className="pt-3 border-t border-surface-100 dark:border-surface-800 flex items-center justify-between gap-2">
        {isConnected && account ? (
          <>
            <div className="flex items-center gap-2">
              <button
                onClick={() => onSync(account.id)}
                disabled={!config.syncSupported || isSyncing || account.syncStatus === 'PENDING'}
                className="px-2.5 py-1.5 rounded-lg text-xs font-semibold text-emerald-600 dark:text-emerald-400 hover:bg-emerald-500/10 flex items-center gap-1.5 disabled:opacity-40 transition-colors"
                title={
                  !config.syncSupported
                    ? 'Automated synchronization is unsupported for this platform'
                    : 'Fetch and update live statistics from external platform'
                }
              >
                <RefreshCw
                  className={`w-3.5 h-3.5 ${
                    isSyncing || account.syncStatus === 'PENDING' ? 'animate-spin' : ''
                  }`}
                />
                {account.syncStatus === 'PENDING' ? 'Syncing...' : 'Sync'}
              </button>

              <button
                onClick={() => onToggleHistory(account.id)}
                className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors ${
                  isHistoryOpen
                    ? 'bg-primary-500 text-white'
                    : 'text-primary-600 dark:text-primary-400 hover:bg-primary-500/10'
                }`}
                title="Inspect historical synchronization snapshots"
              >
                <BookOpen className="w-3.5 h-3.5" />
                {isHistoryOpen ? 'Hide' : 'History'}
              </button>
            </div>

            <button
              onClick={() => onDisconnect(account.id)}
              disabled={isDisconnecting}
              className="p-1.5 text-xs font-medium text-surface-400 hover:text-red-500 rounded-lg hover:bg-red-500/10 transition-colors disabled:opacity-40"
              title="Disconnect platform account"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </>
        ) : (
          <button
            onClick={() => onConnect(platformKey)}
            className="w-full py-2 px-3 rounded-xl text-xs font-semibold bg-primary-50 dark:bg-primary-950/40 text-primary-600 dark:text-primary-400 hover:bg-primary-100 dark:hover:bg-primary-900/50 flex items-center justify-center gap-1.5 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            Link {config.displayName} Profile
          </button>
        )}
      </div>
    </div>
  );
};
