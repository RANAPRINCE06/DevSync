import React from 'react';
import { BarChart3 } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useApp } from '@/contexts/AppContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { useProgressList } from '@/hooks/useProgress';
import { useGoals } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { useUserTotalPoints } from '@/hooks/useAchievements';
import { DailyProgress } from '@/types/progress';
import { Goal } from '@/types/goal';
import { Task } from '@/types/task';

export const AnalyticsPage: React.FC = () => {
  const { user } = useAuth();
  const { activeTeam } = useApp();

  const { data: progressPage, isLoading: isProgressLoading } = useProgressList({
    userId: user?.id,
    teamId: activeTeam?.id,
    size: 1000,
  });

  const { data: goalsPage } = useGoals({ teamId: activeTeam?.id, active: true, size: 1000 });
  const { data: tasksPage } = useTasks({ teamId: activeTeam?.id, active: true, size: 1000 });
  const { data: pointsData } = useUserTotalPoints(user?.id);

  const logs = progressPage?.content || [];
  const goals = goalsPage?.content || [];
  const tasks = tasksPage?.content || [];

  const totalMinutes = logs.reduce((acc: number, p: DailyProgress) => acc + p.studyMinutes, 0);
  const totalHours = (totalMinutes / 60).toFixed(1);
  const completedTasksCount = tasks.filter((t: Task) => t.status === 'COMPLETED').length;
  const taskCompletionRate = tasks.length > 0 ? Math.round((completedTasksCount / tasks.length) * 100) : 0;
  const avgGoalProgress =
    goals.length > 0
      ? Math.round(goals.reduce((acc: number, g: Goal) => acc + g.progress, 0) / goals.length)
      : 0;

  // 16-Week Heatmap matrix
  const weeksCount = 16;
  const daysInGrid = weeksCount * 7;
  const today = new Date();
  const heatmapData = Array.from({ length: daysInGrid }, (_, i) => {
    const d = new Date();
    d.setDate(today.getDate() - (daysInGrid - 1 - i));
    const dateStr = d.toISOString().split('T')[0];
    const log = logs.find((p: DailyProgress) => p.progressDate === dateStr);
    const mins = log?.studyMinutes || 0;
    return {
      date: dateStr,
      dayOfWeek: d.getDay(),
      minutes: mins,
    };
  });

  // Group by week
  const weeks: typeof heatmapData[] = [];
  for (let i = 0; i < heatmapData.length; i += 7) {
    weeks.push(heatmapData.slice(i, i + 7));
  }

  // Trailing 7 days focus
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(today.getDate() - (6 - i));
    const dateStr = d.toISOString().split('T')[0];
    const log = logs.find((p: DailyProgress) => p.progressDate === dateStr);
    return {
      day: d.toLocaleDateString('en-US', { weekday: 'short' }),
      date: dateStr,
      minutes: log?.studyMinutes || 0,
    };
  });

  return (
    <div className="space-y-4">
      {/* 1. Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
        <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
          <BarChart3 className="w-4 h-4 text-blue-600" />
          Productivity & Velocity Analytics
        </h1>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
          Comprehensive execution telemetry, focus heatmaps, and learning efficiency metrics
        </p>
      </div>

      {/* 2. Key Metrics Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <span className="text-[11px] font-bold uppercase text-slate-500 block">Total Focus Time</span>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">
            {totalHours} <span className="text-xs font-normal text-slate-400">hours</span>
          </div>
          <span className="text-[10px] text-slate-400">{logs.length} logged sessions</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <span className="text-[11px] font-bold uppercase text-slate-500 block">Task Velocity</span>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">{taskCompletionRate}%</div>
          <span className="text-[10px] text-slate-400">{completedTasksCount} of {tasks.length} tasks completed</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <span className="text-[11px] font-bold uppercase text-slate-500 block">Avg Goal Progress</span>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">{avgGoalProgress}%</div>
          <span className="text-[10px] text-slate-400">Across {goals.length} target milestones</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <span className="text-[11px] font-bold uppercase text-slate-500 block">Achievement Points</span>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">
            {pointsData?.totalPoints || 0} <span className="text-xs font-normal text-slate-400">pts</span>
          </div>
          <span className="text-[10px] text-slate-400">Verified platform badges</span>
        </div>
      </div>

      {/* 3. 16-Week Activity Heatmap */}
      <Card>
        <CardHeader>
          <CardTitle>16-Week Consistency & Focus Heatmap</CardTitle>
          <span className="text-[11px] text-slate-500">Trailing 112 Days</span>
        </CardHeader>
        <CardContent>
          {isProgressLoading ? (
            <Skeleton className="h-28 w-full" />
          ) : (
            <div>
              <div className="overflow-x-auto pb-2">
                <div className="flex gap-1 min-w-[650px]">
                  {weeks.map((week, wIdx) => (
                    <div key={wIdx} className="flex flex-col gap-1">
                      {week.map((day) => {
                        let bgClass = 'bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700';
                        if (day.minutes > 120) {
                          bgClass = 'bg-blue-700 border-blue-800';
                        } else if (day.minutes > 60) {
                          bgClass = 'bg-blue-600 border-blue-700';
                        } else if (day.minutes > 30) {
                          bgClass = 'bg-blue-400 border-blue-500';
                        } else if (day.minutes > 0) {
                          bgClass = 'bg-blue-200 border-blue-300';
                        }

                        return (
                          <div
                            key={day.date}
                            className={`w-3.5 h-3.5 rounded-[1px] ${bgClass} transition-colors`}
                            title={`${day.date}: ${day.minutes} minutes focus`}
                          />
                        );
                      })}
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-between text-[11px] text-slate-500 mt-2 pt-2 border-t border-slate-200 dark:border-slate-800">
                <span>16 Weeks Ago</span>
                <div className="flex items-center gap-1.5">
                  <span>Less</span>
                  <div className="w-2.5 h-2.5 bg-slate-100 dark:bg-slate-800 border border-slate-300 rounded-[1px]" />
                  <div className="w-2.5 h-2.5 bg-blue-200 rounded-[1px]" />
                  <div className="w-2.5 h-2.5 bg-blue-400 rounded-[1px]" />
                  <div className="w-2.5 h-2.5 bg-blue-600 rounded-[1px]" />
                  <div className="w-2.5 h-2.5 bg-blue-700 rounded-[1px]" />
                  <span>More</span>
                </div>
                <span>Today</span>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 4. Focus Chart & Goal Health */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Weekly Focus Bar Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Trailing 7-Day Focus Minutes</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-end justify-between gap-2 h-36 pt-4 pb-2">
              {last7Days.map((d) => {
                const heightPercent = Math.min(100, Math.max(8, Math.round((d.minutes / 180) * 100)));
                return (
                  <div key={d.date} className="flex-1 flex flex-col items-center gap-1.5 h-full justify-end">
                    <span className="text-[10px] font-bold text-slate-600 dark:text-slate-400">{d.minutes}m</span>
                    <div
                      className="w-full bg-blue-700 hover:bg-blue-600 rounded-[2px] transition-all"
                      style={{ height: `${heightPercent}%` }}
                    />
                    <span className="text-[10px] font-semibold text-slate-500">{d.day}</span>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* Goal Execution Breakdown */}
        <Card>
          <CardHeader>
            <CardTitle>Milestone Health Status</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {goals.slice(0, 4).map((g: Goal) => (
              <div key={g.id} className="p-2.5 bg-[#f8fafc] dark:bg-slate-800/60 border border-[#e2e8f0] dark:border-slate-700 rounded-[2px]">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-xs text-slate-900 dark:text-slate-100">{g.title}</span>
                  <Badge size="sm" variant="primary">
                    {g.progress}%
                  </Badge>
                </div>
                <div className="w-full bg-slate-200 dark:bg-slate-700 h-1.5 rounded-[2px] mt-1.5 overflow-hidden">
                  <div className="bg-blue-600 h-full rounded-[2px]" style={{ width: `${g.progress}%` }} />
                </div>
              </div>
            ))}

            {goals.length === 0 && (
              <p className="text-xs text-slate-500 italic text-center py-4">No active milestones recorded.</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
