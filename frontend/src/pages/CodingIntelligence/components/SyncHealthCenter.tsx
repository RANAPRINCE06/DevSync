import React from 'react';
import {
  Activity,
  AlertTriangle,
  RefreshCw,
  Clock,
  Zap,
  Database,
  ExternalLink,
} from 'lucide-react';
import { PlatformAccount, PlatformConfig, CodingPlatform } from '@/types/codingIntelligence';

interface SyncHealthCenterProps {
  accounts: PlatformAccount[];
  platformConfigs: Record<CodingPlatform, PlatformConfig>;
  syncingAccountId: string | null;
  onSync: (accountId: string) => void;
  onRefreshAll: () => void;
  isRefreshing: boolean;
}

export const SyncHealthCenter: React.FC<SyncHealthCenterProps> = ({
  accounts,
  platformConfigs,
  syncingAccountId,
  onSync,
  onRefreshAll,
  isRefreshing,
}) => {
  const activeAccounts = accounts.filter((a) => a.active);

  const RATE_LIMIT_CONFIGS: Record<
    CodingPlatform,
    { capacity: number; refillRate: string; description: string }
  > = {
    CODEFORCES: { capacity: 5, refillRate: '2.0 req/sec', description: 'Official REST API' },
    GITHUB: { capacity: 10, refillRate: '0.5 req/sec', description: 'Official GitHub REST API' },
    LEETCODE: { capacity: 5, refillRate: '0.2 req/sec', description: 'Public GraphQL endpoint' },
    ATCODER: { capacity: 1, refillRate: '1.0 req/sec', description: 'Community API safe throttle' },
    CODECHEF: { capacity: 1, refillRate: '0.1 req/sec', description: 'Requires OAuth2 Partner' },
    HACKERRANK: { capacity: 5, refillRate: '0.5 req/sec', description: 'Enterprise Auth only' },
  };

  const hasFailingAccount = activeAccounts.some((a) => a.syncStatus === 'FAILED');

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-500">
            <Activity className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
              Synchronization Health Center
              <span
                className={`text-[11px] font-semibold px-2 py-0.5 rounded-full border ${
                  hasFailingAccount
                    ? 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20'
                    : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20'
                }`}
              >
                {hasFailingAccount ? 'Attention Needed' : 'All Sync Channels Active'}
              </span>
            </h3>
            <p className="text-xs text-surface-500">
              Reliability layer monitoring: 6-hour scheduler, token-bucket rate limiters, in-memory cache, and retry executors.
            </p>
          </div>
        </div>

        <button
          onClick={onRefreshAll}
          disabled={isRefreshing}
          className="inline-flex items-center gap-2 px-3 py-2 text-xs font-semibold rounded-xl bg-surface-100 hover:bg-surface-200 dark:bg-surface-800 dark:hover:bg-surface-700 text-surface-700 dark:text-surface-300 transition-colors self-start sm:self-auto disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin' : ''}`} />
          Refresh Health Status
        </button>
      </div>

      {/* Reliability Architecture Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div className="p-3.5 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800 flex items-start gap-3">
          <Clock className="w-4 h-4 text-blue-500 shrink-0 mt-0.5" />
          <div>
            <span className="text-xs font-bold text-surface-900 dark:text-white block">
              6-Hour Background Sync
            </span>
            <span className="text-[11px] text-surface-500">
              `PlatformSyncScheduler` triggers periodic updates across active accounts with in-flight concurrency locks.
            </span>
          </div>
        </div>

        <div className="p-3.5 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800 flex items-start gap-3">
          <Zap className="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
          <div>
            <span className="text-xs font-bold text-surface-900 dark:text-white block">
              Token-Bucket Throttling
            </span>
            <span className="text-[11px] text-surface-500">
              `PlatformRateLimitManager` enforces strict outbound request quotas to prevent 429 rate limit bans.
            </span>
          </div>
        </div>

        <div className="p-3.5 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800 flex items-start gap-3">
          <Database className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
          <div>
            <span className="text-xs font-bold text-surface-900 dark:text-white block">
              15-Min In-Memory Cache
            </span>
            <span className="text-[11px] text-surface-500">
              `PlatformProfileCache` caches profile snapshots with 900s TTL to eliminate redundant external HTTP queries.
            </span>
          </div>
        </div>
      </div>

      {/* Account Sync Channel List */}
      <div className="space-y-3">
        <h4 className="text-xs font-bold text-surface-900 dark:text-white uppercase tracking-wider">
          Active Account Channels & Health
        </h4>

        {activeAccounts.length === 0 ? (
          <div className="p-6 text-center text-xs text-surface-400 border border-dashed border-surface-200 dark:border-surface-800 rounded-xl">
            No active platform accounts connected yet. Link a platform above to monitor synchronization telemetry.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {activeAccounts.map((account) => {
              const cfg = platformConfigs[account.platform];
              const rateLimit = RATE_LIMIT_CONFIGS[account.platform];
              const isSyncing = syncingAccountId === account.id || account.syncStatus === 'PENDING';
              const nextSyncDate = account.lastSyncedAt
                ? new Date(new Date(account.lastSyncedAt).getTime() + 6 * 3600 * 1000)
                : null;

              return (
                <div
                  key={account.id}
                  className="p-4 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800 space-y-3"
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className={`px-2 py-0.5 rounded text-[11px] font-bold border ${cfg.bgLight}`}>
                        {cfg.displayName}
                      </span>
                      <a
                        href={account.profileUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="text-xs font-semibold text-surface-800 dark:text-surface-200 hover:text-primary-500 flex items-center gap-1"
                      >
                        @{account.username}
                        <ExternalLink className="w-2.5 h-2.5" />
                      </a>
                    </div>

                    <span
                      className={`font-semibold px-2 py-0.5 rounded text-[10px] flex items-center gap-1 ${
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
                        <RefreshCw className="w-2.5 h-2.5 animate-spin" />
                      )}
                      {account.syncStatus}
                    </span>
                  </div>

                  {/* Sync Channel Telemetry Grid */}
                  <div className="grid grid-cols-2 gap-2 text-[11px] text-surface-500">
                    <div>
                      <span className="text-surface-400 block text-[10px]">Last Sync Success:</span>
                      <span className="font-medium text-surface-700 dark:text-surface-300">
                        {account.lastSyncedAt
                          ? new Date(account.lastSyncedAt).toLocaleString([], {
                              dateStyle: 'short',
                              timeStyle: 'short',
                            })
                          : 'Never synced'}
                      </span>
                    </div>

                    <div>
                      <span className="text-surface-400 block text-[10px]">Next Scheduled Sync:</span>
                      <span className="font-mono text-surface-700 dark:text-surface-300">
                        {nextSyncDate
                          ? `~${nextSyncDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
                          : 'Every 6 hours'}
                      </span>
                    </div>

                    <div>
                      <span className="text-surface-400 block text-[10px]">Rate Limit Token-Bucket:</span>
                      <span className="font-mono text-surface-700 dark:text-surface-300">
                        Cap: {rateLimit.capacity} • {rateLimit.refillRate}
                      </span>
                    </div>

                    <div>
                      <span className="text-surface-400 block text-[10px]">Channel Reliability:</span>
                      <span className="font-medium text-surface-700 dark:text-surface-300">
                        {cfg.syncSupported ? 'Automated Sync' : 'Profile Link Only'}
                      </span>
                    </div>
                  </div>

                  {/* Sanitized Error Alert */}
                  {account.syncStatus === 'FAILED' && account.lastSyncError && (
                    <div className="p-2.5 rounded-lg bg-red-50/80 dark:bg-red-950/30 border border-red-200 dark:border-red-900/50 text-red-600 dark:text-red-400 text-[11px] flex items-start gap-1.5">
                      <AlertTriangle className="w-3.5 h-3.5 shrink-0 mt-0.5" />
                      <div className="flex flex-col">
                        <span className="font-semibold text-[10px] uppercase tracking-wider">
                          Sync Error:
                        </span>
                        <span className="break-all">{account.lastSyncError}</span>
                      </div>
                    </div>
                  )}

                  {/* Manual Sync Trigger */}
                  {cfg.syncSupported && (
                    <div className="pt-2 border-t border-surface-200/50 dark:border-surface-700/50 flex justify-end">
                      <button
                        onClick={() => onSync(account.id)}
                        disabled={isSyncing}
                        className="px-2.5 py-1 rounded-lg text-xs font-semibold text-emerald-600 dark:text-emerald-400 hover:bg-emerald-500/10 flex items-center gap-1.5 disabled:opacity-50 transition-colors"
                      >
                        <RefreshCw className={`w-3 h-3 ${isSyncing ? 'animate-spin' : ''}`} />
                        {isSyncing ? 'Syncing Now...' : 'Run On-Demand Sync'}
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
