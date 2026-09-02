import React, { useState, useMemo } from 'react';
import { Trophy } from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useLeaderboard } from '@/hooks/useLeaderboard';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { LeaderboardPeriod } from '@/types/leaderboard';
import { filterRealUsers } from '@/lib/userFilter';

export const LeaderboardPage: React.FC = () => {
  const { activeTeam, activeUser } = useApp();
  const [period, setPeriod] = useState<LeaderboardPeriod>('ALL_TIME');

  const { data: leaderboardPage, isLoading } = useLeaderboard(activeTeam?.id, period, { size: 1000 });

  const rawEntries = leaderboardPage?.content || [];
  // Strictly filter out internal system/admin accounts
  const entries = useMemo(() => filterRealUsers(rawEntries), [rawEntries]);

  const periods: { value: LeaderboardPeriod; label: string }[] = [
    { value: 'ALL_TIME', label: 'All-Time' },
    { value: 'MONTHLY', label: 'This Month' },
    { value: 'WEEKLY', label: 'This Week' },
    { value: 'DAILY', label: 'Today' },
  ];

  return (
    <div className="space-y-4">
      {/* 1. Header & Period Tabs */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Trophy className="w-4 h-4 text-amber-500" />
            Team Standings & Scoring Leaderboard
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Team: <span className="font-semibold text-slate-700 dark:text-slate-200">{activeTeam?.name || 'Developer Workspace'}</span> • +10 pts Daily Progress, +20 pts Task, +50 pts Goal
          </p>
        </div>

        {/* Period Selector */}
        <div className="flex border border-[#cbd5e1] dark:border-slate-700 rounded-[2px] overflow-hidden">
          {periods.map((p) => (
            <button
              key={p.value}
              onClick={() => setPeriod(p.value)}
              className={`px-3 py-1 text-xs font-semibold ${
                period === p.value
                  ? 'bg-blue-600 text-white'
                  : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700'
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>

      {/* 2. Top 3 Standings Highlight Boxes */}
      {entries.length >= 3 && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {/* Rank 2 */}
          <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 flex items-center gap-1">
                🥈 2nd Place
              </span>
              <Badge size="sm" variant="default">
                {entries[1].score} pts
              </Badge>
            </div>
            <div className="font-bold text-sm text-slate-900 dark:text-slate-100 mt-2">{entries[1].userName}</div>
            <div className="text-[11px] text-slate-500">{entries[1].completedTasks} tasks • {entries[1].completedGoals} goals</div>
          </div>

          {/* Rank 1 */}
          <div className="bg-white dark:bg-slate-900 border-2 border-amber-400 dark:border-amber-500 p-3 rounded-[3px] bg-amber-50/20">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-amber-600 flex items-center gap-1">
                🥇 1st Place (Leader)
              </span>
              <Badge size="sm" variant="warning">
                {entries[0].score} pts
              </Badge>
            </div>
            <div className="font-bold text-base text-slate-900 dark:text-slate-100 mt-2">{entries[0].userName}</div>
            <div className="text-[11px] text-slate-500">{entries[0].completedTasks} tasks • {entries[0].completedGoals} goals</div>
          </div>

          {/* Rank 3 */}
          <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500 flex items-center gap-1">
                🥉 3rd Place
              </span>
              <Badge size="sm" variant="default">
                {entries[2].score} pts
              </Badge>
            </div>
            <div className="font-bold text-sm text-slate-900 dark:text-slate-100 mt-2">{entries[2].userName}</div>
            <div className="text-[11px] text-slate-500">{entries[2].completedTasks} tasks • {entries[2].completedGoals} goals</div>
          </div>
        </div>
      )}

      {/* 3. Standings Table */}
      <Card>
        <CardHeader>
          <CardTitle>Team Member Rankings</CardTitle>
          <span className="text-[11px] text-slate-500">{entries.length} Engineers Ranked</span>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : entries.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="classic-table">
                <thead>
                  <tr>
                    <th style={{ width: '60px' }}>Rank</th>
                    <th>Engineer</th>
                    <th>Tasks Completed</th>
                    <th>Goals Completed</th>
                    <th>Progress Logs</th>
                    <th>Badges Points</th>
                    <th className="text-right">Total Score</th>
                  </tr>
                </thead>
                <tbody>
                  {entries.map((item, idx) => {
                    const isCurrentUser = activeUser && item.userId === activeUser.id;
                    return (
                      <tr
                        key={item.userId}
                        className={isCurrentUser ? 'bg-blue-50/70 dark:bg-blue-950/30 font-semibold' : ''}
                      >
                        <td>
                          <div className="flex items-center gap-1">
                            {idx === 0 ? (
                              <span className="text-amber-500 font-bold">🥇 1</span>
                            ) : idx === 1 ? (
                              <span className="text-slate-500 font-bold">🥈 2</span>
                            ) : idx === 2 ? (
                              <span className="text-amber-700 font-bold">🥉 3</span>
                            ) : (
                              <span className="text-slate-400 font-mono text-[11px]">#{idx + 1}</span>
                            )}
                          </div>
                        </td>
                        <td>
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-slate-900 dark:text-slate-100">{item.userName}</span>
                            {isCurrentUser && (
                              <Badge size="sm" variant="primary">
                                YOU
                              </Badge>
                            )}
                          </div>
                        </td>
                        <td className="text-slate-700 dark:text-slate-300">
                          {item.completedTasks || 0}
                        </td>
                        <td className="text-slate-700 dark:text-slate-300">
                          {item.completedGoals || 0}
                        </td>
                        <td className="text-slate-700 dark:text-slate-300">
                          {item.progressEntries || 0}
                        </td>
                        <td className="text-slate-700 dark:text-slate-300">
                          {item.achievementPoints || 0} pts
                        </td>
                        <td className="text-right">
                          <span className="font-bold text-blue-700 dark:text-blue-400 text-sm">
                            {item.score} <span className="text-xs font-normal text-slate-400">pts</span>
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-6">
              <EmptyState
                icon={Trophy}
                title="No leaderboard standings recorded"
                description="Log daily progress or complete sprint tasks to start ranking on the team leaderboard."
              />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
