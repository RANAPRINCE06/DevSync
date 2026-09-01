import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Clock,
  Target,
  CheckSquare,
  Flame,
  Plus,
  ArrowUpRight,
  Sparkles,
  Trophy,
  Medal,
  Calendar,
  CheckCircle2,
  Sliders,
  Edit2,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { useProgressList, useCreateProgress, useUpdateProgress } from '@/hooks/useProgress';
import { useGoals, useUpdateGoal } from '@/hooks/useGoals';
import { useTasks, useUpdateTask } from '@/hooks/useTasks';
import { useLeaderboard } from '@/hooks/useLeaderboard';
import { useUserAchievements, useUserTotalPoints } from '@/hooks/useAchievements';
import { formatMinutes, formatDate } from '@/lib/utils';
import { DailyProgress, ProgressStatus } from '@/types/progress';
import { Goal } from '@/types/goal';
import { Task } from '@/types/task';

export const DashboardPage: React.FC = () => {
  const { activeUser, activeTeam } = useApp();
  const { showToast } = useToast();

  // Progress Modal States
  const [isAddProgressOpen, setIsAddProgressOpen] = useState(false);
  const [editingProgress, setEditingProgress] = useState<DailyProgress | null>(null);
  const [whatStudied, setWhatStudied] = useState('');
  const [whatCompleted, setWhatCompleted] = useState('');
  const [blockers, setBlockers] = useState('');
  const [studyMinutes, setStudyMinutes] = useState(60);
  const [status, setStatus] = useState<ProgressStatus>('IN_PROGRESS');

  // Goal Progress Slider Modal State
  const [selectedGoalForProgress, setSelectedGoalForProgress] = useState<Goal | null>(null);
  const [goalProgressValue, setGoalProgressValue] = useState<number>(0);

  const todayStr = new Date().toISOString().split('T')[0];

  // Queries
  const { data: progressPage, isLoading: isProgressLoading } = useProgressList({
    userId: activeUser?.id,
    teamId: activeTeam?.id,
    date: todayStr,
  });
  const todayProgress = progressPage?.content?.[0];

  // Fetch 7 days progress for weekly focus chart
  const weekStart = new Date();
  weekStart.setDate(weekStart.getDate() - 6);
  const weekStartStr = weekStart.toISOString().split('T')[0];

  const { data: weekProgressData } = useProgressList({
    userId: activeUser?.id,
    teamId: activeTeam?.id,
    fromDate: weekStartStr,
    toDate: todayStr,
    size: 20,
  });

  const { data: goalsPage, isLoading: isGoalsLoading } = useGoals({
    teamId: activeTeam?.id,
    active: true,
    size: 4,
  });

  const { data: tasksPage, isLoading: isTasksLoading } = useTasks({
    teamId: activeTeam?.id,
    active: true,
    size: 5,
  });

  const { data: leaderboardPage, isLoading: isLeaderboardLoading } = useLeaderboard(
    activeTeam?.id,
    'ALL_TIME',
    { size: 3 }
  );

  const { data: userAchievements, isLoading: isAchievementsLoading } = useUserAchievements(
    activeUser?.id
  );
  const { data: totalPointsData } = useUserTotalPoints(activeUser?.id);

  // Mutations
  const createProgressMutation = useCreateProgress();
  const updateProgressMutation = useUpdateProgress();
  const updateGoalMutation = useUpdateGoal();
  const updateTaskMutation = useUpdateTask();

  const handleOpenAddProgress = () => {
    setEditingProgress(null);
    setWhatStudied('');
    setWhatCompleted('');
    setBlockers('');
    setStudyMinutes(60);
    setStatus('IN_PROGRESS');
    setIsAddProgressOpen(true);
  };

  const handleOpenEditProgress = (p: DailyProgress) => {
    setEditingProgress(p);
    setWhatStudied(p.whatStudied);
    setWhatCompleted(p.whatCompleted || '');
    setBlockers(p.blockers || '');
    setStudyMinutes(p.studyMinutes);
    setStatus(p.status);
    setIsAddProgressOpen(true);
  };

  const handleSaveProgress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeUser || !activeTeam) {
      showToast('error', 'Please ensure an active user and team are selected');
      return;
    }
    if (!whatStudied.trim()) {
      showToast('warning', 'What studied is required');
      return;
    }

    try {
      if (editingProgress) {
        await updateProgressMutation.mutateAsync({
          id: editingProgress.id,
          data: {
            whatStudied,
            whatCompleted: whatCompleted.trim() ? whatCompleted : undefined,
            blockers: blockers.trim() ? blockers : undefined,
            studyMinutes: Number(studyMinutes) || 0,
            status,
          },
        });
        showToast('success', 'Progress updated successfully!');
      } else {
        await createProgressMutation.mutateAsync({
          userId: activeUser.id,
          teamId: activeTeam.id,
          progressDate: todayStr,
          whatStudied,
          whatCompleted: whatCompleted.trim() ? whatCompleted : undefined,
          blockers: blockers.trim() ? blockers : undefined,
          studyMinutes: Number(studyMinutes) || 0,
          status,
        });
        showToast('success', 'Daily progress logged successfully!');
      }

      setIsAddProgressOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleToggleTaskComplete = async (task: Task) => {
    try {
      const isCompleted = task.status === 'COMPLETED';
      const newStatus = isCompleted ? 'TODO' : 'COMPLETED';
      await updateTaskMutation.mutateAsync({
        id: task.id,
        data: {
          title: task.title,
          description: task.description || undefined,
          status: newStatus,
          priority: task.priority,
          estimatedMinutes: task.estimatedMinutes || undefined,
          actualMinutes: task.actualMinutes || undefined,
          dueDate: task.dueDate || undefined,
          active: true,
        },
      });
      showToast('success', isCompleted ? 'Task marked as Todo' : 'Task marked as Completed (+20 score)');
    } catch (err: unknown) {
      showToast('error', 'Failed to update task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleUpdateGoalProgress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedGoalForProgress) return;

    try {
      const isCompleted = goalProgressValue >= 100;
      await updateGoalMutation.mutateAsync({
        id: selectedGoalForProgress.id,
        data: {
          title: selectedGoalForProgress.title,
          description: selectedGoalForProgress.description || undefined,
          priority: selectedGoalForProgress.priority,
          startDate: selectedGoalForProgress.startDate,
          targetDate: selectedGoalForProgress.targetDate,
          progress: goalProgressValue,
          status: isCompleted ? 'COMPLETED' : 'IN_PROGRESS',
        },
      });
      showToast(
        'success',
        isCompleted ? 'Goal completed at 100%! (+50 score)' : `Goal progress updated to ${goalProgressValue}%`
      );
      setSelectedGoalForProgress(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to update goal progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  const currentDateFormatted = new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date());

  // Task Stats Calculation
  const totalTasks = tasksPage?.content?.length || 0;
  const completedTasksCount = tasksPage?.content?.filter((t) => t.status === 'COMPLETED').length || 0;
  const inProgressTasksCount = tasksPage?.content?.filter((t) => t.status === 'IN_PROGRESS').length || 0;
  const pendingTasksCount = tasksPage?.content?.filter((t) => t.status === 'TODO').length || 0;
  const taskCompletionRate = totalTasks > 0 ? Math.round((completedTasksCount / totalTasks) * 100) : 0;

  // 7-day focus chart data builder
  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    const dateStr = d.toISOString().split('T')[0];
    const dayLabel = daysOfWeek[d.getDay()];
    const entry = weekProgressData?.content?.find((p) => p.progressDate === dateStr);
    const minutes = entry ? entry.studyMinutes : 0;
    const isToday = dateStr === todayStr;
    return { dateStr, dayLabel, minutes, isToday };
  });
  const maxFocusMinutes = Math.max(...last7Days.map((d) => d.minutes), 120);

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* 1. Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 tracking-tight flex items-center gap-2">
            {getGreeting()}, {activeUser?.name || 'Developer'}{' '}
            <span className="inline-block animate-bounce">👋</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1 flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5 text-primary-400" />
            {currentDateFormatted}
            {activeTeam && (
              <>
                <span className="text-slate-600">•</span>
                <span className="text-slate-300 font-medium">{activeTeam.name}</span>
              </>
            )}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="primary"
            size="sm"
            leftIcon={<Plus className="w-4 h-4" />}
            onClick={handleOpenAddProgress}
          >
            Log Progress
          </Button>
        </div>
      </div>

      {/* 2. Key Metrics Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Study Minutes */}
        <Card hoverable className="relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Today's Focus</span>
            <div className="w-8 h-8 rounded-xl bg-primary-950/70 border border-primary-800/60 flex items-center justify-center text-primary-400">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-slate-100">
              {todayProgress ? formatMinutes(todayProgress.studyMinutes) : '0m'}
            </div>
            <p className="text-[11px] text-slate-500 mt-0.5">
              {todayProgress ? 'Logged for today' : 'No study logged yet'}
            </p>
          </div>
        </Card>

        {/* Active Goals */}
        <Card hoverable className="relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Active Goals</span>
            <div className="w-8 h-8 rounded-xl bg-indigo-950/70 border border-indigo-800/60 flex items-center justify-center text-indigo-400">
              <Target className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-slate-100">
              {isGoalsLoading ? <Skeleton className="h-8 w-12" /> : goalsPage?.totalElements || 0}
            </div>
            <p className="text-[11px] text-slate-500 mt-0.5">In progress & target goals</p>
          </div>
        </Card>

        {/* Pending Tasks */}
        <Card hoverable className="relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Pending Tasks</span>
            <div className="w-8 h-8 rounded-xl bg-emerald-950/70 border border-emerald-800/60 flex items-center justify-center text-emerald-400">
              <CheckSquare className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-slate-100">
              {isTasksLoading ? <Skeleton className="h-8 w-12" /> : pendingTasksCount + inProgressTasksCount}
            </div>
            <p className="text-[11px] text-slate-500 mt-0.5">Tasks due soon</p>
          </div>
        </Card>

        {/* Achievement Points */}
        <Card hoverable className="relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Achievement Points</span>
            <div className="w-8 h-8 rounded-xl bg-amber-950/70 border border-amber-800/60 flex items-center justify-center text-amber-400">
              <Flame className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-slate-100">
              {totalPointsData?.totalPoints || 0} <span className="text-xs font-normal text-amber-400">pts</span>
            </div>
            <p className="text-[11px] text-slate-500 mt-0.5">Earned from milestones</p>
          </div>
        </Card>
      </div>

      {/* 3. Productivity Analytics Section (7-Day Focus & Task Completion) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: 7-Day Focus Bar Chart (2 columns) */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <Clock className="w-4 h-4 text-primary-400" />
                <CardTitle>Weekly Focus Analytics</CardTitle>
              </div>
              <span className="text-xs text-slate-400 font-mono">Last 7 days</span>
            </div>
          </CardHeader>
          <CardContent>
            <div className="h-44 flex items-end justify-between gap-3 pt-4 px-2">
              {last7Days.map((d) => {
                const heightPercent = Math.max(Math.round((d.minutes / maxFocusMinutes) * 100), 8);
                return (
                  <div key={d.dateStr} className="flex-1 flex flex-col items-center gap-2 group">
                    <span className="text-[10px] text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity">
                      {d.minutes}m
                    </span>
                    <div className="w-full max-w-[36px] bg-slate-800 rounded-t-lg h-28 flex items-end p-1">
                      <div
                        className={`w-full rounded-md transition-all duration-500 ${
                          d.isToday
                            ? 'bg-gradient-to-t from-primary-600 to-indigo-400 shadow-glow'
                            : 'bg-slate-700 group-hover:bg-primary-500/70'
                        }`}
                        style={{ height: `${heightPercent}%` }}
                      />
                    </div>
                    <span
                      className={`text-xs font-medium ${
                        d.isToday ? 'text-primary-400 font-bold' : 'text-slate-400'
                      }`}
                    >
                      {d.dayLabel}
                    </span>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* Right: Task Completion Overview (1 column) */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <CheckSquare className="w-4 h-4 text-emerald-400" />
              <CardTitle>Task Completion</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4 pt-1">
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400">Total Completion</span>
                <span className="text-lg font-bold text-emerald-400">{taskCompletionRate}%</span>
              </div>
              <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 rounded-full transition-all duration-500"
                  style={{ width: `${taskCompletionRate}%` }}
                />
              </div>

              <div className="space-y-2 pt-2 border-t border-slate-800 text-xs">
                <div className="flex items-center justify-between text-slate-300">
                  <span className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-emerald-400" /> Completed
                  </span>
                  <span className="font-semibold text-slate-100">{completedTasksCount}</span>
                </div>
                <div className="flex items-center justify-between text-slate-300">
                  <span className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-primary-400" /> In Progress
                  </span>
                  <span className="font-semibold text-slate-100">{inProgressTasksCount}</span>
                </div>
                <div className="flex items-center justify-between text-slate-300">
                  <span className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-slate-500" /> To Do
                  </span>
                  <span className="font-semibold text-slate-100">{pendingTasksCount}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 4. Today's Accountability Widget */}
      <Card className="bg-gradient-to-br from-slate-900/90 to-slate-900/40 border-slate-800">
        <CardHeader>
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-primary-400" />
            <CardTitle>Today's Accountability & Progress</CardTitle>
          </div>
          {todayProgress && (
            <div className="flex items-center gap-2">
              <Badge
                variant={
                  todayProgress.status === 'COMPLETED'
                    ? 'success'
                    : todayProgress.status === 'IN_PROGRESS'
                    ? 'primary'
                    : 'warning'
                }
              >
                {todayProgress.status}
              </Badge>
              <Button
                variant="ghost"
                size="sm"
                leftIcon={<Edit2 className="w-3.5 h-3.5" />}
                onClick={() => handleOpenEditProgress(todayProgress)}
              >
                Edit
              </Button>
            </div>
          )}
        </CardHeader>
        <CardContent>
          {isProgressLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-4 w-3/4" />
              <Skeleton className="h-4 w-1/2" />
            </div>
          ) : todayProgress ? (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-1">
              <div className="p-3.5 rounded-xl bg-slate-950/50 border border-slate-800/80">
                <span className="text-[11px] font-medium text-slate-400 block mb-1">What Studied / Built</span>
                <p className="text-sm text-slate-200">{todayProgress.whatStudied}</p>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-950/50 border border-slate-800/80">
                <span className="text-[11px] font-medium text-slate-400 block mb-1">Completed Items</span>
                <p className="text-sm text-slate-200">{todayProgress.whatCompleted || 'In progress'}</p>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-950/50 border border-slate-800/80 flex flex-col justify-between">
                <div>
                  <span className="text-[11px] font-medium text-slate-400 block mb-1">Focus Time</span>
                  <p className="text-sm font-semibold text-primary-300">
                    {formatMinutes(todayProgress.studyMinutes)}
                  </p>
                </div>
                {todayProgress.blockers && (
                  <p className="text-xs text-amber-400/90 mt-2 truncate">
                    ⚠️ {todayProgress.blockers}
                  </p>
                )}
              </div>
            </div>
          ) : (
            <EmptyState
              icon={Clock}
              title="Nothing logged yet today"
              description="Keep your team in sync and maintain your daily streak by recording what you worked on."
              action={
                <Button
                  variant="primary"
                  size="sm"
                  leftIcon={<Plus className="w-4 h-4" />}
                  onClick={handleOpenAddProgress}
                >
                  Add Today's Progress
                </Button>
              }
            />
          )}
        </CardContent>
      </Card>

      {/* 5. Active Goals & Upcoming Tasks (2 Columns) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Active Goals */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <Target className="w-4 h-4 text-indigo-400" />
                <CardTitle>Active Goals</CardTitle>
              </div>
              <Link
                to="/goals"
                className="text-xs text-primary-400 hover:text-primary-300 flex items-center gap-0.5 font-medium transition-colors"
              >
                View all <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </CardHeader>
          <CardContent>
            {isGoalsLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-14 w-full" />
                <Skeleton className="h-14 w-full" />
              </div>
            ) : goalsPage?.content && goalsPage.content.length > 0 ? (
              <div className="space-y-3">
                {goalsPage.content.map((goal) => (
                  <div
                    key={goal.id}
                    className="p-3.5 rounded-xl bg-slate-950/40 border border-slate-800/80 hover:border-slate-700/80 transition-all space-y-2 group"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-slate-200">{goal.title}</span>
                      <div className="flex items-center gap-2">
                        <Badge
                          size="sm"
                          variant={
                            goal.priority === 'CRITICAL' || goal.priority === 'HIGH'
                              ? 'danger'
                              : goal.priority === 'MEDIUM'
                              ? 'warning'
                              : 'default'
                          }
                        >
                          {goal.priority}
                        </Badge>
                        <button
                          onClick={() => {
                            setSelectedGoalForProgress(goal);
                            setGoalProgressValue(goal.progress);
                          }}
                          className="text-slate-400 hover:text-primary-300 p-1 transition-colors"
                          title="Quick update progress"
                        >
                          <Sliders className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>

                    <div className="space-y-1">
                      <div className="flex justify-between text-[11px] text-slate-400">
                        <span>Progress: {goal.progress}%</span>
                        <span>Due: {formatDate(goal.targetDate)}</span>
                      </div>
                      <div className="w-full h-1.5 rounded-full bg-slate-800 overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-primary-600 to-indigo-500 rounded-full transition-all duration-300"
                          style={{ width: `${Math.min(goal.progress, 100)}%` }}
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState
                icon={Target}
                title="No active goals"
                description="Set your first team or personal learning goal to start tracking progress."
                action={
                  <Link to="/goals">
                    <Button variant="outline" size="sm">
                      Create Goal
                    </Button>
                  </Link>
                }
              />
            )}
          </CardContent>
        </Card>

        {/* Upcoming Tasks */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <CheckSquare className="w-4 h-4 text-emerald-400" />
                <CardTitle>Upcoming Tasks</CardTitle>
              </div>
              <Link
                to="/tasks"
                className="text-xs text-primary-400 hover:text-primary-300 flex items-center gap-0.5 font-medium transition-colors"
              >
                View all <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </CardHeader>
          <CardContent>
            {isTasksLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
              </div>
            ) : tasksPage?.content && tasksPage.content.length > 0 ? (
              <div className="space-y-2.5">
                {tasksPage.content.map((task) => {
                  const isCompleted = task.status === 'COMPLETED';
                  return (
                    <div
                      key={task.id}
                      className="p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-center justify-between gap-3 hover:border-slate-700/80 transition-all"
                    >
                      <div className="min-w-0 flex items-center gap-2.5">
                        <button
                          onClick={() => handleToggleTaskComplete(task)}
                          className={`w-5 h-5 rounded-lg border flex items-center justify-center transition-colors shrink-0 ${
                            isCompleted
                              ? 'bg-emerald-600 border-emerald-500 text-white'
                              : 'border-slate-700 hover:border-emerald-500 text-transparent'
                          }`}
                          title={isCompleted ? 'Mark as Todo' : 'Mark as Completed'}
                        >
                          <CheckCircle2 className="w-3.5 h-3.5" />
                        </button>
                        <div className="min-w-0">
                          <p
                            className={`text-xs font-medium truncate ${
                              isCompleted ? 'line-through text-slate-500' : 'text-slate-200'
                            }`}
                          >
                            {task.title}
                          </p>
                          <p className="text-[10px] text-slate-500 truncate">
                            {task.goalTitle} • Assignee: {task.assigneeName}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 shrink-0">
                        <Badge
                          size="sm"
                          variant={
                            task.status === 'COMPLETED'
                              ? 'success'
                              : task.status === 'IN_PROGRESS'
                              ? 'primary'
                              : 'default'
                          }
                        >
                          {task.status}
                        </Badge>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <EmptyState
                icon={CheckSquare}
                title="No upcoming tasks"
                description="Break down goals into actionable tasks for you and your team."
                action={
                  <Link to="/tasks">
                    <Button variant="outline" size="sm">
                      Create Task
                    </Button>
                  </Link>
                }
              />
            )}
          </CardContent>
        </Card>
      </div>

      {/* 6. Team Leaderboard Podium & Recent Achievements */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Team Leaderboard Podium (2 columns wide) */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <Trophy className="w-4 h-4 text-amber-400" />
                <CardTitle>Team Leaderboard Podium</CardTitle>
              </div>
              <Link
                to="/leaderboard"
                className="text-xs text-primary-400 hover:text-primary-300 flex items-center gap-0.5 font-medium transition-colors"
              >
                Full Leaderboard <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </CardHeader>
          <CardContent>
            {isLeaderboardLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
              </div>
            ) : leaderboardPage?.content && leaderboardPage.content.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
                {leaderboardPage.content.map((entry) => {
                  const medalEmoji =
                    entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : entry.rank === 3 ? '🥉' : `#${entry.rank}`;
                  const isFirst = entry.rank === 1;
                  return (
                    <div
                      key={entry.userId}
                      className={`p-4 rounded-xl border flex flex-col items-center text-center transition-all ${
                        isFirst
                          ? 'bg-gradient-to-b from-amber-950/30 to-slate-900/80 border-amber-500/40 shadow-glow'
                          : 'bg-slate-950/40 border-slate-800'
                      }`}
                    >
                      <span className="text-2xl mb-1">{medalEmoji}</span>
                      <p className="text-xs font-bold text-slate-100 truncate max-w-full">{entry.userName}</p>
                      <p className="text-base font-extrabold text-primary-300 mt-1">{entry.score} pts</p>
                      <p className="text-[10px] text-slate-500 mt-0.5">
                        {entry.completedTasks} tasks • {entry.progressEntries} entries
                      </p>
                    </div>
                  );
                })}
              </div>
            ) : (
              <EmptyState
                icon={Trophy}
                title="No leaderboard data"
                description="Start completing tasks, goals, and daily progress to compete on the leaderboard."
              />
            )}
          </CardContent>
        </Card>

        {/* Recent Achievements (1 column wide) */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <Medal className="w-4 h-4 text-primary-400" />
                <CardTitle>Achievements</CardTitle>
              </div>
              <Link
                to="/achievements"
                className="text-xs text-primary-400 hover:text-primary-300 flex items-center gap-0.5 font-medium transition-colors"
              >
                View all <ArrowUpRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          </CardHeader>
          <CardContent>
            {isAchievementsLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
              </div>
            ) : userAchievements && userAchievements.length > 0 ? (
              <div className="space-y-2.5">
                {userAchievements.slice(0, 3).map((ach) => (
                  <div
                    key={ach.id}
                    className="p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-center gap-3"
                  >
                    <div className="w-8 h-8 rounded-xl bg-primary-950/80 border border-primary-800/60 flex items-center justify-center text-primary-300 font-bold text-xs shrink-0">
                      🏆
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-xs font-semibold text-slate-200 truncate">{ach.title}</p>
                      <p className="text-[10px] text-primary-400 font-medium">+{ach.points} pts</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState
                icon={Medal}
                title="No badges earned yet"
                description="Unlock milestones through consistency and problem solving."
              />
            )}
          </CardContent>
        </Card>
      </div>

      {/* Quick Add/Edit Progress Modal */}
      <Modal
        isOpen={isAddProgressOpen}
        onClose={() => setIsAddProgressOpen(false)}
        title={editingProgress ? "Edit Progress Entry" : "Log Today's Progress"}
        description="Share what you studied and built to stay accountable with your team."
      >
        <form onSubmit={handleSaveProgress} className="space-y-4">
          <Input
            label="What did you study / work on today? *"
            placeholder="e.g. Completed LeetCode Tree problems, revised Flyway migrations"
            value={whatStudied}
            onChange={(e) => setWhatStudied(e.target.value)}
            required
          />

          <Input
            label="Completed Work (Optional)"
            placeholder="e.g. Solved 3 medium DSA questions"
            value={whatCompleted}
            onChange={(e) => setWhatCompleted(e.target.value)}
          />

          <Input
            label="Any Blockers? (Optional)"
            placeholder="e.g. Need clarification on API pagination specification"
            value={blockers}
            onChange={(e) => setBlockers(e.target.value)}
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              type="number"
              label="Study Minutes"
              min={0}
              max={1440}
              value={studyMinutes}
              onChange={(e) => setStudyMinutes(Number(e.target.value))}
            />

            <Select
              label="Status"
              value={status}
              onChange={(e) => setStatus(e.target.value as ProgressStatus)}
              options={[
                { label: 'In Progress', value: 'IN_PROGRESS' },
                { label: 'Completed', value: 'COMPLETED' },
                { label: 'Partial', value: 'PARTIAL' },
              ]}
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => setIsAddProgressOpen(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={createProgressMutation.isPending || updateProgressMutation.isPending}
            >
              {editingProgress ? "Update Entry" : "Submit Progress"}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Quick Goal Progress Slider Modal */}
      {selectedGoalForProgress && (
        <Modal
          isOpen={!!selectedGoalForProgress}
          onClose={() => setSelectedGoalForProgress(null)}
          title={`Update Progress: ${selectedGoalForProgress.title}`}
          description="Adjust current progress percentage. Reaching 100% automatically marks the goal as Completed."
        >
          <form onSubmit={handleUpdateGoalProgress} className="space-y-5">
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs text-slate-300">
                <span>Progress Percentage</span>
                <span className="text-base font-bold text-primary-400">{goalProgressValue}%</span>
              </div>
              <input
                type="range"
                min={0}
                max={100}
                value={goalProgressValue}
                onChange={(e) => setGoalProgressValue(Number(e.target.value))}
                className="w-full accent-primary-500 cursor-pointer h-2 bg-slate-800 rounded-lg"
              />
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setSelectedGoalForProgress(null)}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="primary"
                size="sm"
                isLoading={updateGoalMutation.isPending}
              >
                Save Progress
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
