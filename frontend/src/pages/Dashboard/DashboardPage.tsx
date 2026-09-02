import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
  Clock,
  Target,
  CheckSquare,
  TrendingUp,
  Plus,
  ChevronRight,
} from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useApp } from '@/contexts/AppContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { useProgressList, useCreateProgress } from '@/hooks/useProgress';
import { useGoals, useUpdateGoal } from '@/hooks/useGoals';
import { useTasks, useUpdateTask } from '@/hooks/useTasks';
import { useLeaderboard } from '@/hooks/useLeaderboard';
import { useUserTotalPoints } from '@/hooks/useAchievements';
import { formatDate } from '@/lib/utils';
import { filterRealUsers } from '@/lib/userFilter';
import { Goal } from '@/types/goal';
import { Task } from '@/types/task';
import { DailyProgress } from '@/types/progress';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const { activeTeam } = useApp();
  const { showToast } = useToast();

  const [isLogProgressModalOpen, setIsLogProgressModalOpen] = useState(false);
  const [isUpdateGoalModalOpen, setIsUpdateGoalModalOpen] = useState(false);
  const [selectedGoal, setSelectedGoal] = useState<Goal | null>(null);
  const [goalProgressValue, setGoalProgressValue] = useState<number>(0);

  // Quick Log State
  const [logTopics, setLogTopics] = useState('');
  const [logCompleted, setLogCompleted] = useState('');
  const [logMinutes, setLogMinutes] = useState('60');

  // Queries
  const { data: progressData } = useProgressList({ userId: user?.id, size: 50 });
  const { data: goalsData } = useGoals({ teamId: activeTeam?.id, active: true, size: 50 });
  const { data: tasksData } = useTasks({ teamId: activeTeam?.id, active: true, size: 50 });
  const { data: leaderboardData } = useLeaderboard(activeTeam?.id, 'ALL_TIME');
  const { data: userPointsData } = useUserTotalPoints(user?.id);

  const createProgressMutation = useCreateProgress();
  const updateGoalMutation = useUpdateGoal();
  const updateTaskMutation = useUpdateTask();

  // Metrics Calculations
  const progressLogs = progressData?.content || [];
  const activeGoals = goalsData?.content || [];
  const allTasks = tasksData?.content || [];

  // Today's focus minutes
  const todayStr = new Date().toISOString().split('T')[0];
  const todayLog = progressLogs.find((p: DailyProgress) => p.progressDate === todayStr);
  const todayFocusMinutes = todayLog?.studyMinutes || 0;

  // Task metrics
  const pendingTasks = allTasks.filter((t) => t.status !== 'COMPLETED');
  const completedTasks = allTasks.filter((t) => t.status === 'COMPLETED');
  const taskCompletionRate = allTasks.length > 0 ? Math.round((completedTasks.length / allTasks.length) * 100) : 0;

  // Filter leaderboard to real users only
  const rawLeaderboard = leaderboardData?.content || [];
  const realLeaderboard = filterRealUsers(rawLeaderboard);

  // Total Achievement Points
  const achievementPoints = userPointsData?.totalPoints || 0;

  // Productivity Score Calculation (0 - 100)
  const focusScore = Math.min(40, Math.round((todayFocusMinutes / 120) * 40));
  const taskScore = Math.min(30, Math.round((taskCompletionRate / 100) * 30));
  const goalScore = Math.min(
    20,
    activeGoals.length > 0
      ? Math.round((activeGoals.reduce((acc, g) => acc + g.progress, 0) / (activeGoals.length * 100)) * 20)
      : 15
  );
  const badgeScore = Math.min(10, Math.round((achievementPoints / 50) * 10));
  const totalVelocityScore = Math.min(100, focusScore + taskScore + goalScore + badgeScore);

  // Trailing 7-day focus data
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    const dateStr = d.toISOString().split('T')[0];
    const log = progressLogs.find((p: DailyProgress) => p.progressDate === dateStr);
    return {
      day: d.toLocaleDateString('en-US', { weekday: 'short' }),
      date: dateStr,
      minutes: log?.studyMinutes || 0,
    };
  });

  const handleQuickLog = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !activeTeam) return;
    try {
      await createProgressMutation.mutateAsync({
        userId: user.id,
        teamId: activeTeam.id,
        progressDate: todayStr,
        whatStudied: logTopics.trim(),
        completed: logCompleted.trim() || logTopics.trim(),
        studyMinutes: parseInt(logMinutes, 10) || 60,
        status: 'COMPLETED',
      });
      showToast('success', 'Daily progress logged successfully!');
      setIsLogProgressModalOpen(false);
      setLogTopics('');
      setLogCompleted('');
    } catch (err: unknown) {
      showToast('error', 'Failed to log progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleUpdateGoalProgress = async () => {
    if (!selectedGoal) return;
    try {
      await updateGoalMutation.mutateAsync({
        id: selectedGoal.id,
        data: {
          title: selectedGoal.title,
          description: selectedGoal.description || undefined,
          priority: selectedGoal.priority,
          startDate: selectedGoal.startDate,
          targetDate: selectedGoal.targetDate,
          progress: goalProgressValue,
          status: goalProgressValue >= 100 ? 'COMPLETED' : 'IN_PROGRESS',
        },
      });
      showToast('success', 'Goal progress updated successfully!');
      setIsUpdateGoalModalOpen(false);
      setSelectedGoal(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to update goal', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleCompleteTask = async (task: Task) => {
    try {
      await updateTaskMutation.mutateAsync({
        id: task.id,
        data: {
          title: task.title,
          description: task.description || undefined,
          priority: task.priority,
          estimatedMinutes: task.estimatedMinutes || undefined,
          dueDate: task.dueDate || undefined,
          status: 'COMPLETED',
        },
      });
      showToast('success', `Task "${task.title}" completed!`);
    } catch (err: unknown) {
      showToast('error', 'Failed to complete task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  return (
    <div className="space-y-4">
      {/* 1. Portal Welcome Bar */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            Welcome back, {user?.name || 'Developer'}
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Active Team: <span className="font-semibold text-slate-700 dark:text-slate-200">{activeTeam?.name || 'Default Team'}</span> • Today is {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="primary"
            size="sm"
            leftIcon={<Plus className="w-3.5 h-3.5" />}
            onClick={() => setIsLogProgressModalOpen(true)}
          >
            Log Daily Progress
          </Button>
        </div>
      </div>

      {/* 2. Key Metric Boxes */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase text-slate-500">Today's Focus</span>
            <Clock className="w-3.5 h-3.5 text-blue-600" />
          </div>
          <div className="text-lg font-bold text-slate-900 dark:text-slate-100 mt-1">
            {todayFocusMinutes} <span className="text-xs font-normal text-slate-400">mins</span>
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">Logged study/coding time</div>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase text-slate-500">Active Goals</span>
            <Target className="w-3.5 h-3.5 text-indigo-600" />
          </div>
          <div className="text-lg font-bold text-slate-900 dark:text-slate-100 mt-1">
            {activeGoals.length} <span className="text-xs font-normal text-slate-400">goals</span>
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">Target milestones</div>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase text-slate-500">Pending Tasks</span>
            <CheckSquare className="w-3.5 h-3.5 text-emerald-600" />
          </div>
          <div className="text-lg font-bold text-slate-900 dark:text-slate-100 mt-1">
            {pendingTasks.length} <span className="text-xs font-normal text-slate-400">tasks</span>
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">{taskCompletionRate}% completed</div>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase text-slate-500">Productivity Score</span>
            <TrendingUp className="w-3.5 h-3.5 text-amber-600" />
          </div>
          <div className="text-lg font-bold text-slate-900 dark:text-slate-100 mt-1">
            {totalVelocityScore} <span className="text-xs font-normal text-slate-400">/ 100</span>
          </div>
          <div className="text-[10px] text-slate-400 mt-0.5">Derived efficiency rating</div>
        </div>
      </div>

      {/* 3. Main Dashboard Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Left 2 Columns: Focus Chart & Sprint Tasks */}
        <div className="lg:col-span-2 space-y-4">
          {/* 7-Day Focus Distribution */}
          <Card>
            <CardHeader>
              <CardTitle>7-Day Focus Time Distribution</CardTitle>
              <span className="text-[11px] text-slate-500">Trailing Week</span>
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

          {/* Pending Sprint Tasks Table */}
          <Card>
            <CardHeader>
              <CardTitle>Sprint Task Queue</CardTitle>
              <NavLink to="/tasks" className="text-xs text-blue-600 hover:underline font-semibold flex items-center gap-0.5">
                View All Tasks <ChevronRight className="w-3.5 h-3.5" />
              </NavLink>
            </CardHeader>
            <CardContent className="p-0">
              {pendingTasks.length > 0 ? (
                <div className="overflow-x-auto">
                  <table className="classic-table">
                    <thead>
                      <tr>
                        <th>Task</th>
                        <th>Assignee</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th className="text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pendingTasks.slice(0, 5).map((t) => (
                        <tr key={t.id}>
                          <td className="font-semibold text-slate-900 dark:text-slate-100">{t.title}</td>
                          <td className="text-slate-600 dark:text-slate-400">{t.assigneeName}</td>
                          <td>
                            <Badge
                              size="sm"
                              variant={
                                t.priority === 'CRITICAL' || t.priority === 'HIGH'
                                  ? 'danger'
                                  : t.priority === 'MEDIUM'
                                  ? 'warning'
                                  : 'default'
                              }
                            >
                              {t.priority}
                            </Badge>
                          </td>
                          <td>
                            <Badge size="sm" variant={t.status === 'IN_PROGRESS' ? 'primary' : 'default'}>
                              {t.status}
                            </Badge>
                          </td>
                          <td className="text-right">
                            <button
                              onClick={() => handleCompleteTask(t)}
                              className="px-2 py-0.5 text-[11px] bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border border-emerald-300 rounded-[2px] font-semibold"
                            >
                              Mark Done
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="p-4 text-xs text-slate-500 italic text-center">No pending sprint tasks.</p>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Column: Learning Goals & Leaderboard */}
        <div className="space-y-4">
          {/* Active Goals Panel */}
          <Card>
            <CardHeader>
              <CardTitle>Active Learning Goals</CardTitle>
              <NavLink to="/goals" className="text-xs text-blue-600 hover:underline font-semibold flex items-center gap-0.5">
                Manage <ChevronRight className="w-3.5 h-3.5" />
              </NavLink>
            </CardHeader>
            <CardContent className="space-y-3">
              {activeGoals.slice(0, 3).map((g) => (
                <div key={g.id} className="p-2.5 bg-[#f8fafc] dark:bg-slate-800/60 border border-[#e2e8f0] dark:border-slate-700 rounded-[2px]">
                  <div className="flex items-center justify-between gap-1">
                    <span className="font-bold text-xs text-slate-900 dark:text-slate-100 truncate">{g.title}</span>
                    <Badge size="sm" variant="primary">
                      {g.progress}%
                    </Badge>
                  </div>
                  <div className="w-full bg-slate-200 dark:bg-slate-700 h-1.5 rounded-[2px] mt-2 overflow-hidden">
                    <div className="bg-blue-600 h-full rounded-[2px]" style={{ width: `${g.progress}%` }} />
                  </div>
                  <div className="flex items-center justify-between text-[10px] text-slate-400 mt-1.5">
                    <span>Due: {formatDate(g.targetDate)}</span>
                    <button
                      onClick={() => {
                        setSelectedGoal(g);
                        setGoalProgressValue(g.progress);
                        setIsUpdateGoalModalOpen(true);
                      }}
                      className="text-blue-600 hover:underline font-semibold"
                    >
                      Update %
                    </button>
                  </div>
                </div>
              ))}

              {activeGoals.length === 0 && (
                <p className="text-xs text-slate-500 italic text-center py-2">No active goals found.</p>
              )}
            </CardContent>
          </Card>

          {/* Team Standings Summary */}
          <Card>
            <CardHeader>
              <CardTitle>Team Standings</CardTitle>
              <NavLink to="/leaderboard" className="text-xs text-blue-600 hover:underline font-semibold flex items-center gap-0.5">
                Full Board <ChevronRight className="w-3.5 h-3.5" />
              </NavLink>
            </CardHeader>
            <CardContent className="p-0">
              {realLeaderboard.length > 0 ? (
                <table className="classic-table">
                  <thead>
                    <tr>
                      <th style={{ width: '30px' }}>#</th>
                      <th>Engineer</th>
                      <th className="text-right">Score</th>
                    </tr>
                  </thead>
                  <tbody>
                    {realLeaderboard.slice(0, 4).map((entry, idx) => (
                      <tr key={entry.userId}>
                        <td className="font-bold text-slate-500 text-[11px]">{idx + 1}</td>
                        <td className="font-semibold text-slate-900 dark:text-slate-100">{entry.userName}</td>
                        <td className="text-right font-bold text-blue-700 dark:text-blue-400">{entry.score} pts</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p className="p-4 text-xs text-slate-500 italic text-center">No standings available.</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Quick Log Progress Modal */}
      <Modal
        isOpen={isLogProgressModalOpen}
        onClose={() => setIsLogProgressModalOpen(false)}
        title="Log Daily Progress"
        description="Record your focus topics and completed deliverables for today"
      >
        <form onSubmit={handleQuickLog} className="space-y-3">
          <Input
            label="What did you study / build today? *"
            value={logTopics}
            onChange={(e) => setLogTopics(e.target.value)}
            placeholder="e.g. Distributed Consensus, Raft Algorithm implementation"
            required
          />
          <Input
            label="Key Deliverables Completed"
            value={logCompleted}
            onChange={(e) => setLogCompleted(e.target.value)}
            placeholder="e.g. Unit tests passed, Leader election algorithm completed"
          />
          <Input
            label="Focus Time (Minutes) *"
            type="number"
            min="1"
            max="1440"
            value={logMinutes}
            onChange={(e) => setLogMinutes(e.target.value)}
            required
          />

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsLogProgressModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createProgressMutation.isPending}>
              Save Progress Log
            </Button>
          </div>
        </form>
      </Modal>

      {/* Update Goal Modal */}
      {selectedGoal && (
        <Modal
          isOpen={isUpdateGoalModalOpen}
          onClose={() => setIsUpdateGoalModalOpen(false)}
          title={`Update Progress: ${selectedGoal.title}`}
          description="Adjust the percentage completed for this goal milestone"
        >
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-xs font-bold mb-1">
                <span>Completion Percentage</span>
                <span className="text-blue-600">{goalProgressValue}%</span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                step="5"
                value={goalProgressValue}
                onChange={(e) => setGoalProgressValue(Number(e.target.value))}
                className="w-full h-2 bg-slate-200 rounded-[2px] appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsUpdateGoalModalOpen(false)}>
                Cancel
              </Button>
              <Button
                type="button"
                variant="primary"
                size="sm"
                onClick={handleUpdateGoalProgress}
                isLoading={updateGoalMutation.isPending}
              >
                Update Goal
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
