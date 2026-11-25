import React, { useState, useMemo } from 'react';
import {
  Trophy,
  Info,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { useAllContestHistories } from '@/hooks/useCodingPerformance';
import { CodingPlatform } from '@/types/codingIntelligence';
import { ContestRecordResponse } from '@/types/codingPerformance';

const CONTEST_PLATFORMS: { id: CodingPlatform; label: string; color: string; badgeBg: string }[] = [
  { id: 'LEETCODE', label: 'LeetCode', color: '#FFA116', badgeBg: 'bg-amber-500/10 text-amber-500 border-amber-500/20' },
  { id: 'CODEFORCES', label: 'Codeforces', color: '#1F8ACB', badgeBg: 'bg-blue-500/10 text-blue-500 border-blue-500/20' },
  { id: 'ATCODER', label: 'AtCoder', color: '#06b6d4', badgeBg: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20' },
  { id: 'CODECHEF', label: 'CodeChef', color: '#d97706', badgeBg: 'bg-orange-500/10 text-orange-400 border-orange-500/20' },
];

type TimeFilter = 'ALL' | '1Y' | '6M' | '90D';

export const DashboardContestSection: React.FC = () => {
  const [selectedPlatform, setSelectedPlatform] = useState<CodingPlatform>('LEETCODE');
  const [timeFilter, setTimeFilter] = useState<TimeFilter>('ALL');
  const [hoveredPoint, setHoveredPoint] = useState<ContestRecordResponse | null>(null);

  const { data: allHistories, isLoading } = useAllContestHistories();

  const currentHistory = allHistories ? allHistories[selectedPlatform] : undefined;
  const rawContests = currentHistory?.contests || [];

  // Filter contests by time range
  const filteredContests = useMemo(() => {
    if (!rawContests.length) return [];
    if (timeFilter === 'ALL') return rawContests;

    const now = new Date().getTime();
    let cutoffDays = 365;
    if (timeFilter === '6M') cutoffDays = 180;
    if (timeFilter === '90D') cutoffDays = 90;

    const cutoffTime = now - cutoffDays * 24 * 60 * 60 * 1000;
    return rawContests.filter((c) => new Date(c.contestDate).getTime() >= cutoffTime);
  }, [rawContests, timeFilter]);

  // Derived KPI Stats
  const kpis = useMemo(() => {
    const currentRating = currentHistory?.currentRating ?? (filteredContests.length > 0 ? filteredContests[filteredContests.length - 1].newRating : null);
    const peakRating = currentHistory?.highestRating ?? (filteredContests.length > 0 ? Math.max(...filteredContests.map((c) => c.newRating)) : null);
    const totalContests = currentHistory?.totalContests ?? rawContests.length;

    let bestRank: number | null = null;
    let totalDelta = 0;
    let positiveDeltas = 0;

    filteredContests.forEach((c) => {
      if (c.contestRank !== null && (bestRank === null || c.contestRank < bestRank)) {
        bestRank = c.contestRank;
      }
      if (c.ratingChange !== null) {
        totalDelta += c.ratingChange;
        if (c.ratingChange > 0) positiveDeltas++;
      }
    });

    const netDelta = filteredContests.length > 0 ? totalDelta : 0;
    const positiveRate = filteredContests.length > 0 ? Math.round((positiveDeltas / filteredContests.length) * 100) : 0;

    return {
      currentRating,
      peakRating,
      totalContests,
      bestRank,
      netDelta,
      positiveRate,
    };
  }, [currentHistory, filteredContests, rawContests]);

  // Chart Dimensions & Coordinate Mapping
  const chartData = useMemo(() => {
    if (filteredContests.length < 2) return null;

    const ratings = filteredContests.map((c) => c.newRating);
    const minRating = Math.floor(Math.min(...ratings) / 50) * 50 - 50;
    const maxRating = Math.ceil(Math.max(...ratings) / 50) * 50 + 50;
    const range = maxRating - minRating || 1;

    const width = 800;
    const height = 220;
    const padding = { top: 20, right: 30, bottom: 30, left: 50 };

    const innerW = width - padding.left - padding.right;
    const innerH = height - padding.top - padding.bottom;

    const points = filteredContests.map((c, i) => {
      const x = padding.left + (i / (filteredContests.length - 1)) * innerW;
      const y = padding.top + innerH - ((c.newRating - minRating) / range) * innerH;
      return { x, y, record: c };
    });

    const linePath = points.reduce((acc, p, i) => `${acc} ${i === 0 ? 'M' : 'L'} ${p.x.toFixed(1)} ${p.y.toFixed(1)}`, '');
    const areaPath = `${linePath} L ${points[points.length - 1].x.toFixed(1)} ${padding.top + innerH} L ${points[0].x.toFixed(1)} ${padding.top + innerH} Z`;

    const yTicks = [
      minRating,
      Math.round(minRating + range * 0.33),
      Math.round(minRating + range * 0.66),
      maxRating,
    ];

    return {
      width,
      height,
      padding,
      minRating,
      maxRating,
      points,
      linePath,
      areaPath,
      yTicks,
      innerH,
      innerW,
    };
  }, [filteredContests]);

  const activeMeta = CONTEST_PLATFORMS.find((p) => p.id === selectedPlatform) || CONTEST_PLATFORMS[0];

  return (
    <Card className="rounded-[3px] border border-[#cfd5dc] dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm overflow-hidden">
      <CardHeader className="p-4 sm:p-5 border-b border-[#cfd5dc] dark:border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-50/50 dark:bg-slate-800/40">
        <div>
          <div className="flex items-center gap-2">
            <Trophy className="w-5 h-5 text-amber-500" />
            <CardTitle className="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wider">
              Contest Performance & Rating Intelligence
            </CardTitle>
          </div>
          <p className="text-xs text-slate-700 dark:text-slate-300 mt-1">
            Platform-native contest progression, rating telemetry, and competitive programming participation history
          </p>
        </div>

        {/* Platform Selector Tabs */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
          {CONTEST_PLATFORMS.map((platform) => {
            const hist = allHistories ? allHistories[platform.id] : undefined;
            const count = hist?.totalContests ?? 0;
            const isSelected = selectedPlatform === platform.id;

            return (
              <button
                key={platform.id}
                onClick={() => setSelectedPlatform(platform.id)}
                className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-[2px] border transition-colors whitespace-nowrap ${
                  isSelected
                    ? 'bg-blue-600 text-white border-blue-600 shadow-xs'
                    : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/60'
                }`}
              >
                <span
                  className="w-2 h-2 rounded-full"
                  style={{ backgroundColor: platform.color }}
                />
                <span>{platform.label}</span>
                {count > 0 && (
                  <span
                    className={`text-[10px] px-1 py-0.2 rounded-[2px] font-bold ${
                      isSelected ? 'bg-blue-700 text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300'
                    }`}
                  >
                    {count}
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </CardHeader>

      <CardContent className="p-4 sm:p-5 space-y-6">
        {/* KPI Summary Cards */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Current Rating</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-slate-900 dark:text-white">
                {kpis.currentRating ? Math.round(kpis.currentRating) : '—'}
              </span>
              <span className="text-[11px] font-medium text-slate-600 dark:text-slate-400">
                {activeMeta.label}
              </span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Peak Rating</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-amber-500">
                {kpis.peakRating ? Math.round(kpis.peakRating) : '—'}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">all-time high</span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Total Contests</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-blue-600 dark:text-blue-400">
                {kpis.totalContests}
              </span>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">participations</span>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800">
            <div className="text-[11px] font-medium text-slate-700 dark:text-slate-300 uppercase tracking-wider">Best Global Rank</div>
            <div className="mt-1 flex items-baseline gap-1.5">
              <span className="text-xl font-bold text-emerald-600 dark:text-emerald-400">
                {kpis.bestRank !== null ? `#${Number(kpis.bestRank).toLocaleString()}` : '—'}
              </span>
              {kpis.netDelta !== 0 && (
                <span className={`text-[11px] font-bold ${kpis.netDelta > 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                  {kpis.netDelta > 0 ? `+${Math.round(kpis.netDelta)}` : Math.round(kpis.netDelta)}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Rating Progression Graph Section */}
        <div className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                {activeMeta.label} Rating Progression
              </h4>
              <p className="text-[11px] text-slate-700 dark:text-slate-300">
                Historical rating trajectory per contest round
              </p>
            </div>

            {/* Time Filter Buttons */}
            <div className="flex items-center gap-1 self-start sm:self-auto">
              {(['ALL', '1Y', '6M', '90D'] as TimeFilter[]).map((tf) => (
                <button
                  key={tf}
                  onClick={() => setTimeFilter(tf)}
                  className={`px-2 py-1 text-[11px] font-semibold rounded-[2px] transition-colors ${
                    timeFilter === tf
                      ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                      : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                  }`}
                >
                  {tf === 'ALL' ? 'All Time' : tf}
                </button>
              ))}
            </div>
          </div>

          {/* SVG Rating Progression Chart */}
          <div className="relative border border-[#cfd5dc] dark:border-slate-800 rounded-[3px] p-3 bg-white dark:bg-slate-950/60 overflow-hidden">
            {isLoading ? (
              <div className="h-[220px] flex items-center justify-center text-xs text-slate-600 dark:text-slate-400">
                Loading contest rating telemetry...
              </div>
            ) : chartData ? (
              <div className="relative w-full overflow-x-auto">
                <svg
                  viewBox={`0 0 ${chartData.width} ${chartData.height}`}
                  className="w-full h-auto min-w-[500px]"
                >
                  <defs>
                    <linearGradient id={`gradient-${selectedPlatform}`} x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor={activeMeta.color} stopOpacity="0.28" />
                      <stop offset="100%" stopColor={activeMeta.color} stopOpacity="0.0" />
                    </linearGradient>
                  </defs>

                  {/* Horizontal grid lines & Y labels */}
                  {chartData.yTicks.map((val) => {
                    const y =
                      chartData.padding.top +
                      chartData.innerH -
                      ((val - chartData.minRating) / (chartData.maxRating - chartData.minRating || 1)) *
                        chartData.innerH;

                    return (
                      <g key={val}>
                        <line
                          x1={chartData.padding.left}
                          y1={y}
                          x2={chartData.width - chartData.padding.right}
                          y2={y}
                          stroke="#e2e8f0"
                          strokeDasharray="4 4"
                          className="dark:stroke-slate-800"
                        />
                        <text
                          x={chartData.padding.left - 8}
                          y={y + 3}
                          textAnchor="end"
                          fontSize="10"
                          fill="#94a3b8"
                          className="font-mono font-medium"
                        >
                          {val}
                        </text>
                      </g>
                    );
                  })}

                  {/* Area fill */}
                  <path d={chartData.areaPath} fill={`url(#gradient-${selectedPlatform})`} />

                  {/* Trend line */}
                  <path
                    d={chartData.linePath}
                    fill="none"
                    stroke={activeMeta.color}
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />

                  {/* Interactive points */}
                  {chartData.points.map((p, idx) => (
                    <circle
                      key={idx}
                      cx={p.x}
                      cy={p.y}
                      r={hoveredPoint?.contestId === p.record.contestId ? '5.5' : '3.5'}
                      fill={hoveredPoint?.contestId === p.record.contestId ? '#ffffff' : activeMeta.color}
                      stroke={activeMeta.color}
                      strokeWidth={hoveredPoint?.contestId === p.record.contestId ? '3' : '1.5'}
                      className="cursor-pointer transition-all duration-150"
                      onMouseEnter={() => {
                        setHoveredPoint(p.record);
                      }}
                      onMouseLeave={() => setHoveredPoint(null)}
                    />
                  ))}
                </svg>

                {/* Inline Hover Tooltip */}
                {hoveredPoint && (
                  <div className="absolute top-2 right-2 bg-slate-900 text-white dark:bg-slate-800 p-2.5 rounded-[3px] shadow-lg border border-slate-700 text-xs pointer-events-none z-10 max-w-[240px]">
                    <div className="font-semibold text-slate-100 truncate">{hoveredPoint.contestName}</div>
                    <div className="text-[11px] text-slate-400 mt-0.5">
                      {new Date(hoveredPoint.contestDate).toLocaleDateString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                      })}
                    </div>
                    <div className="mt-1.5 flex items-center justify-between gap-3 pt-1 border-t border-slate-700/60 font-mono text-[11px]">
                      <span>Rating: <strong className="text-white">{Math.round(hoveredPoint.newRating)}</strong></span>
                      {hoveredPoint.ratingChange !== null && (
                        <span className={hoveredPoint.ratingChange >= 0 ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'}>
                          {hoveredPoint.ratingChange >= 0 ? `+${Math.round(hoveredPoint.ratingChange)}` : Math.round(hoveredPoint.ratingChange)}
                        </span>
                      )}
                    </div>
                    {hoveredPoint.contestRank && (
                      <div className="text-[10px] text-slate-400 mt-1">
                        Rank: #{hoveredPoint.contestRank.toLocaleString()}
                        {hoveredPoint.problemsSolved !== null ? ` • Solved: ${hoveredPoint.problemsSolved}` : ''}
                      </div>
                    )}
                  </div>
                )}
              </div>
            ) : (
              <div className="h-[180px] flex flex-col items-center justify-center text-center p-4">
                <Info className="w-8 h-8 text-slate-400 dark:text-slate-600 mb-2" />
                <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                  {rawContests.length === 0
                    ? `No contest participation recorded on ${activeMeta.label} yet`
                    : 'Insufficient data points to render progression graph'}
                </p>
                <p className="text-[11px] text-slate-600 dark:text-slate-400 mt-1 max-w-sm">
                  {rawContests.length === 0
                    ? `Connect your public ${activeMeta.label} profile and participate in rated rounds to track rating trajectory.`
                    : 'Adjust the time range filter above to view all historical rounds.'}
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Contest Participation History Table */}
        {rawContests.length > 0 && (
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h4 className="text-xs font-bold text-slate-900 dark:text-white uppercase tracking-wider">
                Recent Contest Rounds ({rawContests.length})
              </h4>
              <span className="text-[11px] text-slate-600 dark:text-slate-400">
                Sorted by most recent participation
              </span>
            </div>

            <div className="border border-[#cfd5dc] dark:border-slate-800 rounded-[3px] overflow-hidden">
              <div className="overflow-x-auto max-h-[260px] overflow-y-auto">
                <table className="w-full text-left text-xs border-collapse">
                  <thead className="bg-slate-50 dark:bg-slate-800/80 sticky top-0 z-1 text-[11px] font-semibold text-slate-700 dark:text-slate-300 border-b border-[#cfd5dc] dark:border-slate-800">
                    <tr>
                      <th className="py-2.5 px-3">Contest Name</th>
                      <th className="py-2.5 px-3">Date</th>
                      <th className="py-2.5 px-3">Rank</th>
                      <th className="py-2.5 px-3">Problems</th>
                      <th className="py-2.5 px-3 text-right">Delta</th>
                      <th className="py-2.5 px-3 text-right">New Rating</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 dark:divide-slate-800 font-mono text-[11px]">
                    {[...rawContests].reverse().slice(0, 10).map((c, i) => (
                      <tr key={c.contestId || i} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/50 transition-colors">
                        <td className="py-2 px-3 font-sans font-medium text-slate-900 dark:text-white truncate max-w-[220px]">
                          {c.contestName}
                        </td>
                        <td className="py-2 px-3 text-slate-600 dark:text-slate-400 whitespace-nowrap font-sans">
                          {new Date(c.contestDate).toLocaleDateString('en-US', {
                            month: 'short',
                            day: 'numeric',
                            year: 'numeric',
                          })}
                        </td>
                        <td className="py-2 px-3 text-slate-700 dark:text-slate-300">
                          {c.contestRank ? `#${c.contestRank.toLocaleString()}` : '—'}
                        </td>
                        <td className="py-2 px-3 text-slate-700 dark:text-slate-300">
                          {c.problemsSolved !== null ? c.problemsSolved : '—'}
                        </td>
                        <td className="py-2 px-3 text-right">
                          {c.ratingChange !== null ? (
                            <span
                              className={`font-bold inline-flex items-center gap-0.5 ${
                                c.ratingChange > 0
                                  ? 'text-emerald-600 dark:text-emerald-400'
                                  : c.ratingChange < 0
                                  ? 'text-rose-600 dark:text-rose-400'
                                  : 'text-slate-500'
                              }`}
                            >
                              {c.ratingChange > 0 ? `+${Math.round(c.ratingChange)}` : Math.round(c.ratingChange)}
                            </span>
                          ) : (
                            <span className="text-slate-400">—</span>
                          )}
                        </td>
                        <td className="py-2 px-3 text-right font-bold text-slate-900 dark:text-white">
                          {Math.round(c.newRating)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
