import React, { useState, useMemo } from 'react';
import { NavLink } from 'react-router-dom';
import {
  GitCommit,
  GitBranch,
  Star,
  Flame,
  FolderGit2,
  ExternalLink,
  ChevronRight,
  Info,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import {
  useGitHubContributions,
  useGitHubCommitActivity,
  useGitHubRepositories,
} from '@/hooks/useCodingPerformance';
import { GitHubContributionDay } from '@/types/codingPerformance';

const INTENSITY_COLORS = [
  'bg-slate-100 dark:bg-slate-800 border-slate-200/50 dark:border-slate-700/50', // 0
  'bg-emerald-200 dark:bg-emerald-950 border-emerald-300 dark:border-emerald-800', // 1
  'bg-emerald-400 dark:bg-emerald-700 border-emerald-500 dark:border-emerald-600', // 2
  'bg-emerald-600 dark:bg-emerald-500 border-emerald-700 dark:border-emerald-400', // 3
  'bg-emerald-800 dark:bg-emerald-400 border-emerald-900 dark:border-emerald-300', // 4
];

const LANGUAGE_COLORS: Record<string, string> = {
  TypeScript: '#3178c6',
  JavaScript: '#f7df1e',
  Java: '#b07219',
  Python: '#3572A5',
  Go: '#00ADD8',
  Rust: '#dea584',
  'C++': '#f34b7d',
  C: '#555555',
  HTML: '#e34c26',
  CSS: '#563d7c',
  Shell: '#89e051',
  Kotlin: '#A97BFF',
};

export const DashboardGitHubSection: React.FC = () => {
  const [period, setPeriod] = useState<string>('90d');
  const [hoveredDay, setHoveredDay] = useState<GitHubContributionDay | null>(null);

  const { data: contributions, isLoading: isContribLoading } = useGitHubContributions();
  const { data: activity } = useGitHubCommitActivity(period);
  const { data: repositories = [] } = useGitHubRepositories();

  const isLinked = (contributions?.publicRepos ?? 0) > 0 || (contributions?.totalContributions ?? 0) > 0 || repositories.length > 0;

  // Build 52-Week Matrix (7 rows x 52 columns)
  const heatmapMatrix = useMemo(() => {
    const rawDays = contributions?.dailyContributions || [];
    if (!rawDays.length) return null;

    // Organize into weeks (52 weeks, each with up to 7 days)
    const weeks: GitHubContributionDay[][] = [];
    let currentWeek: GitHubContributionDay[] = [];

    rawDays.forEach((day) => {
      currentWeek.push(day);
      if (currentWeek.length === 7) {
        weeks.push(currentWeek);
        currentWeek = [];
      }
    });

    if (currentWeek.length > 0) {
      weeks.push(currentWeek);
    }

    // Month headers
    const monthLabels: { label: string; weekIndex: number }[] = [];
    let lastMonth = '';

    weeks.forEach((week, wIndex) => {
      if (week.length > 0) {
        const d = new Date(week[0].date);
        const m = d.toLocaleDateString('en-US', { month: 'short' });
        if (m !== lastMonth && wIndex > 0) {
          monthLabels.push({ label: m, weekIndex: wIndex });
          lastMonth = m;
        } else if (wIndex === 0) {
          lastMonth = m;
          monthLabels.push({ label: m, weekIndex: 0 });
        }
      }
    });

    return {
      weeks: weeks.slice(-52), // exactly 52 trailing weeks
      monthLabels,
    };
  }, [contributions]);

  // Daily Commit Max for scaling
  const maxDailyCommits = useMemo(() => {
    if (!activity?.dailyCommits?.length) return 1;
    return Math.max(...activity.dailyCommits.map((d) => d.count), 1);
  }, [activity]);

  return (
    <Card className="rounded-[3px] border border-[#cfd5dc] dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm overflow-hidden">
      <CardHeader className="p-4 sm:p-5 border-b border-[#cfd5dc] dark:border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-50/50 dark:bg-slate-800/40">
        <div>
          <div className="flex items-center gap-2">
            <GitCommit className="w-5 h-5 text-purple-500" />
            <CardTitle className="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wider">
              GitHub Developer Activity & Commit Analytics
            </CardTitle>
            {isLinked && (
              <span className="px-2 py-0.5 rounded-[2px] text-xs font-bold bg-purple-50 dark:bg-purple-950/60 text-purple-600 dark:text-purple-400 border border-purple-200 dark:border-purple-800">
                Connected
              </span>
            )}
          </div>
          <p className="text-xs text-slate-700 dark:text-slate-300 mt-1">
            52-week contribution heatmap matrix, daily and monthly commit velocity, and repository activity insights
          </p>
        </div>

        {/* External Link to Coding Profiles if unlinked */}
        {!isLinked && (
          <NavLink to="/coding-profiles">
            <Button variant="outline" size="sm" className="rounded-[2px] text-xs">
              Connect GitHub Profile
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          </NavLink>
        )}
      </CardHeader>

      <CardContent className="p-4 sm:p-5 space-y-6">
        {/* KPI Summary Cards */}
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-3">
          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Total Commits</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-slate-900 dark:text-white">
                {activity?.totalCommits ?? contributions?.totalContributions ?? 0}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">commits</span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Current Streak</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <Flame className="w-4 h-4 text-amber-500 fill-amber-500" />
              <span className="text-xl font-bold text-amber-500">
                {contributions?.currentStreak ?? 0}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">days</span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Longest Streak</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-indigo-600 dark:text-indigo-400">
                {contributions?.longestStreak ?? 0}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">days peak</span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Active Days</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-emerald-600 dark:text-emerald-400">
                {contributions?.activeDays ?? 0}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">active</span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Public Repos</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-purple-600 dark:text-purple-400">
                {contributions?.publicRepos ?? repositories.length}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">repos</span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Total Stars</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <Star className="w-4 h-4 text-amber-500 fill-amber-500" />
              <span className="text-xl font-bold text-amber-500">
                {contributions?.totalStars ?? 0}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">earned</span>
            </div>
          </div>
        </div>

        {/* 52-Week Contribution Heatmap */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                52-Week Contribution Matrix
              </h4>
              <p className="text-[11px] text-slate-700 dark:text-slate-300">
                {contributions?.totalContributions ?? 0} total contributions in the trailing 365 days
              </p>
            </div>

            {/* Hover Day Tooltip / Indicator */}
            {hoveredDay && (
              <div className="text-[11px] font-mono text-slate-800 dark:text-slate-200 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded-[2px] border border-slate-200 dark:border-slate-700">
                <strong>{hoveredDay.count}</strong> contributions on {hoveredDay.date}
              </div>
            )}
          </div>

          <div className="p-3.5 border border-[#cfd5dc] dark:border-slate-800 rounded-[3px] bg-slate-50/50 dark:bg-slate-950/60 overflow-x-auto">
            {isContribLoading ? (
              <div className="h-28 flex items-center justify-center text-xs text-slate-600 dark:text-slate-400">
                Loading contribution calendar...
              </div>
            ) : heatmapMatrix ? (
              <div className="inline-block min-w-[700px]">
                {/* Month labels */}
                <div className="flex text-[10px] text-slate-600 dark:text-slate-400 mb-1 ml-6 font-mono">
                  {heatmapMatrix.weeks.map((_, idx) => {
                    const match = heatmapMatrix.monthLabels.find((m) => m.weekIndex === idx);
                    return (
                      <div key={idx} className="w-3 text-center">
                        {match ? match.label : ''}
                      </div>
                    );
                  })}
                </div>

                {/* Heatmap Grid (7 rows Mon-Sun x 52 cols) */}
                <div className="flex gap-1">
                  {/* Day of Week Labels */}
                  <div className="flex flex-col justify-between text-[9px] text-slate-600 dark:text-slate-400 font-mono pr-1.5 h-[84px] select-none">
                    <span>Mon</span>
                    <span>Wed</span>
                    <span>Fri</span>
                  </div>

                  {/* Weeks columns */}
                  <div className="flex gap-1">
                    {heatmapMatrix.weeks.map((week, wIndex) => (
                      <div key={wIndex} className="flex flex-col gap-1">
                        {week.map((day, dIndex) => (
                          <div
                            key={day.date || dIndex}
                            onMouseEnter={() => setHoveredDay(day)}
                            onMouseLeave={() => setHoveredDay(null)}
                            className={`w-2.5 h-2.5 rounded-[1.5px] border cursor-pointer transition-transform hover:scale-125 hover:z-10 ${
                              INTENSITY_COLORS[day.intensity] || INTENSITY_COLORS[0]
                            }`}
                            title={`${day.count} contributions on ${day.date}`}
                          />
                        ))}
                      </div>
                    ))}
                  </div>
                </div>

                {/* Legend */}
                <div className="mt-3 flex items-center justify-between text-[11px] text-slate-600 dark:text-slate-400 pt-2 border-t border-slate-200/60 dark:border-slate-800">
                  <span>Trailing 52 Weeks Activity</span>
                  <div className="flex items-center gap-1.5">
                    <span>Less</span>
                    {INTENSITY_COLORS.map((cls, idx) => (
                      <div key={idx} className={`w-2.5 h-2.5 rounded-[1.5px] border ${cls}`} />
                    ))}
                    <span>More</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="h-28 flex flex-col items-center justify-center text-center p-4">
                <Info className="w-6 h-6 text-slate-400 dark:text-slate-600 mb-1" />
                <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                  GitHub profile not connected
                </p>
                <p className="text-[11px] text-slate-600 dark:text-slate-400 mt-0.5">
                  Link your public GitHub profile to see your 52-week contribution matrix.
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Daily & Monthly Commit Charts + Period Selector */}
        <div className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                Commit Activity Trends
              </h4>
              <p className="text-[11px] text-slate-700 dark:text-slate-300">
                Daily and monthly commit distribution over selected period
              </p>
            </div>

            {/* Period Filter */}
            <div className="flex items-center gap-1">
              {['30d', '90d', '1y'].map((p) => (
                <button
                  key={p}
                  onClick={() => setPeriod(p)}
                  className={`px-2.5 py-1 text-[11px] font-semibold rounded-[2px] transition-colors ${
                    period === p
                      ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                      : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                  }`}
                >
                  {p.toUpperCase()}
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
            {/* Daily Commits Bar Chart (2 cols) */}
            <div className="lg:col-span-2 border border-[#cfd5dc] dark:border-slate-800 rounded-[3px] p-3.5 bg-slate-50/50 dark:bg-slate-950/60 flex flex-col justify-between">
              <div className="flex items-center justify-between text-xs font-medium text-slate-700 dark:text-slate-300 mb-2">
                <span>Daily Commit Volume</span>
                <span className="font-mono text-[11px] text-slate-600 dark:text-slate-400">
                  Max: {maxDailyCommits} commits/day
                </span>
              </div>

              {activity?.dailyCommits && activity.dailyCommits.length > 0 ? (
                <div className="h-36 flex items-end gap-1 overflow-x-auto pt-4 pb-1">
                  {activity.dailyCommits.map((pt, i) => {
                    const heightPercent = Math.max(8, Math.round((pt.count / maxDailyCommits) * 100));
                    return (
                      <div
                        key={pt.date || i}
                        className="flex-1 min-w-[6px] max-w-[14px] flex flex-col items-center group relative cursor-pointer"
                      >
                        <div
                          style={{ height: `${heightPercent}%` }}
                          className={`w-full rounded-t-[1.5px] transition-all group-hover:brightness-110 ${
                            pt.count > 0 ? 'bg-purple-600 dark:bg-purple-500' : 'bg-slate-200 dark:bg-slate-800'
                          }`}
                        />
                        {/* Tooltip */}
                        <div className="absolute -top-7 hidden group-hover:flex bg-slate-900 text-white text-[10px] font-mono px-1.5 py-0.5 rounded-[2px] whitespace-nowrap shadow-sm z-10 pointer-events-none">
                          {pt.date}: {pt.count}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="h-36 flex items-center justify-center text-xs text-slate-600 dark:text-slate-400">
                  No daily commit telemetry recorded in this timeframe
                </div>
              )}

              <div className="flex items-center justify-between text-[10px] text-slate-600 dark:text-slate-400 pt-2 border-t border-slate-200 dark:border-slate-800 mt-2">
                <span>7 Days: <strong className="text-slate-900 dark:text-white">{activity?.last7DaysCommits ?? 0}</strong></span>
                <span>30 Days: <strong className="text-slate-900 dark:text-white">{activity?.last30DaysCommits ?? 0}</strong></span>
                <span>90 Days: <strong className="text-slate-900 dark:text-white">{activity?.last90DaysCommits ?? 0}</strong></span>
              </div>
            </div>

            {/* Monthly Commit Breakdown (1 col) */}
            <div className="border border-[#cfd5dc] dark:border-slate-800 rounded-[3px] p-3.5 bg-slate-50/50 dark:bg-slate-950/60 flex flex-col justify-between">
              <div className="text-xs font-medium text-slate-700 dark:text-slate-300 mb-2">
                Monthly Aggregation
              </div>

              {activity?.monthlyCommits && activity.monthlyCommits.length > 0 ? (
                <div className="space-y-2 max-h-36 overflow-y-auto">
                  {activity.monthlyCommits.map((m) => {
                    const maxM = Math.max(...activity.monthlyCommits.map((item) => item.count), 1);
                    const pct = Math.round((m.count / maxM) * 100);

                    return (
                      <div key={m.month} className="space-y-1">
                        <div className="flex justify-between text-[11px] font-mono">
                          <span className="text-slate-700 dark:text-slate-300">{m.month}</span>
                          <span className="font-bold text-slate-900 dark:text-white">{m.count} commits</span>
                        </div>
                        <div className="w-full h-1.5 bg-slate-200 dark:bg-slate-800 rounded-[1px] overflow-hidden">
                          <div
                            style={{ width: `${pct}%` }}
                            className="h-full bg-purple-600 dark:bg-purple-500 rounded-[1px]"
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="h-36 flex items-center justify-center text-xs text-slate-600 dark:text-slate-400">
                  No monthly commits recorded
                </div>
              )}

              <div className="text-[10px] text-slate-600 dark:text-slate-400 pt-2 border-t border-slate-200 dark:border-slate-800 mt-2">
                Aggregated by push events
              </div>
            </div>
          </div>
        </div>

        {/* Repository Activity Insights */}
        {repositories.length > 0 && (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                  Top Public Repositories ({repositories.length})
                </h4>
                <p className="text-[11px] text-slate-700 dark:text-slate-300">
                  Repository insights, stars, forks, and development activity
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {repositories.slice(0, 6).map((repo) => {
                const langColor = repo.primaryLanguage
                  ? LANGUAGE_COLORS[repo.primaryLanguage] || '#64748b'
                  : '#64748b';

                return (
                  <div
                    key={repo.id}
                    className="p-3 bg-slate-50/70 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800 flex flex-col justify-between space-y-2 hover:border-purple-400 dark:hover:border-purple-600 transition-colors"
                  >
                    <div>
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-center gap-1.5 min-w-0">
                          <FolderGit2 className="w-4 h-4 text-purple-500 shrink-0" />
                          <a
                            href={repo.repoUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="font-semibold text-xs text-slate-900 dark:text-white hover:text-purple-600 dark:hover:text-purple-400 truncate flex items-center gap-1"
                          >
                            <span>{repo.repoName}</span>
                            <ExternalLink className="w-3 h-3 text-slate-400 shrink-0" />
                          </a>
                        </div>
                      </div>

                      {repo.description && (
                        <p className="text-[11px] text-slate-700 dark:text-slate-300 line-clamp-2 mt-1 leading-snug">
                          {repo.description}
                        </p>
                      )}
                    </div>

                    <div className="pt-2 border-t border-slate-200/60 dark:border-slate-700/60 flex items-center justify-between text-[11px] text-slate-600 dark:text-slate-400">
                      {repo.primaryLanguage ? (
                        <div className="flex items-center gap-1">
                          <span
                            className="w-2 h-2 rounded-full"
                            style={{ backgroundColor: langColor }}
                          />
                          <span>{repo.primaryLanguage}</span>
                        </div>
                      ) : (
                        <span className="text-slate-400">Unknown</span>
                      )}

                      <div className="flex items-center gap-2.5 font-mono text-[10px]">
                        <span className="flex items-center gap-0.5">
                          <Star className="w-3 h-3 text-amber-500 fill-amber-500" />
                          {repo.starsCount}
                        </span>
                        <span className="flex items-center gap-0.5">
                          <GitBranch className="w-3 h-3 text-slate-400" />
                          {repo.forksCount}
                        </span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
