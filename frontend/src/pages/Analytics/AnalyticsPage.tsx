import React, { useMemo } from 'react';
import {
  BarChart3,
  Clock,
  CheckSquare,
  Target,
  Medal,
  Zap,
  Activity,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { useProgressList } from '@/hooks/useProgress';
import { useGoals } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { useUserAchievements, useUserTotalPoints } from '@/hooks/useAchievements';
import { formatMinutes, formatDate } from '@/lib/utils';

export const AnalyticsPage: React.FC = () => {
  const { activeUser, activeTeam } = useApp();

  // Queries (fetch up to 100 entries for deep analytics)
  const { data: progressPage, isLoading: isProgressLoading } = useProgressList({
    userId: activeUser?.id,
    teamId: activeTeam?.id,
    size: 100,
  });

  const { data: goalsPage, isLoading: isGoalsLoading } = useGoals({
    teamId: activeTeam?.id,
    active: true,
    size: 100,
  });

  const { data: tasksPage, isLoading: isTasksLoading } = useTasks({
    teamId: activeTeam?.id,
    active: true,
    size: 100,
  });

  const { data: userAchievements, isLoading: isAchievementsLoading } = useUserAchievements(
    activeUser?.id
  );
  const { data: totalPointsData } = useUserTotalPoints(activeUser?.id);

  const allProgress = progressPage?.content || [];
  const allGoals = goalsPage?.content || [];
  const allTasks = tasksPage?.content || [];
  const achievements = userAchievements || [];

  // Focus Metrics
  const totalFocusMinutes = allProgress.reduce((acc, curr) => acc + curr.studyMinutes, 0);
  const averageFocusMinutes = allProgress.length > 0 ? Math.round(totalFocusMinutes / allProgress.length) : 0;
  const highestFocusSession = allProgress.length > 0 ? Math.max(...allProgress.map((p) => p.studyMinutes)) : 0;

  // Task Breakdown
  const completedTasks = allTasks.filter((t) => t.status === 'COMPLETED').length;
  const inProgressTasks = allTasks.filter((t) => t.status === 'IN_PROGRESS').length;
  const todoTasks = allTasks.filter((t) => t.status === 'TODO').length;
  const overdueTasks = allTasks.filter((t) => {
    if (!t.dueDate || t.status === 'COMPLETED') return false;
    return new Date(t.dueDate).getTime() < new Date().setHours(0, 0, 0, 0);
  }).length;
  const taskCompletionRate = allTasks.length > 0 ? Math.round((completedTasks / allTasks.length) * 100) : 0;

  // Goal Breakdown
  const completedGoals = allGoals.filter((g) => g.status === 'COMPLETED' || g.progress >= 100).length;
  const activeGoals = allGoals.filter((g) => g.status !== 'COMPLETED' && g.progress < 100);
  const averageGoalProgress =
    allGoals.length > 0
      ? Math.round(allGoals.reduce((acc, curr) => acc + curr.progress, 0) / allGoals.length)
      : 0;

  // 7-day focus chart
  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    const dateStr = d.toISOString().split('T')[0];
    const dayLabel = daysOfWeek[d.getDay()];
    const entry = allProgress.find((p) => p.progressDate === dateStr);
    const minutes = entry ? entry.studyMinutes : 0;
    return { dateStr, dayLabel, minutes };
  });
  const maxFocus = Math.max(...last7Days.map((d) => d.minutes), 120);

  // GitHub-style contribution heatmap (last 16 weeks / ~112 days)
  const heatmapDays = useMemo(() => {
    const totalDays = 112; // 16 weeks
    const days = [];
    const progressMap = new Map<string, number>();
    allProgress.forEach((p) => {
      progressMap.set(p.progressDate, (progressMap.get(p.progressDate) || 0) + p.studyMinutes);
    });

    for (let i = totalDays - 1; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const dateStr = d.toISOString().split('T')[0];
      const minutes = progressMap.get(dateStr) || 0;
      days.push({ dateStr, minutes, dayOfWeek: d.getDay() });
    }
    return days;
  }, [allProgress]);

  const getHeatmapColor = (minutes: number) => {
    if (minutes === 0) return 'bg-slate-850 hover:bg-slate-700';
    if (minutes < 60) return 'bg-primary-900/60 hover:bg-primary-800 text-primary-300';
    if (minutes < 120) return 'bg-primary-700 hover:bg-primary-600';
    if (minutes < 240) return 'bg-primary-500 hover:bg-primary-400';
    return 'bg-indigo-400 hover:bg-indigo-300 shadow-glow';
  };

  const isLoading = isProgressLoading || isGoalsLoading || isTasksLoading || isAchievementsLoading;

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Page Header */}
      <div className="pb-2 border-b border-slate-850">
        <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
          <BarChart3 className="w-5 h-5 text-primary-400" />
          Productivity & Growth Analytics
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Deep performance metrics, focus distributions, and milestone completion velocity.
        </p>
      </div>

      {/* 2. Top Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card hoverable>
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Total Focus Time</span>
            <Clock className="w-4 h-4 text-primary-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">
            {isLoading ? <Skeleton className="h-8 w-16" /> : formatMinutes(totalFocusMinutes)}
          </div>
          <p className="text-[11px] text-slate-500 mt-0.5">Across {allProgress.length} recorded sessions</p>
        </Card>

        <Card hoverable>
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Average Session</span>
            <Zap className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">
            {isLoading ? <Skeleton className="h-8 w-16" /> : formatMinutes(averageFocusMinutes)}
          </div>
          <p className="text-[11px] text-slate-500 mt-0.5">Peak session: {formatMinutes(highestFocusSession)}</p>
        </Card>

        <Card hoverable>
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Task Velocity</span>
            <CheckSquare className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-emerald-400 mt-2">
            {isLoading ? <Skeleton className="h-8 w-16" /> : `${taskCompletionRate}%`}
          </div>
          <p className="text-[11px] text-slate-500 mt-0.5">{completedTasks} of {allTasks.length} completed</p>
        </Card>

        <Card hoverable>
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Achievement Points</span>
            <Medal className="w-4 h-4 text-primary-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">
            {isLoading ? <Skeleton className="h-8 w-16" /> : totalPointsData?.totalPoints || 0}
          </div>
          <p className="text-[11px] text-slate-500 mt-0.5">{achievements.length} badges unlocked</p>
        </Card>
      </div>

      {/* 3. Developer Activity Heatmap */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between w-full">
            <div className="flex items-center gap-2">
              <Activity className="w-4 h-4 text-emerald-400" />
              <CardTitle>Developer Activity Heatmap</CardTitle>
            </div>
            <span className="text-xs text-slate-400 font-mono">Last 16 Weeks</span>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3 pt-2">
            <div className="overflow-x-auto pb-2">
              <div className="inline-grid grid-rows-7 grid-flow-col gap-1.5 min-w-[650px]">
                {heatmapDays.map((day) => (
                  <div
                    key={day.dateStr}
                    className={`w-3.5 h-3.5 rounded-sm transition-all ${getHeatmapColor(day.minutes)}`}
                    title={`${formatDate(day.dateStr)}: ${day.minutes}m focus logged`}
                  />
                ))}
              </div>
            </div>

            <div className="flex items-center justify-between text-[11px] text-slate-500 pt-2 border-t border-slate-850">
              <span>Contributions derived from recorded daily progress</span>
              <div className="flex items-center gap-1.5">
                <span>Less</span>
                <div className="w-2.5 h-2.5 rounded-sm bg-slate-850" />
                <div className="w-2.5 h-2.5 rounded-sm bg-primary-900/60" />
                <div className="w-2.5 h-2.5 rounded-sm bg-primary-700" />
                <div className="w-2.5 h-2.5 rounded-sm bg-primary-500" />
                <div className="w-2.5 h-2.5 rounded-sm bg-indigo-400" />
                <span>More</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 4. Focus Chart & Task Velocity Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Weekly Focus Bar Chart */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-primary-400" />
              <CardTitle>Weekly Focus Distribution</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="h-44 flex items-end justify-between gap-3 pt-4 px-2">
              {last7Days.map((d) => {
                const heightPercent = Math.max(Math.round((d.minutes / maxFocus) * 100), 8);
                return (
                  <div key={d.dateStr} className="flex-1 flex flex-col items-center gap-2 group">
                    <span className="text-[10px] text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity">
                      {d.minutes}m
                    </span>
                    <div className="w-full max-w-[36px] bg-slate-800 rounded-t-lg h-28 flex items-end p-1">
                      <div
                        className="w-full rounded-md bg-gradient-to-t from-primary-600 to-indigo-400 transition-all duration-500"
                        style={{ height: `${heightPercent}%` }}
                      />
                    </div>
                    <span className="text-xs font-medium text-slate-400">{d.dayLabel}</span>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* Task Velocity Distribution */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <CheckSquare className="w-4 h-4 text-emerald-400" />
              <CardTitle>Task Execution Breakdown</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4 pt-1">
              <div className="space-y-2 text-xs">
                <div className="flex items-center justify-between text-slate-300">
                  <span className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-emerald-400" /> Completed
                  </span>
                  <span className="font-semibold text-slate-100">{completedTasks}</span>
                </div>
                <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-emerald-400 rounded-full"
                    style={{ width: `${allTasks.length > 0 ? (completedTasks / allTasks.length) * 100 : 0}%` }}
                  />
                </div>

                <div className="flex items-center justify-between text-slate-300 pt-1">
                  <span className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-primary-400" /> In Progress
                  </span>
                  <span className="font-semibold text-slate-100">{inProgressTasks}</span>
                </div>
                <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-primary-400 rounded-full"
                    style={{ width: `${allTasks.length > 0 ? (inProgressTasks / allTasks.length) * 100 : 0}%` }}
                  />
                </div>

                <div className="flex items-center justify-between text-slate-300 pt-1">
                  <span className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-slate-500" /> To Do
                  </span>
                  <span className="font-semibold text-slate-100">{todoTasks}</span>
                </div>
                <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-slate-500 rounded-full"
                    style={{ width: `${allTasks.length > 0 ? (todoTasks / allTasks.length) * 100 : 0}%` }}
                  />
                </div>

                {overdueTasks > 0 && (
                  <div className="flex items-center justify-between text-rose-400 pt-1 font-semibold">
                    <span className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full bg-rose-500" /> Overdue Tasks
                    </span>
                    <span>{overdueTasks}</span>
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 5. Goals Health & Achievements Categories */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Goal Health Status */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Target className="w-4 h-4 text-indigo-400" />
              <CardTitle>Goal Progress & Completion</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4 text-xs">
              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/40 border border-slate-800">
                <div>
                  <span className="font-bold text-slate-100 block">Average Goal Progress</span>
                  <span className="text-slate-500 text-[11px]">Across {allGoals.length} total goals</span>
                </div>
                <span className="text-lg font-bold text-primary-400">{averageGoalProgress}%</span>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="p-3 rounded-xl bg-slate-950/40 border border-slate-800">
                  <span className="text-slate-500 text-[10px] uppercase font-semibold">Active Goals</span>
                  <span className="text-base font-bold text-slate-100 mt-1 block">{activeGoals.length}</span>
                </div>
                <div className="p-3 rounded-xl bg-slate-950/40 border border-slate-800">
                  <span className="text-slate-500 text-[10px] uppercase font-semibold">Completed Goals</span>
                  <span className="text-base font-bold text-emerald-400 mt-1 block">{completedGoals}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Achievements Category Breakdown */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Medal className="w-4 h-4 text-amber-400" />
              <CardTitle>Milestones & Badges</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {achievements.length > 0 ? (
                achievements.map((ach) => (
                  <div
                    key={ach.id}
                    className="p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="flex items-center gap-2.5 min-w-0">
                      <div className="w-7 h-7 rounded-lg bg-primary-950 border border-primary-800 flex items-center justify-center text-primary-300 font-bold shrink-0">
                        🏆
                      </div>
                      <div className="min-w-0">
                        <p className="font-semibold text-slate-100 truncate">{ach.title}</p>
                        <p className="text-[10px] text-slate-500 truncate">{ach.type}</p>
                      </div>
                    </div>
                    <Badge size="sm" variant="warning">
                      +{ach.points} pts
                    </Badge>
                  </div>
                ))
              ) : (
                <p className="text-xs text-slate-500 text-center py-6">
                  No badges unlocked yet. Keep logging progress and solving tasks to earn points!
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
