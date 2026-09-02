import React, { useState, useMemo } from 'react';
import {
  Target,
  Plus,
  Search,
  Sliders,
  Calendar,
  Trash2,
  Edit2,
  CheckSquare,
  Eye,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useToast } from '@/contexts/ToastContext';
import { Card } from '@/components/ui/Card';
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
  const { activeUser, activeTeam } = useApp();
  const { showToast } = useToast();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState<'TARGET_DATE' | 'PROGRESS' | 'PRIORITY'>('TARGET_DATE');

  // Modals
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState<Goal | null>(null);
  const [viewingGoal, setViewingGoal] = useState<Goal | null>(null);
  const [progressModalGoal, setProgressModalGoal] = useState<Goal | null>(null);
  const [sliderProgress, setSliderProgress] = useState(0);
  const [deletingGoalId, setDeletingGoalId] = useState<string | null>(null);

  // Form states
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<GoalPriority>('MEDIUM');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [targetDate, setTargetDate] = useState('');
  const [progress, setProgress] = useState(0);

  // Queries
  const { data: goalsPage, isLoading: isGoalsLoading } = useGoals({
    teamId: activeTeam?.id,
    active: true,
    size: 50,
  });

  const { data: tasksPage } = useTasks({
    teamId: activeTeam?.id,
    active: true,
    size: 100,
  });

  // Mutations
  const createGoalMutation = useCreateGoal();
  const updateGoalMutation = useUpdateGoal();
  const deleteGoalMutation = useDeleteGoal();

  const allGoals = goalsPage?.content || [];
  const allTasks = tasksPage?.content || [];

  // Goal Health Indicator Helper
  const getGoalHealth = (goal: Goal) => {
    if (goal.status === 'COMPLETED' || goal.progress >= 100) {
      return { label: 'Completed', variant: 'success' as const };
    }
    const diff = new Date(goal.targetDate).getTime() - new Date().getTime();
    const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
    if (days < 0) {
      return { label: 'Overdue', variant: 'danger' as const };
    }
    if (days <= 3 && goal.progress < 50) {
      return { label: 'At Risk', variant: 'warning' as const };
    }
    return { label: 'On Track', variant: 'primary' as const };
  };

  // Filter & Sort Goals
  const filteredGoals = useMemo(() => {
    return allGoals
      .filter((g) => {
        const matchesSearch =
          g.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          (g.description && g.description.toLowerCase().includes(searchQuery.toLowerCase()));
        const matchesStatus = statusFilter === 'ALL' || g.status === statusFilter;
        const matchesPriority = priorityFilter === 'ALL' || g.priority === priorityFilter;
        return matchesSearch && matchesStatus && matchesPriority;
      })
      .sort((a, b) => {
        if (sortBy === 'PROGRESS') return b.progress - a.progress;
        if (sortBy === 'PRIORITY') {
          const pOrder: Record<GoalPriority, number> = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
          return pOrder[b.priority] - pOrder[a.priority];
        }
        return new Date(a.targetDate).getTime() - new Date(b.targetDate).getTime();
      });
  }, [allGoals, searchQuery, statusFilter, priorityFilter, sortBy]);

  const handleOpenCreate = () => {
    setEditingGoal(null);
    setTitle('');
    setDescription('');
    setPriority('MEDIUM');
    setStartDate(new Date().toISOString().split('T')[0]);
    const target = new Date();
    target.setDate(target.getDate() + 14);
    setTargetDate(target.toISOString().split('T')[0]);
    setProgress(0);
    setIsCreateOpen(true);
  };

  const handleOpenEdit = (g: Goal) => {
    setEditingGoal(g);
    setTitle(g.title);
    setDescription(g.description || '');
    setPriority(g.priority);
    setStartDate(g.startDate);
    setTargetDate(g.targetDate);
    setProgress(g.progress);
    setIsCreateOpen(true);
  };

  const handleSaveGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      showToast('warning', 'Title is required');
      return;
    }
    if (!activeUser || !activeTeam) {
      showToast('error', 'Active user and team required');
      return;
    }

    try {
      if (editingGoal) {
        await updateGoalMutation.mutateAsync({
          id: editingGoal.id,
          data: {
            title: title.trim(),
            description: description.trim() ? description : undefined,
            priority,
            startDate,
            targetDate,
            progress,
            status: progress >= 100 ? 'COMPLETED' : editingGoal.status,
          },
        });
        showToast('success', 'Goal updated successfully!');
      } else {
        await createGoalMutation.mutateAsync({
          ownerId: activeUser.id,
          teamId: activeTeam.id,
          title: title.trim(),
          description: description.trim() ? description : undefined,
          priority,
          startDate,
          targetDate,
        });
        showToast('success', 'Goal created successfully!');
      }
      setIsCreateOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save goal', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleSaveQuickProgress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!progressModalGoal) return;
    try {
      const isCompleted = sliderProgress >= 100;
      await updateGoalMutation.mutateAsync({
        id: progressModalGoal.id,
        data: {
          title: progressModalGoal.title,
          description: progressModalGoal.description || undefined,
          priority: progressModalGoal.priority,
          startDate: progressModalGoal.startDate,
          targetDate: progressModalGoal.targetDate,
          progress: sliderProgress,
          status: isCompleted ? 'COMPLETED' : 'IN_PROGRESS',
        },
      });
      showToast(
        'success',
        isCompleted ? 'Goal completed at 100%! (+50 score)' : `Goal progress updated to ${sliderProgress}%`
      );
      setProgressModalGoal(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to update progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteGoal = async () => {
    if (!deletingGoalId) return;
    try {
      await deleteGoalMutation.mutateAsync(deletingGoalId);
      showToast('info', 'Goal deleted successfully');
      setDeletingGoalId(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to delete goal', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Target className="w-5 h-5 text-indigo-400" />
            Learning Goals & Objectives
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Track high-level engineering objectives and technical milestones.
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={handleOpenCreate}
        >
          Create Goal
        </Button>
      </div>

      {/* 2. Filter Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 p-3 rounded-2xl bg-slate-900/80 border border-slate-800">
        <div className="flex flex-wrap items-center gap-2 flex-1 max-w-xl">
          {/* Search */}
          <div className="relative min-w-[200px] flex-1">
            <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search goals..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
            />
          </div>

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-8 px-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
          >
            <option value="ALL">All Statuses</option>
            <option value="NOT_STARTED">Not Started</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="ON_HOLD">On Hold</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          {/* Priority Filter */}
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="h-8 px-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
          >
            <option value="ALL">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>

        {/* Sort Selector */}
        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="h-8 px-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
          >
            <option value="TARGET_DATE">Target Date</option>
            <option value="PROGRESS">Progress %</option>
            <option value="PRIORITY">Priority</option>
          </select>
        </div>
      </div>

      {/* 3. Goals Grid */}
      {isGoalsLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Skeleton className="h-48 rounded-2xl" />
          <Skeleton className="h-48 rounded-2xl" />
          <Skeleton className="h-48 rounded-2xl" />
        </div>
      ) : filteredGoals.length === 0 ? (
        <EmptyState
          icon={Target}
          title="No goals match criteria"
          description="Create your first goal or try adjusting your search filters."
          action={
            <Button variant="primary" size="sm" onClick={handleOpenCreate}>
              Create Goal
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredGoals.map((goal) => {
            const health = getGoalHealth(goal);
            const associatedTasks = allTasks.filter((t) => t.goalId === goal.id);
            const completedCount = associatedTasks.filter((t) => t.status === 'COMPLETED').length;

            return (
              <Card
                key={goal.id}
                hoverable
                className="flex flex-col justify-between p-4 space-y-3 relative group"
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <span className="text-sm font-bold text-slate-100 line-clamp-1">{goal.title}</span>
                    <div className="flex items-center gap-1.5 shrink-0">
                      <Badge size="sm" variant={health.variant}>
                        {health.label}
                      </Badge>
                      <Badge
                        size="sm"
                        variant={
                          goal.priority === 'CRITICAL'
                            ? 'danger'
                            : goal.priority === 'HIGH'
                            ? 'warning'
                            : 'default'
                        }
                      >
                        {goal.priority}
                      </Badge>
                    </div>
                  </div>

                  {goal.description && (
                    <p className="text-xs text-slate-400 line-clamp-2 mt-1.5">{goal.description}</p>
                  )}
                </div>

                {/* Progress Bar & Stats */}
                <div className="space-y-2 pt-2 border-t border-slate-850">
                  <div className="flex items-center justify-between text-xs text-slate-300">
                    <span>Progress</span>
                    <span className="font-bold text-primary-400">{goal.progress}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-primary-600 to-indigo-500 rounded-full transition-all duration-300"
                      style={{ width: `${Math.min(goal.progress, 100)}%` }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3 h-3 text-slate-400" />
                      Due {formatDate(goal.targetDate)}
                    </span>
                    <span className="flex items-center gap-1">
                      <CheckSquare className="w-3 h-3 text-slate-400" />
                      {completedCount}/{associatedTasks.length} tasks
                    </span>
                  </div>
                </div>

                {/* Quick Action Footer */}
                <div className="flex items-center justify-between pt-2 border-t border-slate-850">
                  <button
                    onClick={() => {
                      setProgressModalGoal(goal);
                      setSliderProgress(goal.progress);
                    }}
                    className="text-xs text-primary-400 hover:text-primary-300 flex items-center gap-1 font-medium transition-colors"
                  >
                    <Sliders className="w-3.5 h-3.5" /> Update %
                  </button>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => setViewingGoal(goal)}
                      className="p-1 text-slate-500 hover:text-slate-300 transition-colors"
                      title="View Details"
                    >
                      <Eye className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleOpenEdit(goal)}
                      className="p-1 text-slate-500 hover:text-slate-300 transition-colors"
                      title="Edit Goal"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => setDeletingGoalId(goal.id)}
                      className="p-1 text-slate-500 hover:text-rose-400 transition-colors"
                      title="Delete Goal"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create/Edit Goal Modal */}
      <Modal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        title={editingGoal ? 'Edit Learning Goal' : 'Create Learning Goal'}
        description="Set measurable objectives for your engineering growth."
      >
        <form onSubmit={handleSaveGoal} className="space-y-4">
          <Input
            label="Goal Title *"
            placeholder="e.g. Master Distributed Systems & Event-Driven Architecture"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />

          <Input
            label="Description (Optional)"
            placeholder="e.g. Read Designing Data-Intensive Applications chapters 1-6"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <Select
              label="Priority"
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
              type="date"
              label="Start Date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
            />

            <Input
              type="date"
              label="Target Date"
              value={targetDate}
              onChange={(e) => setTargetDate(e.target.value)}
              required
            />
          </div>

          {editingGoal && (
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs text-slate-300">
                <span>Progress</span>
                <span className="font-bold text-primary-400">{progress}%</span>
              </div>
              <input
                type="range"
                min={0}
                max={100}
                value={progress}
                onChange={(e) => setProgress(Number(e.target.value))}
                className="w-full accent-primary-500 cursor-pointer h-2 bg-slate-800 rounded-lg"
              />
            </div>
          )}

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => setIsCreateOpen(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={createGoalMutation.isPending || updateGoalMutation.isPending}
            >
              {editingGoal ? 'Update Goal' : 'Create Goal'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Goal Details Modal */}
      {viewingGoal && (
        <Modal
          isOpen={!!viewingGoal}
          onClose={() => setViewingGoal(null)}
          title={viewingGoal.title}
          description={`Created on ${formatDate(viewingGoal.createdAt)}`}
        >
          <div className="space-y-4 text-xs">
            {viewingGoal.description && (
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
                <span className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold block mb-1">
                  Description
                </span>
                <p className="text-slate-200">{viewingGoal.description}</p>
              </div>
            )}

            {/* Health & Progress */}
            <div className="grid grid-cols-2 gap-3">
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Health Status</span>
                <Badge size="sm" variant={getGoalHealth(viewingGoal).variant} className="mt-1">
                  {getGoalHealth(viewingGoal).label}
                </Badge>
              </div>

              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Target Deadline</span>
                <span className="font-semibold text-slate-200 mt-1 block">
                  {formatDate(viewingGoal.targetDate)}
                </span>
              </div>
            </div>

            {/* Associated Tasks List */}
            <div className="space-y-2">
              <span className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold block">
                Associated Tasks
              </span>
              {allTasks.filter((t) => t.goalId === viewingGoal.id).length > 0 ? (
                <div className="space-y-1.5 max-h-40 overflow-y-auto">
                  {allTasks
                    .filter((t) => t.goalId === viewingGoal.id)
                    .map((task) => (
                      <div
                        key={task.id}
                        className="p-2 rounded-lg bg-slate-950/60 border border-slate-800 flex items-center justify-between"
                      >
                        <span className="text-slate-300 truncate">{task.title}</span>
                        <Badge size="sm" variant={task.status === 'COMPLETED' ? 'success' : 'default'}>
                          {task.status}
                        </Badge>
                      </div>
                    ))}
                </div>
              ) : (
                <p className="text-slate-500 italic">No associated tasks created for this goal yet.</p>
              )}
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setViewingGoal(null)}>
                Close
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* Quick Progress Slider Modal */}
      {progressModalGoal && (
        <Modal
          isOpen={!!progressModalGoal}
          onClose={() => setProgressModalGoal(null)}
          title={`Update Progress: ${progressModalGoal.title}`}
          description="Adjust current goal progress. 100% completion awards 50 points to team leaderboard."
        >
          <form onSubmit={handleSaveQuickProgress} className="space-y-5">
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs text-slate-300">
                <span>Progress Percentage</span>
                <span className="text-base font-bold text-primary-400">{sliderProgress}%</span>
              </div>
              <input
                type="range"
                min={0}
                max={100}
                value={sliderProgress}
                onChange={(e) => setSliderProgress(Number(e.target.value))}
                className="w-full accent-primary-500 cursor-pointer h-2 bg-slate-800 rounded-lg"
              />
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setProgressModalGoal(null)}
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

      {/* Delete Confirmation Modal */}
      {deletingGoalId && (
        <Modal
          isOpen={!!deletingGoalId}
          onClose={() => setDeletingGoalId(null)}
          title="Delete Goal"
          description="Are you sure you want to delete this goal? Any associated tasks will remain."
        >
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" size="sm" onClick={() => setDeletingGoalId(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              size="sm"
              onClick={handleDeleteGoal}
              isLoading={deleteGoalMutation.isPending}
            >
              Confirm Delete
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
};
