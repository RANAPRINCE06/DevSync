import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  Code2,
  Trophy,
  CheckCircle2,
  Layers,
  ArrowUpRight,
  TrendingUp,
  BarChart2,
  GitBranch,
  Flame,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import {
  useConnectedAccounts,
  useCodingAnalyticsOverview,
  useRatingHistory,
  useProblemDistribution,
  useCodingActivity,
  useContestPerformance,
} from '@/hooks/useCodingIntelligence';
import { RatingProgressionSection } from '@/pages/CodingIntelligence/components/RatingProgressionSection';
import { ProblemDistributionSection } from '@/pages/CodingIntelligence/components/ProblemDistributionSection';
import { ContestPerformanceSection } from '@/pages/CodingIntelligence/components/ContestPerformanceSection';
import { GitHubInsightsSection } from '@/pages/CodingIntelligence/components/GitHubInsightsSection';
import { CodingActivitySection } from '@/pages/CodingIntelligence/components/CodingActivitySection';
import { PlatformComparisonSection } from '@/pages/CodingIntelligence/components/PlatformComparisonSection';
import { PLATFORM_CONFIGS } from '@/pages/CodingIntelligence/CodingIntelligencePage';

export const CodingIntelligenceAnalytics: React.FC = () => {
  const navigate = useNavigate();
  const [period, setPeriod] = useState<'7d' | '30d' | '90d' | '1y' | 'all'>('30d');
  const [selectedSubTab, setSelectedSubTab] = useState<'overview' | 'ratings' | 'problems' | 'contests' | 'github' | 'consistency' | 'comparison'>('overview');

  const { data: accounts = [] } = useConnectedAccounts();
  const { data: overview } = useCodingAnalyticsOverview();
  const { data: ratingData } = useRatingHistory({ period });
  const { data: problemData } = useProblemDistribution({ period });
  const { data: activityData } = useCodingActivity({ period });
  const { data: contestData } = useContestPerformance({ period });

  const activeAccounts = accounts.filter((a) => a.active);
  const leetCodeAccount = activeAccounts.find((a) => a.platform === 'LEETCODE');
  const codeforcesAccount = activeAccounts.find((a) => a.platform === 'CODEFORCES');
  const atCoderAccount = activeAccounts.find((a) => a.platform === 'ATCODER');
  const gfgAccount = activeAccounts.find((a) => a.platform === 'GEEKSFORGEEKS');
  const gitHubAccount = activeAccounts.find((a) => a.platform === 'GITHUB');

  const gfgAdditional = gfgAccount?.latestSnapshot?.additionalStats as Record<string, unknown> | undefined;
  const atCoderAdditional = atCoderAccount?.latestSnapshot?.additionalStats as Record<string, unknown> | undefined;

  return (
    <div className="space-y-4">
      {/* 1. Header Banner & Sub-Navigation */}
      <div className="bg-gradient-to-r from-slate-900/95 via-indigo-950/70 to-slate-900/95 border border-indigo-500/30 p-4 rounded-xl shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shrink-0 shadow-inner">
            <Code2 className="w-5 h-5 text-indigo-400" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-slate-100 uppercase tracking-wider">
                Coding Intelligence & Performance Analytics
              </h2>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 font-semibold border border-indigo-500/30">
                7 Platforms
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Historical progression, competitive ratings, difficulty breakdowns, and GitHub analytics
            </p>
          </div>
        </div>

        {/* Date Filter & Quick Links */}
        <div className="flex items-center flex-wrap gap-2 text-xs">
          <div className="flex bg-slate-800/80 p-0.5 rounded-lg border border-slate-700">
            {(['7d', '30d', '90d', '1y', 'all'] as const).map((p) => (
              <button
                key={p}
                onClick={() => setPeriod(p)}
                className={`px-2.5 py-1 rounded text-[11px] font-semibold transition-all ${
                  period === p
                    ? 'bg-indigo-600 text-white shadow-xs'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {p.toUpperCase()}
              </button>
            ))}
          </div>

          <NavLink
            to="/coding"
            className="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-semibold transition-colors flex items-center gap-1"
          >
            Intelligence Dashboard <ArrowUpRight className="w-3.5 h-3.5" />
          </NavLink>
        </div>
      </div>

      {/* Sub-Tabs Navigation */}
      <div className="flex items-center gap-1.5 border-b border-slate-700/80 pb-2 overflow-x-auto text-xs">
        {[
          { key: 'overview', label: 'Overview & KPIs', icon: BarChart2 },
          { key: 'ratings', label: 'Rating Progression', icon: Trophy },
          { key: 'problems', label: 'Problem Solving', icon: CheckCircle2 },
          { key: 'contests', label: 'Contest Rounds', icon: TrendingUp },
          { key: 'github', label: 'GitHub Ecosystem', icon: GitBranch },
          { key: 'consistency', label: 'Coding Consistency', icon: Flame },
          { key: 'comparison', label: 'Platform Comparison', icon: Layers },
        ].map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => setSelectedSubTab(key as typeof selectedSubTab)}
            className={`px-3 py-1.5 rounded-lg font-semibold flex items-center gap-1.5 shrink-0 transition-all ${
              selectedSubTab === key
                ? 'bg-indigo-600 text-white shadow-xs'
                : 'bg-slate-800/60 text-slate-400 hover:bg-slate-800 hover:text-slate-200 border border-slate-700/60'
            }`}
          >
            <Icon className="w-3.5 h-3.5" />
            {label}
          </button>
        ))}
      </div>

      {/* SUB-TAB 1: Overview & KPIs */}
      {selectedSubTab === 'overview' && (
        <div className="space-y-4 animate-in fade-in">
          {/* Key Metric Boxes */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
            <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">
                Platform-Reported Solved
              </span>
              <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">
                {overview?.totalProblemsSolved ?? '—'} <span className="text-xs font-normal text-slate-400">problems</span>
              </div>
              <span className="text-[10px] text-slate-400 mt-0.5 block">
                LeetCode, Codeforces, AtCoder, GFG
              </span>
            </div>

            <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">
                Active Integrations
              </span>
              <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">
                {activeAccounts.length} <span className="text-xs font-normal text-slate-400">/ 7 platforms</span>
              </div>
              <span className="text-[10px] text-slate-400 mt-0.5 block">
                {overview?.activeSyncPlatformsCount ?? 0} auto-synchronized
              </span>
            </div>

            <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">
                Peak CP Rating
              </span>
              <div className="text-xl font-bold text-blue-600 dark:text-blue-400 mt-1">
                {overview?.highestRating != null ? Math.round(overview.highestRating) : '—'}
              </div>
              <span className="text-[10px] text-slate-400 mt-0.5 block truncate">
                {overview?.highestRatingPlatform ? `Platform: ${overview.highestRatingPlatform}` : 'No rating recorded'}
              </span>
            </div>

            <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">
                GitHub Repos & Stars
              </span>
              <div className="text-xl font-bold text-purple-400 mt-1">
                {overview?.totalGitHubRepositories != null ? `${overview.totalGitHubRepositories} repos` : '—'}
              </div>
              <span className="text-[10px] text-purple-400 mt-0.5 block">
                ★ {overview?.totalGitHubStars != null ? `${overview.totalGitHubStars} total stars` : '0 stars'}
              </span>
            </div>
          </div>

          {/* Platform Performance Cards Summary */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {/* LeetCode Card */}
            {leetCodeAccount && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-amber-500">LeetCode Performance</CardTitle>
                  <span className="text-[11px] text-slate-400">@{leetCodeAccount.username}</span>
                </CardHeader>
                <CardContent className="space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-400">Contest Rating:</span>
                    <span className="font-bold text-amber-400">
                      {leetCodeAccount.latestSnapshot?.currentRating != null
                        ? Math.round(leetCodeAccount.latestSnapshot.currentRating)
                        : '—'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Solved Problems:</span>
                    <span className="font-bold text-slate-100">
                      {leetCodeAccount.latestSnapshot?.problemsSolved ?? '—'}
                    </span>
                  </div>
                  {leetCodeAccount.latestSnapshot?.easySolved != null && (
                    <div className="text-[10px] text-slate-400 font-mono text-right">
                      {leetCodeAccount.latestSnapshot.easySolved} Easy / {leetCodeAccount.latestSnapshot.mediumSolved} Med / {leetCodeAccount.latestSnapshot.hardSolved} Hard
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            {/* Codeforces Card */}
            {codeforcesAccount && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-blue-400">Codeforces Telemetry</CardTitle>
                  <span className="text-[11px] text-slate-400">@{codeforcesAccount.username}</span>
                </CardHeader>
                <CardContent className="space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-400">Rating:</span>
                    <span className="font-bold text-blue-400">
                      {codeforcesAccount.latestSnapshot?.currentRating != null
                        ? Math.round(codeforcesAccount.latestSnapshot.currentRating)
                        : '—'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Unique AC Solved:</span>
                    <span className="font-bold text-slate-100">
                      {codeforcesAccount.latestSnapshot?.problemsSolved ?? '—'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Contests Attended:</span>
                    <span className="text-slate-300">
                      {codeforcesAccount.latestSnapshot?.contestsParticipated ?? '—'} rounds
                    </span>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* GeeksforGeeks Card */}
            {gfgAccount && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-emerald-400">GeeksforGeeks Analytics</CardTitle>
                  <span className="text-[11px] text-slate-400">@{gfgAccount.username}</span>
                </CardHeader>
                <CardContent className="space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-400">Coding Score:</span>
                    <span className="font-bold text-emerald-400">
                      {gfgAdditional?.codingScore != null ? Number(gfgAdditional.codingScore).toLocaleString() : '—'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Problems Solved:</span>
                    <span className="font-bold text-slate-100">
                      {gfgAccount.latestSnapshot?.problemsSolved ?? '—'}
                    </span>
                  </div>
                  {gfgAdditional?.instituteRank != null && (
                    <div className="flex justify-between">
                      <span className="text-slate-400">Institute Rank:</span>
                      <span className="text-slate-300">#{String(gfgAdditional.instituteRank)}</span>
                    </div>
                  )}
                  {gfgAdditional?.currentStreak != null && (
                    <div className="flex justify-between">
                      <span className="text-slate-400">Active Streak:</span>
                      <span className="text-slate-300">{String(gfgAdditional.currentStreak)} days</span>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            {/* AtCoder Card */}
            {atCoderAccount && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-cyan-400">AtCoder Community Metrics</CardTitle>
                  <span className="text-[11px] text-slate-400">@{atCoderAccount.username}</span>
                </CardHeader>
                <CardContent className="space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-400">Accepted (AC):</span>
                    <span className="font-bold text-cyan-400">
                      {atCoderAccount.latestSnapshot?.problemsSolved ?? '—'}
                    </span>
                  </div>
                  {atCoderAdditional?.communityAcRank != null && (
                    <div className="flex justify-between">
                      <span className="text-slate-400">Community Rank:</span>
                      <span className="text-slate-300 font-medium">
                        #{Number(atCoderAdditional.communityAcRank).toLocaleString()}
                      </span>
                    </div>
                  )}
                  {atCoderAdditional?.ratedPointSum != null && (
                    <div className="flex justify-between">
                      <span className="text-slate-400">Rated Points:</span>
                      <span className="text-slate-300 font-medium">
                        {Number(atCoderAdditional.ratedPointSum).toLocaleString()}
                      </span>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            {/* GitHub Card */}
            {gitHubAccount && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-purple-400">GitHub Ecosystem</CardTitle>
                  <span className="text-[11px] text-slate-400">@{gitHubAccount.username}</span>
                </CardHeader>
                <CardContent className="space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-400">Public Repositories:</span>
                    <span className="font-bold text-slate-100">
                      {overview?.totalGitHubRepositories != null ? overview.totalGitHubRepositories : '—'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Total Stars Earned:</span>
                    <span className="font-bold text-purple-400">
                      ★ {overview?.totalGitHubStars != null ? overview.totalGitHubStars : 0}
                    </span>
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      )}

      {/* SUB-TAB 2: Rating Progression */}
      {selectedSubTab === 'ratings' && (
        <div className="animate-in fade-in">
          <RatingProgressionSection ratingData={ratingData} isLoading={false} />
        </div>
      )}

      {/* SUB-TAB 3: Problem Solving Breakdown */}
      {selectedSubTab === 'problems' && (
        <div className="animate-in fade-in">
          <ProblemDistributionSection problemData={problemData} isLoading={false} />
        </div>
      )}

      {/* SUB-TAB 4: Contest Rounds */}
      {selectedSubTab === 'contests' && (
        <div className="animate-in fade-in">
          <ContestPerformanceSection contestData={contestData} isLoading={false} />
        </div>
      )}

      {/* SUB-TAB 5: GitHub Ecosystem */}
      {selectedSubTab === 'github' && (
        <div className="animate-in fade-in">
          <GitHubInsightsSection gitHubAccount={gitHubAccount} onConnectGitHub={() => navigate('/coding-profiles')} />
        </div>
      )}

      {/* SUB-TAB 6: Coding Consistency Timeline */}
      {selectedSubTab === 'consistency' && (
        <div className="animate-in fade-in">
          <CodingActivitySection activityData={activityData} isLoading={false} />
        </div>
      )}

      {/* SUB-TAB 7: Platform Comparison */}
      {selectedSubTab === 'comparison' && (
        <div className="animate-in fade-in">
          <PlatformComparisonSection accounts={accounts} platformConfigs={PLATFORM_CONFIGS} />
        </div>
      )}
    </div>
  );
};
