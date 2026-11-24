import React, { useState, useMemo } from 'react';
import {
  Code2,
  ExternalLink,
  RefreshCw,
  Trash2,
  AlertCircle,
  CheckCircle2,
  Globe,
  Sparkles,
  Link2,
  Trophy,
  Star,
  Users,
  Search,
  Check,
  Info,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import {
  useConnectedAccounts,
  useConnectPlatform,
  useDisconnectPlatform,
  useSyncPlatform,
  usePreviewCodingProfile,
} from '@/hooks/useCodingIntelligence';
import { detectPlatformFromUrl } from '@/utils/platformUrlDetector';
import { CodingPlatform, ProfilePreviewResponse } from '@/types/codingIntelligence';

const EXAMPLE_URLS: { label: string; url: string; platform: CodingPlatform }[] = [
  { label: 'LeetCode', url: 'https://leetcode.com/u/neal_wu/', platform: 'LEETCODE' },
  { label: 'Codeforces', url: 'https://codeforces.com/profile/tourist', platform: 'CODEFORCES' },
  { label: 'GitHub', url: 'https://github.com/torvalds', platform: 'GITHUB' },
  { label: 'AtCoder', url: 'https://atcoder.jp/users/chokudai', platform: 'ATCODER' },
  { label: 'GeeksforGeeks', url: 'https://www.geeksforgeeks.org/user/tourist_dev/', platform: 'GEEKSFORGEEKS' },
  { label: 'CodeChef', url: 'https://www.codechef.com/users/gennady', platform: 'CODECHEF' },
  { label: 'HackerRank', url: 'https://www.hackerrank.com/profile/alicedev', platform: 'HACKERRANK' },
];

export const CodingProfilesPage: React.FC = () => {
  const [urlInput, setUrlInput] = useState('');
  const [previewData, setPreviewData] = useState<ProfilePreviewResponse | null>(null);
  const [syncingAccountId, setSyncingAccountId] = useState<string | null>(null);
  const [disconnectingAccountId, setDisconnectingAccountId] = useState<string | null>(null);

  // Queries & Mutations
  const { data: accounts = [], isLoading, isRefetching } = useConnectedAccounts();
  const previewMutation = usePreviewCodingProfile();
  const connectMutation = useConnectPlatform();
  const disconnectMutation = useDisconnectPlatform();
  const syncMutation = useSyncPlatform();

  // Real-time client-side URL detection
  const clientDetection = useMemo(() => {
    return detectPlatformFromUrl(urlInput);
  }, [urlInput]);

  const handlePreview = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const cleanUrl = urlInput.trim();
    if (!cleanUrl) return;

    try {
      const res = await previewMutation.mutateAsync(cleanUrl);
      setPreviewData(res);
    } catch {
      // Error is handled by API error toast / response
      setPreviewData(null);
    }
  };

  const handleConnect = async () => {
    const targetUrl = previewData?.canonicalProfileUrl || urlInput.trim();
    if (!targetUrl) return;

    await connectMutation.mutateAsync({
      profileUrl: targetUrl,
      platform: previewData?.platform,
      username: previewData?.username,
    });

    setUrlInput('');
    setPreviewData(null);
  };

  const handleSync = async (accountId: string) => {
    setSyncingAccountId(accountId);
    try {
      await syncMutation.mutateAsync(accountId);
    } finally {
      setSyncingAccountId(null);
    }
  };

  const handleDisconnect = async (accountId: string) => {
    if (!confirm('Are you sure you want to disconnect this account? Historical statistics will be preserved.')) return;
    setDisconnectingAccountId(accountId);
    try {
      await disconnectMutation.mutateAsync(accountId);
    } finally {
      setDisconnectingAccountId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Page Header */}
      <div className="bg-gradient-to-r from-slate-900/90 via-indigo-950/60 to-slate-900/90 border border-indigo-500/30 p-4 rounded-xl shadow-lg flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shrink-0 shadow-inner">
            <Code2 className="w-5 h-5 text-indigo-400" />
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-100 flex items-center gap-2">
              Competitive Coding Accounts & Profiles
            </h1>
            <p className="text-xs text-slate-400 mt-0.5">
              Connect public profiles by pasting your profile URL. Powered by live platform integrations.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 text-xs">
          <span className="px-2.5 py-1 rounded-lg bg-indigo-500/15 text-indigo-300 border border-indigo-500/30 font-semibold">
            {accounts.length} Connected
          </span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-400 font-mono text-[11px]">
            {accounts.filter((a) => a.syncStatus === 'SUCCESS').length} Auto-Synced
          </span>
        </div>
      </div>

      {/* 2. Intelligent URL Connection Hero Card */}
      <Card className="border-indigo-500/25 bg-slate-900/50 dark:bg-slate-900/70 shadow-lg">
        <CardHeader className="pb-2 bg-slate-900/80 dark:bg-slate-900/90 border-b border-indigo-500/20">
          <div className="flex items-center justify-between w-full">
            <CardTitle className="text-sm font-bold flex items-center gap-2 text-indigo-300">
              <Sparkles className="w-4 h-4 text-indigo-400" />
              Connect via Public Profile URL
            </CardTitle>
            <span className="text-[11px] text-slate-400">
              Auto-detects LeetCode, Codeforces, GitHub, AtCoder, CodeChef, HackerRank, GeeksforGeeks
            </span>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <form onSubmit={handlePreview} className="space-y-3">
            <div className="relative">
              <input
                type="text"
                value={urlInput}
                onChange={(e) => {
                  setUrlInput(e.target.value);
                  if (previewData) setPreviewData(null);
                }}
                placeholder="Paste your public profile URL (e.g. https://leetcode.com/u/your-username/)"
                className="w-full pl-3.5 pr-28 py-2.5 text-sm rounded-xl border border-indigo-500/30 bg-slate-950/80 text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all font-mono"
              />

              <div className="absolute right-1.5 top-1.5 flex items-center gap-1.5">
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                  isLoading={previewMutation.isPending}
                  disabled={!urlInput.trim()}
                  leftIcon={<Search className="w-3.5 h-3.5" />}
                >
                  Preview
                </Button>
              </div>
            </div>

            {/* Real-time Detection Status Pill */}
            {clientDetection && (
              <div className="flex items-center gap-2 text-xs animate-in fade-in">
                {clientDetection.isValid ? (
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 font-medium">
                    <Check className="w-3.5 h-3.5" />
                    Detected: <strong className="font-semibold">{clientDetection.platformDisplayName}</strong> (@{clientDetection.username})
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20 font-medium">
                    <AlertCircle className="w-3.5 h-3.5" />
                    {clientDetection.errorMessage || 'Invalid profile URL'}
                  </span>
                )}
              </div>
            )}

            {/* Quick Example URLs */}
            <div className="flex items-center flex-wrap gap-1.5 pt-1">
              <span className="text-[11px] text-surface-400 flex items-center gap-1">
                <Link2 className="w-3 h-3" /> Quick Examples:
              </span>
              {EXAMPLE_URLS.map((eg) => (
                <button
                  key={eg.platform}
                  type="button"
                  onClick={() => {
                    setUrlInput(eg.url);
                    setPreviewData(null);
                  }}
                  className="px-2 py-0.5 text-[11px] font-mono rounded bg-surface-100 hover:bg-surface-200 dark:bg-surface-800 dark:hover:bg-surface-700 text-surface-600 dark:text-surface-300 border border-surface-200 dark:border-surface-700 transition-colors"
                >
                  {eg.label}
                </button>
              ))}
            </div>
          </form>

          {/* 3. Live Profile Preview Banner / Card */}
          {previewData && (
            <div className="p-4 rounded-xl border border-indigo-500/30 bg-slate-950/70 space-y-4 animate-in fade-in slide-in-from-top-2 shadow-inner">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  {previewData.avatarUrl ? (
                    <img
                      src={previewData.avatarUrl}
                      alt={previewData.username}
                      className="w-12 h-12 rounded-xl object-cover border border-indigo-500/30 shadow-sm"
                    />
                  ) : (
                    <div className="w-12 h-12 rounded-xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
                      <Code2 className="w-6 h-6" />
                    </div>
                  )}

                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="text-sm font-bold text-slate-100">
                        {previewData.displayName || previewData.username}
                      </h3>
                      <Badge variant="primary" size="sm">
                        {previewData.platformDisplayName}
                      </Badge>
                      {previewData.autoSyncSupported ? (
                        <span className="inline-flex items-center gap-1 text-[10px] font-semibold text-emerald-400">
                          <CheckCircle2 className="w-3 h-3" /> Auto-Sync Supported
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[10px] font-semibold text-slate-400">
                          <Info className="w-3 h-3" /> Profile Link Only
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-2 mt-0.5 text-xs text-slate-400">
                      <span>@{previewData.username}</span>
                      <span>•</span>
                      <a
                        href={previewData.canonicalProfileUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 text-indigo-400 hover:text-indigo-300 hover:underline"
                      >
                        {previewData.canonicalProfileUrl} <ExternalLink className="w-3 h-3" />
                      </a>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 self-end sm:self-center">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setPreviewData(null)}
                  >
                    Dismiss
                  </Button>
                  <Button
                    type="button"
                    variant="primary"
                    size="sm"
                    onClick={handleConnect}
                    isLoading={connectMutation.isPending}
                    disabled={previewData.alreadyConnected || !previewData.profileExists}
                    leftIcon={<Check className="w-3.5 h-3.5" />}
                  >
                    {previewData.alreadyConnected ? 'Already Connected' : 'Confirm & Connect'}
                  </Button>
                </div>
              </div>

              {/* Warning / Disclosures */}
              {previewData.capabilityNotice && (
                <div className="p-2.5 rounded-lg bg-sky-950/40 border border-sky-500/30 text-xs text-sky-300 flex items-start gap-2">
                  <Globe className="w-4 h-4 shrink-0 mt-0.5 text-sky-400" />
                  <span>{previewData.capabilityNotice}</span>
                </div>
              )}

              {previewData.warning && (
                <div className="p-2.5 rounded-lg bg-amber-950/40 border border-amber-500/30 text-xs text-amber-300 flex items-start gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-amber-400" />
                  <span>{previewData.warning}</span>
                </div>
              )}

              {/* Preview Metrics Grid with Themed Color Sections */}
              {previewData.metrics && (
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-1 text-xs">
                  {previewData.metrics.problemsSolved != null && (
                    <div className="p-3 rounded-xl bg-emerald-950/30 border border-emerald-500/30 shadow-xs">
                      <span className="text-[10px] text-emerald-400 block font-bold tracking-wider">PROBLEMS SOLVED</span>
                      <span className="text-base font-extrabold text-emerald-200 mt-0.5 block">
                        {previewData.metrics.problemsSolved}
                      </span>
                      {previewData.metrics.easySolved != null && (
                        <div className="text-[10px] text-emerald-400/80 mt-0.5 font-mono">
                          {previewData.metrics.easySolved}E / {previewData.metrics.mediumSolved}M / {previewData.metrics.hardSolved}H
                        </div>
                      )}
                    </div>
                  )}

                  {previewData.metrics.currentRating != null && (
                    <div className="p-3 rounded-xl bg-amber-950/30 border border-amber-500/30 shadow-xs">
                      <span className="text-[10px] text-amber-400 block font-bold tracking-wider flex items-center gap-1">
                        <Trophy className="w-3 h-3 text-amber-400" /> CONTEST RATING
                      </span>
                      <span className="text-base font-extrabold text-amber-200 mt-0.5 block">
                        {previewData.metrics.currentRating}
                      </span>
                      {previewData.metrics.maximumRating != null && (
                        <div className="text-[10px] text-amber-400/80 mt-0.5 font-mono">
                          Peak: {previewData.metrics.maximumRating}
                        </div>
                      )}
                    </div>
                  )}

                  {previewData.metrics.publicRepos != null && (
                    <div className="p-3 rounded-xl bg-purple-950/30 border border-purple-500/30 shadow-xs">
                      <span className="text-[10px] text-purple-400 block font-bold tracking-wider flex items-center gap-1">
                        <Star className="w-3 h-3 text-purple-400" /> REPOSITORIES & STARS
                      </span>
                      <span className="text-base font-extrabold text-purple-200 mt-0.5 block">
                        {previewData.metrics.publicRepos} Repos
                      </span>
                      <div className="text-[10px] text-purple-400/80 mt-0.5 font-mono">
                        ★ {previewData.metrics.totalStars || 0} Total Stars
                      </div>
                    </div>
                  )}

                  {previewData.metrics.followers != null && (
                    <div className="p-3 rounded-xl bg-indigo-950/30 border border-indigo-500/30 shadow-xs">
                      <span className="text-[10px] text-indigo-400 block font-bold tracking-wider flex items-center gap-1">
                        <Users className="w-3 h-3 text-indigo-400" /> COMMUNITY
                      </span>
                      <span className="text-base font-extrabold text-indigo-200 mt-0.5 block">
                        {previewData.metrics.followers} Followers
                      </span>
                    </div>
                  )}

                  {previewData.metrics.globalRank != null && (
                    <div className="p-3 rounded-xl bg-cyan-950/30 border border-cyan-500/30 shadow-xs">
                      <span className="text-[10px] text-cyan-400 block font-bold tracking-wider">GLOBAL RANK</span>
                      <span className="text-base font-extrabold text-cyan-200 mt-0.5 block">
                        #{previewData.metrics.globalRank.toLocaleString()}
                      </span>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* 4. Connected Accounts Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between w-full">
            <CardTitle className="text-sm font-bold">Connected Platform Profiles</CardTitle>
            <div className="flex items-center gap-2">
              {isRefetching && (
                <span className="text-[11px] text-surface-400 flex items-center gap-1">
                  <RefreshCw className="w-3 h-3 animate-spin text-primary-500" /> Refreshing...
                </span>
              )}
              <span className="text-[11px] text-surface-500 font-mono">
                {accounts.length} Total Accounts
              </span>
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          ) : accounts.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-surface-50 dark:bg-surface-800/60 border-y border-surface-200 dark:border-surface-700 text-surface-500 font-semibold uppercase tracking-wider text-[10px]">
                  <tr>
                    <th className="py-2.5 px-3 w-10">#</th>
                    <th className="py-2.5 px-3">Platform</th>
                    <th className="py-2.5 px-3">Username / Handle</th>
                    <th className="py-2.5 px-3">Rating / Tier</th>
                    <th className="py-2.5 px-3">Problems Solved</th>
                    <th className="py-2.5 px-3">Contests</th>
                    <th className="py-2.5 px-3">Sync Status</th>
                    <th className="py-2.5 px-3">Last Synced</th>
                    <th className="py-2.5 px-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-200 dark:divide-surface-800">
                  {accounts.map((acc, idx) => {
                    const snap = acc.latestSnapshot;
                    const isAutomated = acc.platform !== 'CODECHEF' && acc.platform !== 'HACKERRANK';
                    const isSyncing = syncingAccountId === acc.id;
                    const isDisconnecting = disconnectingAccountId === acc.id;

                    return (
                      <tr key={acc.id} className="hover:bg-surface-50/50 dark:hover:bg-surface-800/30 transition-colors">
                        <td className="py-3 px-3 text-surface-400 font-mono text-[11px]">
                          {idx + 1}
                        </td>
                        <td className="py-3 px-3">
                          <div className="flex items-center gap-1.5">
                            <Badge variant="primary" size="sm">
                              {acc.platformDisplayName || acc.platform}
                            </Badge>
                            {(acc.platform === 'ATCODER' || acc.platform === 'GEEKSFORGEEKS') && (
                              <span className="text-[9px] px-1.5 py-0.2 rounded bg-amber-500/10 text-amber-600 font-medium">
                                Community
                              </span>
                            )}
                          </div>
                        </td>
                        <td className="py-3 px-3 font-bold text-surface-900 dark:text-surface-100">
                          <div className="flex items-center gap-1.5">
                            <span>{acc.username}</span>
                            {acc.profileUrl && (
                              <a
                                href={acc.profileUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-surface-400 hover:text-primary-600 p-0.5"
                                title="Open official profile"
                              >
                                <ExternalLink className="w-3 h-3" />
                              </a>
                            )}
                          </div>
                        </td>
                        <td className="py-3 px-3 font-semibold text-blue-600 dark:text-blue-400">
                          {snap?.currentRating != null ? (
                            <span>{snap.currentRating}</span>
                          ) : (
                            <span className="text-surface-400 font-normal">—</span>
                          )}
                        </td>
                        <td className="py-3 px-3 font-semibold text-surface-800 dark:text-surface-200">
                          {snap?.problemsSolved != null ? (
                            <span>{snap.problemsSolved}</span>
                          ) : acc.platform === 'GITHUB' && snap?.additionalStats?.publicRepos != null ? (
                            <span className="text-surface-600 dark:text-surface-300 font-normal">
                              {String(snap.additionalStats.publicRepos)} repos
                            </span>
                          ) : (
                            <span className="text-surface-400 font-normal">—</span>
                          )}
                        </td>
                        <td className="py-3 px-3 text-surface-600 dark:text-surface-400">
                          {snap?.contestsParticipated != null ? snap.contestsParticipated : '—'}
                        </td>
                        <td className="py-3 px-3">
                          <span
                            className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                              acc.syncStatus === 'SUCCESS'
                                ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20'
                                : acc.syncStatus === 'FAILED'
                                ? 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20'
                                : acc.syncStatus === 'PENDING'
                                ? 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20'
                                : 'bg-surface-200 dark:bg-surface-800 text-surface-500'
                            }`}
                          >
                            {acc.syncStatus}
                          </span>
                        </td>
                        <td className="py-3 px-3 text-surface-500 text-[11px] font-mono">
                          {acc.lastSyncedAt
                            ? new Date(acc.lastSyncedAt).toLocaleString(undefined, {
                                month: 'short',
                                day: 'numeric',
                                hour: '2-digit',
                                minute: '2-digit',
                              })
                            : 'Never'}
                        </td>
                        <td className="py-3 px-3 text-right whitespace-nowrap">
                          <div className="flex items-center justify-end gap-1.5">
                            {/* Manual Sync Button */}
                            {isAutomated ? (
                              <button
                                onClick={() => handleSync(acc.id)}
                                disabled={isSyncing}
                                className="p-1 rounded text-surface-600 hover:text-primary-600 hover:bg-surface-100 dark:hover:bg-surface-800 transition-colors"
                                title="Synchronize latest statistics now"
                              >
                                <RefreshCw className={`w-3.5 h-3.5 ${isSyncing ? 'animate-spin text-primary-500' : ''}`} />
                              </button>
                            ) : (
                              <span
                                className="p-1 text-surface-300 dark:text-surface-600 cursor-not-allowed"
                                title="Automated synchronization restricted for this platform"
                              >
                                <RefreshCw className="w-3.5 h-3.5" />
                              </span>
                            )}

                            {/* Disconnect Account */}
                            <button
                              onClick={() => handleDisconnect(acc.id)}
                              disabled={isDisconnecting}
                              className="p-1 rounded text-surface-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors"
                              title="Disconnect profile"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-8">
              <EmptyState
                icon={Code2}
                title="No coding profiles connected"
                description="Paste your LeetCode, Codeforces, GitHub, or AtCoder profile URL in the field above to connect your account and track statistics."
              />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
