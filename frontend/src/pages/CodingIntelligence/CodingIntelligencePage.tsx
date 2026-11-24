import React, { useState, useMemo } from 'react';
import {
  Code2,
  RefreshCw,
  BarChart3,
  Columns,
  Activity,
  Layers,
} from 'lucide-react';
import {
  CodingPlatform,
  PlatformConfig,
} from '@/types/codingIntelligence';
import {
  useConnectedAccounts,
  useCodingAnalyticsOverview,
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

import { OverviewSection } from './components/OverviewSection';
import { PlatformCardList } from './components/PlatformCardList';
import { AnalyticsHeader } from './components/AnalyticsHeader';
import { RatingProgressionSection } from './components/RatingProgressionSection';
import { ProblemDistributionSection } from './components/ProblemDistributionSection';
import { ContestPerformanceSection } from './components/ContestPerformanceSection';
import { CodingActivitySection } from './components/CodingActivitySection';
import { GitHubInsightsSection } from './components/GitHubInsightsSection';
import { PerformanceInsightsSection } from './components/PerformanceInsightsSection';
import { PlatformComparisonSection } from './components/PlatformComparisonSection';
import { SyncHealthCenter } from './components/SyncHealthCenter';
import { ConnectAccountModal } from './components/ConnectAccountModal';
import { HistoricalStatsChart } from './HistoricalStatsChart';

export const PLATFORM_CONFIGS: Record<CodingPlatform, PlatformConfig> = {
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
    syncSupported: true,
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
    syncSupported: true,
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
    syncSupported: false,
    requiresAuth: true,
    syncNote: 'Automated sync is currently disabled (requires official OAuth2 partner credentials). Profile linking is active.',
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
    syncSupported: false,
    enterpriseOnly: true,
    syncNote: 'Automated sync is currently disabled (official API requires enterprise authorization). Profile linking is active.',
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
    syncSupported: true,
  },
  ATCODER: {
    name: 'AtCoder',
    displayName: 'AtCoder',
    platform: 'ATCODER',
    urlPrefix: 'https://atcoder.jp/users/',
    iconName: 'AtCoder',
    color: '#222222',
    bgLight: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
    description: 'AtCoder Beginner & Regular Contests (Kenkoooo Problems API)',
    usernameRegex: /^[a-zA-Z0-9_]{3,16}$/,
    usernamePlaceholder: 'e.g. chokudai',
    syncSupported: true,
    communityApi: true,
    syncNote: 'Synchronized via community AtCoder Problems API (kenkoooo.com). Solved problems, rated point sum, and streaks are tracked.',
  },
  GEEKSFORGEEKS: {
    name: 'GeeksforGeeks',
    displayName: 'GeeksforGeeks',
    platform: 'GEEKSFORGEEKS',
    urlPrefix: 'https://www.geeksforgeeks.org/user/',
    iconName: 'GeeksforGeeks',
    color: '#2F8D46',
    bgLight: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    description: 'Coding score, problem difficulties, streaks & institute rank',
    usernameRegex: /^[a-zA-Z0-9_.-]{3,50}$/,
    usernamePlaceholder: 'e.g. gfg_coder',
    syncSupported: true,
    communityApi: true,
    syncNote: 'Synchronized via community GeeksforGeeks API. Tracks coding score, solved problems by difficulty (School, Basic, Easy, Medium, Hard), streaks, and institute rank.',
  },
};

type DashboardTab = 'dashboard' | 'analytics' | 'comparison' | 'syncHealth';

export const CodingIntelligencePage: React.FC = () => {
  const { data: accounts = [], isLoading: isLoadingAccounts, refetch } = useConnectedAccounts();
  const { data: overview, isLoading: isLoadingOverview } = useCodingAnalyticsOverview();
  const connectMutation = useConnectPlatform();
  const disconnectMutation = useDisconnectPlatform();
  const syncMutation = useSyncPlatform();

  // Navigation tab state
  const [activeTab, setActiveTab] = useState<DashboardTab>('dashboard');

  // Modals and drawers
  const [modalPlatform, setModalPlatform] = useState<CodingPlatform | null>(null);
  const [activeHistoryAccountId, setActiveHistoryAccountId] = useState<string | null>(null);
  const [syncingAccountId, setSyncingAccountId] = useState<string | null>(null);

  // Analytics Filters State
  const [analyticsPeriod, setAnalyticsPeriod] = useState<'7d' | '30d' | '90d' | '1y' | 'all'>('30d');
  const [analyticsPlatform, setAnalyticsPlatform] = useState<CodingPlatform | 'ALL'>('ALL');

  const analyticsParams = useMemo(
    () => ({
      period: analyticsPeriod,
      platform: analyticsPlatform !== 'ALL' ? analyticsPlatform : undefined,
    }),
    [analyticsPeriod, analyticsPlatform]
  );

  const { data: insightsData, isLoading: isLoadingInsights } = usePerformanceInsights(analyticsParams);
  const { data: ratingData, isLoading: isLoadingRating } = useRatingHistory(analyticsParams);
  const { data: problemData, isLoading: isLoadingProblem } = useProblemDistribution(analyticsParams);
  const { data: activityData, isLoading: isLoadingActivity } = useCodingActivity(analyticsParams);
  const { data: contestData, isLoading: isLoadingContest } = useContestPerformance(analyticsParams);
  const { data: snapshotHistory = [], isLoading: isLoadingHistory } = usePlatformHistory(activeHistoryAccountId);

  const availableConnectedPlatforms = useMemo(() => {
    return accounts.filter((a) => a.active).map((a) => a.platform);
  }, [accounts]);

  const gitHubAccount = useMemo(() => {
    return accounts.find((a) => a.platform === 'GITHUB' && a.active);
  }, [accounts]);

  const activeHistoryAccount = useMemo(() => {
    return accounts.find((a) => a.id === activeHistoryAccountId);
  }, [accounts, activeHistoryAccountId]);

  // Actions
  const handleOpenConnect = (platform: CodingPlatform) => {
    setModalPlatform(platform);
  };

  const handleCloseModal = () => {
    setModalPlatform(null);
  };

  const handleSubmitConnect = (platform: CodingPlatform, username: string) => {
    connectMutation.mutate(
      { platform, username },
      {
        onSuccess: () => {
          handleCloseModal();
        },
      }
    );
  };

  const handleDisconnect = (accountId: string) => {
    if (window.confirm('Are you sure you want to disconnect this platform account? Historical snapshots will remain preserved.')) {
      disconnectMutation.mutate(accountId, {
        onSuccess: () => {
          if (activeHistoryAccountId === accountId) {
            setActiveHistoryAccountId(null);
          }
        },
      });
    }
  };

  const handleSync = (accountId: string) => {
    setSyncingAccountId(accountId);
    syncMutation.mutate(accountId, {
      onSettled: () => {
        setSyncingAccountId(null);
      },
    });
  };

  const handleToggleHistory = (accountId: string) => {
    setActiveHistoryAccountId((prev) => (prev === accountId ? null : accountId));
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-primary-600 to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-primary-950/40">
              <Code2 className="w-5 h-5" />
            </div>
            Coding Intelligence Experience
          </h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">
            Track multi-platform problem solving, competitive ratings, GitHub repositories, and synchronization reliability.
          </p>
        </div>

        <button
          onClick={() => refetch()}
          className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold rounded-xl bg-surface-100 hover:bg-surface-200 dark:bg-surface-800 dark:hover:bg-surface-700 text-surface-700 dark:text-surface-300 transition-colors self-start sm:self-auto"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoadingAccounts ? 'animate-spin' : ''}`} />
          Refresh Data
        </button>
      </div>

      {/* Navigation Pills */}
      <div className="flex items-center gap-2 border-b border-surface-200 dark:border-surface-800 pb-2 overflow-x-auto">
        <button
          onClick={() => setActiveTab('dashboard')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 transition-all shrink-0 ${
            activeTab === 'dashboard'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-surface-600 dark:text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-800'
          }`}
        >
          <Layers className="w-3.5 h-3.5" />
          Overview & Accounts
        </button>

        <button
          onClick={() => setActiveTab('analytics')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 transition-all shrink-0 ${
            activeTab === 'analytics'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-surface-600 dark:text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-800'
          }`}
        >
          <BarChart3 className="w-3.5 h-3.5" />
          Advanced Analytics
        </button>

        <button
          onClick={() => setActiveTab('comparison')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 transition-all shrink-0 ${
            activeTab === 'comparison'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-surface-600 dark:text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-800'
          }`}
        >
          <Columns className="w-3.5 h-3.5" />
          Platform Comparison
        </button>

        <button
          onClick={() => setActiveTab('syncHealth')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 transition-all shrink-0 ${
            activeTab === 'syncHealth'
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-surface-600 dark:text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-800'
          }`}
        >
          <Activity className="w-3.5 h-3.5" />
          Sync Health Center
        </button>
      </div>

      {/* TAB 1: OVERVIEW & PLATFORM CARDS */}
      {activeTab === 'dashboard' && (
        <div className="space-y-8 animate-in fade-in duration-200">
          {/* Key Overview Metrics Section */}
          <OverviewSection
            overview={overview}
            accounts={accounts}
            isLoading={isLoadingOverview}
          />

          {/* Connected Platform Performance Cards */}
          <PlatformCardList
            platformConfigs={PLATFORM_CONFIGS}
            accounts={accounts}
            syncingAccountId={syncingAccountId}
            disconnectingAccountId={disconnectMutation.isPending ? disconnectMutation.variables : null}
            activeHistoryAccountId={activeHistoryAccountId}
            onConnect={handleOpenConnect}
            onDisconnect={handleDisconnect}
            onSync={handleSync}
            onToggleHistory={handleToggleHistory}
          />

          {/* Historical Snapshots Drawer */}
          {activeHistoryAccountId && activeHistoryAccount && (
            <div className="bg-white dark:bg-surface-900 p-6 rounded-2xl border border-surface-200 dark:border-surface-800 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
                    {PLATFORM_CONFIGS[activeHistoryAccount.platform].displayName} Historical Snapshot Telemetry
                  </h3>
                  <p className="text-xs text-surface-500">
                    Inspecting recorded statistical states for @{activeHistoryAccount.username}
                  </p>
                </div>
                <button
                  onClick={() => setActiveHistoryAccountId(null)}
                  className="text-xs font-semibold text-surface-400 hover:text-surface-600 dark:hover:text-surface-200"
                >
                  ✕ Close History
                </button>
              </div>

              <HistoricalStatsChart
                snapshots={snapshotHistory}
                platformName={PLATFORM_CONFIGS[activeHistoryAccount.platform].displayName}
                isLoading={isLoadingHistory}
              />
            </div>
          )}

          {/* Developer Factual Insights Preview */}
          <PerformanceInsightsSection
            insightsData={insightsData}
            isLoading={isLoadingInsights}
          />
        </div>
      )}

      {/* TAB 2: ADVANCED ANALYTICS */}
      {activeTab === 'analytics' && (
        <div className="space-y-8 animate-in fade-in duration-200">
          {/* Analytics Control Bar */}
          <AnalyticsHeader
            selectedPeriod={analyticsPeriod}
            onPeriodChange={setAnalyticsPeriod}
            selectedPlatform={analyticsPlatform}
            onPlatformChange={setAnalyticsPlatform}
            availablePlatforms={availableConnectedPlatforms}
          />

          {/* Developer Performance Insights */}
          <PerformanceInsightsSection
            insightsData={insightsData}
            isLoading={isLoadingInsights}
          />

          {/* Rating Progression Line Chart (Separate LeetCode & Codeforces) */}
          <RatingProgressionSection
            ratingData={ratingData}
            isLoading={isLoadingRating}
          />

          {/* Problem Distribution & Categorized Tiers */}
          <ProblemDistributionSection
            problemData={problemData}
            isLoading={isLoadingProblem}
          />

          {/* Contest Performance & Competitive Rounds */}
          <ContestPerformanceSection
            contestData={contestData}
            isLoading={isLoadingContest}
          />

          {/* Coding Activity & Snapshot Telemetry Coverage */}
          <CodingActivitySection
            activityData={activityData}
            isLoading={isLoadingActivity}
          />

          {/* GitHub Insights */}
          <GitHubInsightsSection
            gitHubAccount={gitHubAccount}
            onConnectGitHub={() => handleOpenConnect('GITHUB')}
          />
        </div>
      )}

      {/* TAB 3: PLATFORM COMPARISON */}
      {activeTab === 'comparison' && (
        <div className="space-y-8 animate-in fade-in duration-200">
          <PlatformComparisonSection
            platformConfigs={PLATFORM_CONFIGS}
            accounts={accounts}
          />
        </div>
      )}

      {/* TAB 4: SYNCHRONIZATION HEALTH CENTER */}
      {activeTab === 'syncHealth' && (
        <div className="space-y-8 animate-in fade-in duration-200">
          <SyncHealthCenter
            accounts={accounts}
            platformConfigs={PLATFORM_CONFIGS}
            syncingAccountId={syncingAccountId}
            onSync={handleSync}
            onRefreshAll={() => refetch()}
            isRefreshing={isLoadingAccounts}
          />
        </div>
      )}

      {/* Connect Account Modal */}
      {modalPlatform && (
        <ConnectAccountModal
          platform={modalPlatform}
          config={PLATFORM_CONFIGS[modalPlatform]}
          isOpen={Boolean(modalPlatform)}
          isSubmitting={connectMutation.isPending}
          onClose={handleCloseModal}
          onSubmit={handleSubmitConnect}
        />
      )}
    </div>
  );
};
