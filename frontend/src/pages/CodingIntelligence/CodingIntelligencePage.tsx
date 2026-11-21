import React, { useState, useMemo } from 'react';
import {
  Code2,
  ExternalLink,
  Plus,
  Trash2,
  RefreshCw,
  Trophy,
  CheckCircle2,
  AlertCircle,
  Flame,
  Globe,
  Award,
  BookOpen,
} from 'lucide-react';
import {
  CodingPlatform,
  PlatformConfig,
  PlatformAccount,
} from '@/types/codingIntelligence';
import {
  useConnectedAccounts,
  useCodingStatistics,
  useConnectPlatform,
  useDisconnectPlatform,
  useSyncPlatform,
  usePlatformHistory,
  useRatingHistory,
  useProblemDistribution,
  useCodingActivity,
  useContestPerformance,
  usePerformanceInsights,
} from '@/hooks/useCodingIntelligence';
import { HistoricalStatsChart } from './HistoricalStatsChart';
import { AnalyticsHeader } from './components/AnalyticsHeader';
import { PerformanceInsightsSection } from './components/PerformanceInsightsSection';
import { RatingProgressionSection } from './components/RatingProgressionSection';
import { ProblemDistributionSection } from './components/ProblemDistributionSection';
import { ContestPerformanceSection } from './components/ContestPerformanceSection';
import { CodingActivitySection } from './components/CodingActivitySection';

const PLATFORM_CONFIGS: Record<CodingPlatform, PlatformConfig> = {
  LEETCODE: {
    name: 'LeetCode',
    displayName: 'LeetCode',
    platform: 'LEETCODE',
    urlPrefix: 'https://leetcode.com/u/',
    iconName: 'LeetCode',
    color: '#FFA116',
    bgLight: 'bg-amber-500/10 text-amber-500 border-amber-500/20',
    description: 'Algorithms, data structures & contest rating',
    usernameRegex: /^[a-zA-Z0-9_-]{3,30}$/,
    usernamePlaceholder: 'e.g. tour_de_code',
  },
  CODEFORCES: {
    name: 'Codeforces',
    displayName: 'Codeforces',
    platform: 'CODEFORCES',
    urlPrefix: 'https://codeforces.com/profile/',
    iconName: 'Codeforces',
    color: '#1F8ACB',
    bgLight: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
    description: 'Competitive programming rounds & max rating',
    usernameRegex: /^[a-zA-Z0-9_.-]{3,24}$/,
    usernamePlaceholder: 'e.g. tourist',
  },
  CODECHEF: {
    name: 'CodeChef',
    displayName: 'CodeChef',
    platform: 'CODECHEF',
    urlPrefix: 'https://www.codechef.com/users/',
    iconName: 'CodeChef',
    color: '#5B4638',
    bgLight: 'bg-orange-500/10 text-orange-500 border-orange-500/20',
    description: 'Star rating, global rank & contest divisions',
    usernameRegex: /^[a-zA-Z0-9_]{3,30}$/,
    usernamePlaceholder: 'e.g. chef_coder',
  },
  HACKERRANK: {
    name: 'HackerRank',
    displayName: 'HackerRank',
    platform: 'HACKERRANK',
    urlPrefix: 'https://www.hackerrank.com/profile/',
    iconName: 'HackerRank',
    color: '#00EA64',
    bgLight: 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20',
    description: 'Skill badges, certifications & practice domains',
    usernameRegex: /^[a-zA-Z0-9_]{3,30}$/,
    usernamePlaceholder: 'e.g. hack_master',
  },
  GITHUB: {
    name: 'GitHub',
    displayName: 'GitHub',
    platform: 'GITHUB',
    urlPrefix: 'https://github.com/',
    iconName: 'GitHub',
    color: '#24292e',
    bgLight: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
    description: 'Public repositories, commits & open source streak',
    usernameRegex: /^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$/,
    usernamePlaceholder: 'e.g. octocat',
  },
  ATCODER: {
    name: 'AtCoder',
    displayName: 'AtCoder',
    platform: 'ATCODER',
    urlPrefix: 'https://atcoder.jp/users/',
    iconName: 'AtCoder',
    color: '#222222',
    bgLight: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
    description: 'AtCoder Beginner & Regular Contests (ABC / ARC)',
    usernameRegex: /^[a-zA-Z0-9_]{3,16}$/,
    usernamePlaceholder: 'e.g. chokudai',
  },
};

export const CodingIntelligencePage: React.FC = () => {
  const { data: accounts = [], isLoading: isLoadingAccounts, refetch } = useConnectedAccounts();
  const { data: stats, isLoading: isLoadingStats } = useCodingStatistics();
  const connectMutation = useConnectPlatform();
  const disconnectMutation = useDisconnectPlatform();
  const syncMutation = useSyncPlatform();

  const [selectedPlatform, setSelectedPlatform] = useState<CodingPlatform | null>(null);
  const [usernameInput, setUsernameInput] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);
  const [activeHistoryAccountId, setActiveHistoryAccountId] = useState<string | null>(null);

  // Analytics Filters State
  const [analyticsPeriod, setAnalyticsPeriod] = useState<'7d' | '30d' | '90d' | '1y' | 'all'>('30d');
  const [analyticsPlatform, setAnalyticsPlatform] = useState<CodingPlatform | 'ALL'>('ALL');

  const analyticsParams = useMemo(() => ({
    period: analyticsPeriod,
    platform: analyticsPlatform !== 'ALL' ? analyticsPlatform : undefined,
  }), [analyticsPeriod, analyticsPlatform]);

  const { data: insightsData, isLoading: isLoadingInsights } = usePerformanceInsights(analyticsParams);
  const { data: ratingData, isLoading: isLoadingRating } = useRatingHistory(analyticsParams);
  const { data: problemData, isLoading: isLoadingProblem } = useProblemDistribution(analyticsParams);
  const { data: activityData, isLoading: isLoadingActivity } = useCodingActivity(analyticsParams);
  const { data: contestData, isLoading: isLoadingContest } = useContestPerformance(analyticsParams);

  const availableConnectedPlatforms = useMemo(() => {
    return accounts.filter((a) => a.active).map((a) => a.platform);
  }, [accounts]);

  const { data: snapshotHistory = [], isLoading: isLoadingHistory } = usePlatformHistory(activeHistoryAccountId);

  // Map connected accounts by platform
  const accountByPlatform = new Map<CodingPlatform, PlatformAccount>();
  accounts.forEach((acc) => {
    if (acc.active) {
      accountByPlatform.set(acc.platform, acc);
    }
  });

  const handleOpenConnect = (platform: CodingPlatform) => {
    setSelectedPlatform(platform);
    setUsernameInput('');
    setValidationError(null);
  };

  const handleCloseModal = () => {
    setSelectedPlatform(null);
    setUsernameInput('');
    setValidationError(null);
  };

  const handleSubmitConnect = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPlatform) return;

    const config = PLATFORM_CONFIGS[selectedPlatform];
    const trimmed = usernameInput.trim();

    if (!trimmed) {
      setValidationError('Username is required');
      return;
    }

    if (!config.usernameRegex.test(trimmed)) {
      setValidationError(`Invalid username format for ${config.displayName}`);
      return;
    }

    connectMutation.mutate(
      { platform: selectedPlatform, username: trimmed },
      {
        onSuccess: () => {
          handleCloseModal();
        },
      }
    );
  };

  const handleDisconnect = (accountId: string) => {
    if (window.confirm('Are you sure you want to disconnect this coding platform?')) {
      disconnectMutation.mutate(accountId, {
        onSuccess: () => {
          if (activeHistoryAccountId === accountId) {
            setActiveHistoryAccountId(null);
          }
        },
      });
    }
  };

  const activeHistoryAccount = accounts.find((a) => a.id === activeHistoryAccountId);

  return (
    <div className="space-y-8 max-w-7xl mx-auto pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-primary-600 to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-primary-950/40">
              <Code2 className="w-5 h-5" />
            </div>
            Coding Intelligence Dashboard
          </h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">
            Connect public developer profiles and track your competitive programming velocity.
          </p>
        </div>
        <button
          onClick={() => refetch()}
          className="inline-flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-lg bg-surface-100 hover:bg-surface-200 dark:bg-surface-800 dark:hover:bg-surface-700 text-surface-700 dark:text-surface-300 transition-colors self-start sm:self-auto"
        >
          <RefreshCw className={`w-4 h-4 ${isLoadingAccounts ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Aggregated Statistics Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Solved</span>
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-500">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white">
            {isLoadingStats ? '...' : (stats?.totalProblemsSolved ?? '—')}
          </div>
          <p className="text-xs text-surface-500 mt-1">Combined algorithmic problems solved</p>
        </div>

        <div className="p-5 rounded-xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Peak Rating</span>
            <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-500">
              <Flame className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white">
            {isLoadingStats ? '...' : (stats?.highestRating ? Math.round(stats.highestRating) : '—')}
          </div>
          <p className="text-xs text-surface-500 mt-1">Highest competitive contest rating</p>
        </div>

        <div className="p-5 rounded-xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Contests</span>
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-500">
              <Trophy className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white">
            {isLoadingStats ? '...' : (stats?.totalContestsParticipated ?? '—')}
          </div>
          <p className="text-xs text-surface-500 mt-1">Live rounds and rated contests</p>
        </div>

        <div className="p-5 rounded-xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm">
          <div className="flex items-center justify-between text-surface-500 dark:text-surface-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Best Global Rank</span>
            <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400">
              <Award className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-bold text-surface-900 dark:text-white">
            {isLoadingStats ? '...' : (stats?.bestRank ? `#${stats.bestRank.toLocaleString()}` : '—')}
          </div>
          <p className="text-xs text-surface-500 mt-1">Across connected platforms</p>
        </div>
      </div>

      {/* Advanced Analytics Control Bar */}
      <AnalyticsHeader
        selectedPeriod={analyticsPeriod}
        onPeriodChange={setAnalyticsPeriod}
        selectedPlatform={analyticsPlatform}
        onPlatformChange={setAnalyticsPlatform}
        availablePlatforms={availableConnectedPlatforms}
      />

      {/* Performance Insights Section */}
      <PerformanceInsightsSection
        insightsData={insightsData}
        isLoading={isLoadingInsights}
      />

      {/* Rating Progression Line Chart */}
      <RatingProgressionSection
        ratingData={ratingData}
        isLoading={isLoadingRating}
      />

      {/* Problem Distribution & Categorized Tiers */}
      <ProblemDistributionSection
        problemData={problemData}
        isLoading={isLoadingProblem}
      />

      {/* Contest Performance & Competitive History */}
      <ContestPerformanceSection
        contestData={contestData}
        isLoading={isLoadingContest}
      />

      {/* Coding Activity & Snapshot Telemetry Coverage */}
      <CodingActivitySection
        activityData={activityData}
        isLoading={isLoadingActivity}
      />

      {/* Historical Performance Chart Section */}
      {activeHistoryAccountId && (
        <div className="bg-white dark:bg-surface-900 p-6 rounded-2xl border border-surface-200 dark:border-surface-800 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-primary-500/10 text-primary-500 flex items-center justify-center font-bold">
                {activeHistoryAccount?.platform.charAt(0)}
              </div>
              <div>
                <h3 className="text-base font-semibold text-surface-900 dark:text-white">
                  {activeHistoryAccount?.platform} Snapshot Telemetry
                </h3>
                <p className="text-xs text-surface-500">
                  Profile: @{activeHistoryAccount?.username}
                </p>
              </div>
            </div>
            <button
              onClick={() => setActiveHistoryAccountId(null)}
              className="text-xs text-surface-500 hover:text-surface-700 dark:hover:text-surface-300"
            >
              Close
            </button>
          </div>
          <HistoricalStatsChart
            snapshots={snapshotHistory}
            isLoading={isLoadingHistory}
            platformName={activeHistoryAccount?.platform}
          />
        </div>
      )}

      {/* Supported Platforms Grid */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-surface-900 dark:text-white flex items-center gap-2">
            <Globe className="w-5 h-5 text-primary-500" />
            Supported Platforms ({accounts.length} / 6 connected)
          </h2>
          <span className="text-xs text-surface-500">Step 5E Advanced Analytics Engine</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {(Object.keys(PLATFORM_CONFIGS) as CodingPlatform[]).map((platformKey) => {
            const config = PLATFORM_CONFIGS[platformKey];
            const account = accountByPlatform.get(platformKey);
            const isConnected = Boolean(account && account.active);

            return (
              <div
                key={platformKey}
                className="bg-white dark:bg-surface-900 rounded-2xl border border-surface-200 dark:border-surface-800 p-5 flex flex-col justify-between shadow-sm hover:border-surface-300 dark:hover:border-surface-700 transition-all"
              >
                <div>
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className={`px-2.5 py-1 rounded-lg text-xs font-bold border ${config.bgLight}`}>
                        {config.displayName}
                      </div>
                    </div>
                    {isConnected ? (
                      <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                        Connected
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-surface-100 dark:bg-surface-800 text-surface-500">
                        <span className="w-1.5 h-1.5 rounded-full bg-surface-400" />
                        Not Linked
                      </span>
                    )}
                  </div>

                  <p className="text-xs text-surface-500 dark:text-surface-400 mb-4">
                    {config.description}
                  </p>

                  {isConnected && account ? (
                    <div className="space-y-2 mb-4 p-3 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800">
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-surface-500">Username:</span>
                        <a
                          href={account.profileUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="font-medium text-primary-600 dark:text-primary-400 hover:underline flex items-center gap-1"
                        >
                          @{account.username}
                          <ExternalLink className="w-3 h-3" />
                        </a>
                      </div>
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-surface-500">Sync Status:</span>
                        <span
                          className={`font-semibold px-1.5 py-0.5 rounded text-[10px] flex items-center gap-1 ${
                            account.syncStatus === 'SUCCESS'
                              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
                              : account.syncStatus === 'FAILED'
                              ? 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'
                              : account.syncStatus === 'PENDING'
                              ? 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400 animate-pulse'
                              : 'bg-surface-100 text-surface-700 dark:bg-surface-800 dark:text-surface-300'
                          }`}
                        >
                          {account.syncStatus === 'PENDING' && (
                            <RefreshCw className="w-2.5 h-2.5 animate-spin" />
                          )}
                          {account.syncStatus}
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-surface-500">Last Synced:</span>
                        <span className="text-surface-500">
                          {account.lastSyncedAt
                            ? new Date(account.lastSyncedAt).toLocaleString()
                            : 'Never synced'}
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-surface-500">Last Attempt:</span>
                        <span className="text-surface-500">
                          {account.lastSyncAttemptAt
                            ? new Date(account.lastSyncAttemptAt).toLocaleString()
                            : 'None'}
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-surface-500">Next Auto-Sync:</span>
                        <span className="text-surface-500 font-mono text-[11px]">
                          {account.lastSyncedAt
                            ? `~${new Date(new Date(account.lastSyncedAt).getTime() + 6 * 3600 * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
                            : 'Every 6 hours'}
                        </span>
                      </div>

                      {account.syncStatus === 'FAILED' && account.lastSyncError && (
                        <div className={`p-2 mt-2 rounded border flex items-start gap-1.5 text-[11px] ${
                          account.lastSyncError.toLowerCase().includes('rate limit') || account.lastSyncError.includes('429')
                            ? 'bg-amber-50/80 dark:bg-amber-950/30 border-amber-200 dark:border-amber-900/50 text-amber-700 dark:text-amber-400'
                            : 'bg-red-50/80 dark:bg-red-950/30 border-red-200 dark:border-red-900/50 text-red-600 dark:text-red-400'
                        }`}>
                          <AlertCircle className="w-3.5 h-3.5 shrink-0 mt-0.5" />
                          <div className="flex flex-col">
                            {account.lastSyncError.toLowerCase().includes('rate limit') && (
                              <span className="font-semibold uppercase tracking-wider text-[10px]">Rate Limited</span>
                            )}
                            <span className="break-all">{account.lastSyncError}</span>
                          </div>
                        </div>
                      )}

                      {account.latestSnapshot && (
                        <div className="pt-2 mt-2 border-t border-surface-200/60 dark:border-surface-700/60 grid grid-cols-2 gap-2 text-xs">
                          {account.latestSnapshot.problemsSolved != null && (
                            <div>
                              <span className="text-[10px] text-surface-400 block">Solved</span>
                              <span className="font-bold text-surface-800 dark:text-surface-200">
                                {account.latestSnapshot.problemsSolved}
                              </span>
                            </div>
                          )}
                          {account.latestSnapshot.currentRating != null && (
                            <div>
                              <span className="text-[10px] text-surface-400 block">Rating</span>
                              <span className="font-bold text-surface-800 dark:text-surface-200">
                                {account.latestSnapshot.currentRating}
                              </span>
                            </div>
                          )}
                          {account.latestSnapshot.globalRank != null && (
                            <div>
                              <span className="text-[10px] text-surface-400 block">Rank</span>
                              <span className="font-bold text-surface-800 dark:text-surface-200">
                                #{account.latestSnapshot.globalRank.toLocaleString()}
                              </span>
                            </div>
                          )}
                          {account.platform === 'GITHUB' && account.latestSnapshot.additionalStats?.publicRepos != null && (
                            <div>
                              <span className="text-[10px] text-surface-400 block">Public Repos</span>
                              <span className="font-bold text-surface-800 dark:text-surface-200">
                                {String(account.latestSnapshot.additionalStats.publicRepos)}
                              </span>
                            </div>
                          )}
                          {account.platform === 'GITHUB' && account.latestSnapshot.additionalStats?.totalStars != null && (
                            <div>
                              <span className="text-[10px] text-surface-400 block">Total Stars</span>
                              <span className="font-bold text-surface-800 dark:text-surface-200">
                                ★ {String(account.latestSnapshot.additionalStats.totalStars)}
                              </span>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  ) : null}
                </div>

                <div className="pt-2 border-t border-surface-100 dark:border-surface-800 flex items-center justify-between gap-2">
                  {isConnected && account ? (
                    <>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => syncMutation.mutate(account.id)}
                          disabled={syncMutation.isPending || account.syncStatus === 'PENDING'}
                          className="text-xs font-medium text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 flex items-center gap-1 disabled:opacity-50"
                          title="Synchronize live statistics from platform"
                        >
                          <RefreshCw
                            className={`w-3.5 h-3.5 ${
                              syncMutation.isPending || account.syncStatus === 'PENDING'
                                ? 'animate-spin'
                                : ''
                            }`}
                          />
                          {account.syncStatus === 'PENDING' ? 'Syncing...' : 'Sync'}
                        </button>
                        <button
                          onClick={() =>
                            setActiveHistoryAccountId(
                              activeHistoryAccountId === account.id ? null : account.id
                            )
                          }
                          className="text-xs font-medium text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 flex items-center gap-1"
                        >
                          <BookOpen className="w-3.5 h-3.5" />
                          {activeHistoryAccountId === account.id ? 'Hide' : 'History'}
                        </button>
                      </div>
                      <button
                        onClick={() => handleDisconnect(account.id)}
                        disabled={disconnectMutation.isPending}
                        className="text-xs font-medium text-red-500 hover:text-red-600 flex items-center gap-1 disabled:opacity-50"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        Disconnect
                      </button>
                    </>
                  ) : (
                    <button
                      onClick={() => handleOpenConnect(platformKey)}
                      className="w-full py-2 px-3 rounded-lg text-xs font-medium bg-primary-50 dark:bg-primary-950/40 text-primary-600 dark:text-primary-400 hover:bg-primary-100 dark:hover:bg-primary-900/50 flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      Connect {config.displayName}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Connect Account Modal */}
      {selectedPlatform && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
          <div className="bg-white dark:bg-surface-900 rounded-2xl border border-surface-200 dark:border-surface-800 shadow-2xl max-w-md w-full p-6 space-y-5">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-surface-900 dark:text-white">
                Connect {PLATFORM_CONFIGS[selectedPlatform].displayName}
              </h3>
              <button
                onClick={handleCloseModal}
                className="text-surface-400 hover:text-surface-600 dark:hover:text-surface-200 text-sm"
              >
                ✕
              </button>
            </div>

            <p className="text-xs text-surface-500">
              Enter your public handle on {PLATFORM_CONFIGS[selectedPlatform].displayName}. DevSync will verify the username and construct your public profile URL.
            </p>

            <form onSubmit={handleSubmitConnect} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-surface-700 dark:text-surface-300 mb-1">
                  Public Username / Handle
                </label>
                <input
                  type="text"
                  value={usernameInput}
                  onChange={(e) => {
                    setUsernameInput(e.target.value);
                    if (validationError) setValidationError(null);
                  }}
                  placeholder={PLATFORM_CONFIGS[selectedPlatform].usernamePlaceholder}
                  className="w-full px-3 py-2 text-sm rounded-lg border border-surface-300 dark:border-surface-700 bg-white dark:bg-surface-800 text-surface-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
                  autoFocus
                />
                {validationError && (
                  <p className="text-xs text-red-500 mt-1 flex items-center gap-1">
                    <AlertCircle className="w-3 h-3" />
                    {validationError}
                  </p>
                )}
              </div>

              {usernameInput.trim() && (
                <div className="p-3 rounded-lg bg-surface-50 dark:bg-surface-800/60 border border-surface-200 dark:border-surface-700 text-xs">
                  <span className="text-surface-500">Profile URL: </span>
                  <span className="font-mono text-primary-600 dark:text-primary-400 break-all">
                    {PLATFORM_CONFIGS[selectedPlatform].urlPrefix}
                    {usernameInput.trim()}
                  </span>
                </div>
              )}

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="px-4 py-2 text-xs font-medium text-surface-600 dark:text-surface-400 hover:text-surface-900 dark:hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={connectMutation.isPending}
                  className="px-4 py-2 text-xs font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg shadow-sm disabled:opacity-50 flex items-center gap-1.5"
                >
                  {connectMutation.isPending && (
                    <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  )}
                  Save & Connect
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
