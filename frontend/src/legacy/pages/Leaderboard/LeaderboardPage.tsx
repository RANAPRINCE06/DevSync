import React, { useState } from 'react';
import { Trophy } from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Tabs } from '@/components/ui/Tabs';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { Pagination } from '@/components/ui/Pagination';
import { useLeaderboard } from '@/hooks/useLeaderboard';
import { LeaderboardPeriod } from '@/types/leaderboard';

export const LeaderboardPage: React.FC = () => {
  const { activeUser, activeTeam } = useApp();
  const [period, setPeriod] = useState<LeaderboardPeriod>('ALL_TIME');
  const [page, setPage] = useState(0);

  const { data: leaderboardData, isLoading } = useLeaderboard(activeTeam?.id, period, {
    page,
    size: 20,
    sort: 'score,desc',
  });

  const periodTabs = [
    { id: 'DAILY', label: 'Today' },
    { id: 'WEEKLY', label: 'This Week' },
    { id: 'MONTHLY', label: 'This Month' },
    { id: 'ALL_TIME', label: 'All Time' },
  ];

  const top3 = leaderboardData?.content?.slice(0, 3) || [];
  const entries = leaderboardData?.content || [];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Header & Period Tabs */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Trophy className="w-5 h-5 text-amber-400" />
            Team Leaderboard & Standings
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time rankings based on daily consistency, task execution, and milestone achievements.
          </p>
        </div>

        <Tabs
          tabs={periodTabs}
          activeTab={period}
          onChange={(tabId) => {
            setPeriod(tabId as LeaderboardPeriod);
            setPage(0);
          }}
        />
      </div>

      {/* 2. Top-3 Podium Display */}
      {top3.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
          {top3.map((entry) => {
            const medal = entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : '🥉';
            const isFirst = entry.rank === 1;
            const isCurrentUser = activeUser && entry.userId === activeUser.id;

            return (
              <Card
                key={entry.userId}
                className={`flex flex-col items-center text-center p-5 relative overflow-hidden transition-all ${
                  isFirst
                    ? 'bg-gradient-to-b from-amber-950/40 via-slate-900 to-slate-900 border-amber-500/50 shadow-glow sm:-translate-y-2'
                    : 'bg-slate-900/80 border-slate-800'
                } ${isCurrentUser ? 'ring-2 ring-primary-500/50' : ''}`}
              >
                <span className="text-4xl mb-2">{medal}</span>
                <span className="text-sm font-bold text-slate-100 truncate max-w-full">
                  {entry.userName}
                </span>
                <span className="text-2xl font-extrabold text-primary-300 mt-1">
                  {entry.score} <span className="text-xs font-normal text-slate-400">pts</span>
                </span>

                <div className="w-full grid grid-cols-3 gap-2 mt-4 pt-3 border-t border-slate-800/80 text-[10px] text-slate-400">
                  <div>
                    <span className="block font-bold text-slate-200">{entry.completedTasks}</span>
                    <span>Tasks</span>
                  </div>
                  <div>
                    <span className="block font-bold text-slate-200">{entry.progressEntries}</span>
                    <span>Logs</span>
                  </div>
                  <div>
                    <span className="block font-bold text-slate-200">{entry.achievementPoints}</span>
                    <span>Badges</span>
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* 3. Score Breakdown Legend */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-primary-950 border border-primary-800 flex items-center justify-center text-primary-400 font-bold text-xs">
            +10
          </div>
          <div className="text-[11px]">
            <span className="font-semibold text-slate-200 block">Daily Progress</span>
            <span className="text-slate-500">Per verified log</span>
          </div>
        </div>

        <div className="p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-emerald-950 border border-emerald-800 flex items-center justify-center text-emerald-400 font-bold text-xs">
            +20
          </div>
          <div className="text-[11px]">
            <span className="font-semibold text-slate-200 block">Completed Task</span>
            <span className="text-slate-500">Per sprint item</span>
          </div>
        </div>

        <div className="p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-indigo-950 border border-indigo-800 flex items-center justify-center text-indigo-400 font-bold text-xs">
            +50
          </div>
          <div className="text-[11px]">
            <span className="font-semibold text-slate-200 block">Completed Goal</span>
            <span className="text-slate-500">100% milestone</span>
          </div>
        </div>

        <div className="p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-amber-950 border border-amber-800 flex items-center justify-center text-amber-400 font-bold text-xs">
            +pts
          </div>
          <div className="text-[11px]">
            <span className="font-semibold text-slate-200 block">Achievements</span>
            <span className="text-slate-500">Badge point value</span>
          </div>
        </div>
      </div>

      {/* 4. Full Ranked Leaderboard Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between w-full">
            <CardTitle>Complete Standings</CardTitle>
            <span className="text-xs text-slate-500">{entries.length} active members</span>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
            </div>
          ) : entries.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300">
                <thead className="bg-slate-950/60 text-slate-400 uppercase text-[10px] font-semibold border-b border-slate-800">
                  <tr>
                    <th className="px-4 py-3 w-16 text-center">Rank</th>
                    <th className="px-4 py-3">Developer</th>
                    <th className="px-4 py-3">Tasks Completed (+20)</th>
                    <th className="px-4 py-3">Progress Logs (+10)</th>
                    <th className="px-4 py-3">Goals (+50)</th>
                    <th className="px-4 py-3">Achievement Points</th>
                    <th className="px-4 py-3 text-right">Total Score</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {entries.map((entry) => {
                    const isCurrentUser = activeUser && entry.userId === activeUser.id;
                    const medalEmoji =
                      entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : entry.rank === 3 ? '🥉' : `#${entry.rank}`;

                    return (
                      <tr
                        key={entry.userId}
                        className={`transition-colors ${
                          isCurrentUser ? 'bg-primary-950/20 font-medium' : 'hover:bg-slate-850/50'
                        }`}
                      >
                        <td className="px-4 py-3 text-center text-sm font-bold text-slate-200">
                          {medalEmoji}
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <span className="font-semibold text-slate-100">{entry.userName}</span>
                            {isCurrentUser && (
                              <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-primary-500/20 text-primary-300 font-bold border border-primary-500/30">
                                You
                              </span>
                            )}
                          </div>
                        </td>
                        <td className="px-4 py-3 text-slate-300">{entry.completedTasks}</td>
                        <td className="px-4 py-3 text-slate-300">{entry.progressEntries}</td>
                        <td className="px-4 py-3 text-slate-300">{entry.completedGoals}</td>
                        <td className="px-4 py-3 text-amber-400 font-medium">
                          {entry.achievementPoints} pts
                        </td>
                        <td className="px-4 py-3 text-right font-extrabold text-primary-300 text-sm">
                          {entry.score}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState
              icon={Trophy}
              title="No leaderboard data"
              description="Start submitting progress and completing tasks to see rankings."
            />
          )}

          {leaderboardData && leaderboardData.totalPages > 1 && (
            <Pagination
              currentPage={page}
              totalPages={leaderboardData.totalPages}
              totalElements={leaderboardData.totalElements}
              onPageChange={setPage}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
};
