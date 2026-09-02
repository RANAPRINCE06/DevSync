import React, { useState } from 'react';
import { Target, Plus, Search } from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { useGoals, useCreateGoal, useUpdateGoal, useDeleteGoal } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { formatDate } from '@/lib/utils';
import { Goal, GoalPriority } from '@/types/goal';

export const GoalsPage: React.FC = () => {
  const { activeTeam } = useApp();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedGoal, setSelectedGoal] = useState<Goal | null>(null);
  const [isProgressModalOpen, setIsProgressModalOpen] = useState(false);
  const [progressValue, setProgressValue] = useState(0);

  // Form State
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<GoalPriority>('MEDIUM');
  const [startDate] = useState(new Date().toISOString().split('T')[0]);
  const [targetDate, setTargetDate] = useState(
    new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );

  const { data: goalsPage, isLoading } = useGoals({ teamId: activeTeam?.id, active: true, size: 1000 });
  const { data: tasksPage } = useTasks({ teamId: activeTeam?.id, active: true, size: 1000 });

  const createGoalMutation = useCreateGoal();
  const updateGoalMutation = useUpdateGoal();
  const deleteGoalMutation = useDeleteGoal();

  const allGoals = goalsPage?.content || [];
  const allTasks = tasksPage?.content || [];

  const filteredGoals = allGoals.filter((g) => {
    const matchesSearch =
      g.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (g.description || '').toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || g.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleCreateGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !activeTeam) {
      showToast('error', 'No active user or team found');
      return;
    }
    try {
      await createGoalMutation.mutateAsync({
        ownerId: user.id,
        teamId: activeTeam.id,
        title: title.trim(),
        description: description.trim() || undefined,
        priority,
        startDate,
        targetDate,
      });
      showToast('success', 'Goal milestone created successfully!');
      setIsCreateModalOpen(false);
      setTitle('');
      setDescription('');
    } catch (err: unknown) {
      showToast('error', 'Failed to create goal', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleUpdateProgress = async () => {
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
          progress: progressValue,
          status: progressValue >= 100 ? 'COMPLETED' : 'IN_PROGRESS',
        },
      });
      showToast('success', 'Goal progress updated!');
      setIsProgressModalOpen(false);
      setSelectedGoal(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to update progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteGoal = async (id: string) => {
    if (!confirm('Are you sure you want to deactivate this goal?')) return;
    try {
      await deleteGoalMutation.mutateAsync(id);
      showToast('success', 'Goal deactivated');
    } catch (err: unknown) {
      showToast('error', 'Failed to deactivate goal', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  return (
    <div className="space-y-4">
      {/* 1. Goals Header Bar */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Target className="w-4 h-4 text-blue-600" />
            Learning Goals & Milestones
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Track structured engineering objectives and measure sprint progress
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-3.5 h-3.5" />}
          onClick={() => setIsCreateModalOpen(true)}
        >
          Create Goal
        </Button>
      </div>

      {/* 2. Filter Bar */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-2.5 rounded-[3px] flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2 flex-1 max-w-md">
          <div className="relative flex-1">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search goals by title or description..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-2.5 py-1 text-xs border border-[#cbd5e1] dark:border-slate-700 rounded-[2px] bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 focus:outline-none focus:border-blue-600"
            />
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-2 py-1 text-xs border border-[#cbd5e1] dark:border-slate-700 rounded-[2px] bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200"
          >
            <option value="ALL">All Statuses</option>
            <option value="NOT_STARTED">Not Started</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>

        <span className="text-[11px] text-slate-500 font-semibold">
          Showing {filteredGoals.length} of {allGoals.length} Goals
        </span>
      </div>

      {/* 3. Goals Table / List */}
      <Card>
        <CardHeader>
          <CardTitle>Milestone Inventory</CardTitle>
          <span className="text-[11px] text-slate-500">{filteredGoals.length} Goals Active</span>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : filteredGoals.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="classic-table">
                <thead>
                  <tr>
                    <th style={{ width: '40px' }}>#</th>
                    <th>Goal Title</th>
                    <th>Owner</th>
                    <th>Priority</th>
                    <th style={{ width: '180px' }}>Progress</th>
                    <th>Target Date</th>
                    <th>Status</th>
                    <th className="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredGoals.map((g, idx) => {
                    const goalTasks = allTasks.filter((t) => t.goalId === g.id);
                    const completedGoalTasks = goalTasks.filter((t) => t.status === 'COMPLETED');
                    return (
                      <tr key={g.id}>
                        <td className="text-slate-400 font-mono text-[11px]">{idx + 1}</td>
                        <td>
                          <div className="font-bold text-slate-900 dark:text-slate-100">{g.title}</div>
                          {g.description && <div className="text-[11px] text-slate-500 line-clamp-1">{g.description}</div>}
                          <div className="text-[10px] text-slate-400 mt-0.5">
                            {completedGoalTasks.length}/{goalTasks.length} tasks completed
                          </div>
                        </td>
                        <td className="text-slate-600 dark:text-slate-400">{g.ownerName}</td>
                        <td>
                          <Badge
                            size="sm"
                            variant={
                              g.priority === 'CRITICAL' || g.priority === 'HIGH'
                                ? 'danger'
                                : g.priority === 'MEDIUM'
                                ? 'warning'
                                : 'default'
                            }
                          >
                            {g.priority}
                          </Badge>
                        </td>
                        <td>
                          <div className="space-y-1">
                            <div className="flex justify-between text-[10px] font-bold">
                              <span>{g.progress}%</span>
                              <span className="text-slate-400">{g.status}</span>
                            </div>
                            <div className="w-full bg-slate-200 dark:bg-slate-700 h-1.5 rounded-[2px] overflow-hidden">
                              <div
                                className="bg-blue-600 h-full rounded-[2px]"
                                style={{ width: `${g.progress}%` }}
                              />
                            </div>
                          </div>
                        </td>
                        <td className="text-slate-500 text-[11px]">{formatDate(g.targetDate)}</td>
                        <td>
                          <Badge size="sm" variant={g.status === 'COMPLETED' ? 'success' : 'primary'}>
                            {g.status}
                          </Badge>
                        </td>
                        <td className="text-right whitespace-nowrap">
                          <button
                            onClick={() => {
                              setSelectedGoal(g);
                              setProgressValue(g.progress);
                              setIsProgressModalOpen(true);
                            }}
                            className="px-2 py-0.5 text-[11px] bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-300 rounded-[2px] font-semibold mr-1"
                          >
                            Update %
                          </button>
                          <button
                            onClick={() => handleDeleteGoal(g.id)}
                            className="px-2 py-0.5 text-[11px] bg-red-50 hover:bg-red-100 text-red-700 border border-red-300 rounded-[2px] font-semibold"
                          >
                            Delete
                          </button>
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
                icon={Target}
                title="No learning goals found"
                description="Create a goal to organize your tasks and daily focus areas."
                action={
                  <Button variant="primary" size="sm" onClick={() => setIsCreateModalOpen(true)}>
                    Create Goal
                  </Button>
                }
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create Goal Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create Goal Milestone"
        description="Define a new target milestone for your team"
      >
        <form onSubmit={handleCreateGoal} className="space-y-3">
          <Input
            label="Goal Title *"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Master Kafka Event Streaming"
            required
          />
          <Input
            label="Description (Optional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Understand partitioned consumer groups and offset management"
          />

          <div className="grid grid-cols-2 gap-2">
            <Select
              label="Priority *"
              value={priority}
              onChange={(e) => setPriority(e.target.value as GoalPriority)}
              options={[
                { label: 'Low', value: 'LOW' },
                { label: 'Medium', value: 'MEDIUM' },
                { label: 'High', value: 'HIGH' },
                { label: 'Critical', value: 'CRITICAL' },
              ]}
            />
            <Input
              label="Target Date *"
              type="date"
              value={targetDate}
              onChange={(e) => setTargetDate(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createGoalMutation.isPending}>
              Create Goal
            </Button>
          </div>
        </form>
      </Modal>

      {/* Update Progress Modal */}
      {selectedGoal && (
        <Modal
          isOpen={isProgressModalOpen}
          onClose={() => setIsProgressModalOpen(false)}
          title={`Update Progress: ${selectedGoal.title}`}
          description="Adjust completion percentage for this milestone"
        >
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-xs font-bold mb-1">
                <span>Completion Percentage</span>
                <span className="text-blue-600">{progressValue}%</span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                step="5"
                value={progressValue}
                onChange={(e) => setProgressValue(Number(e.target.value))}
                className="w-full h-2 bg-slate-200 rounded-[2px] appearance-none cursor-pointer accent-blue-600"
              />
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsProgressModalOpen(false)}>
                Cancel
              </Button>
              <Button
                type="button"
                variant="primary"
                size="sm"
                onClick={handleUpdateProgress}
                isLoading={updateGoalMutation.isPending}
              >
                Save Progress
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
