import React, { useState } from 'react';
import {
  Target,
  Plus,
  Calendar,
  Sliders,
  Edit2,
  Trash2,
  Eye,
  CheckCircle2,
  Filter,
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
import { Pagination } from '@/components/ui/Pagination';
import { useGoals, useCreateGoal, useUpdateGoal, useDeleteGoal } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { formatDate } from '@/lib/utils';
import { Goal, GoalPriority, GoalStatus } from '@/types/goal';

export const GoalsPage: React.FC = () => {
  const { activeUser, activeTeam } = useApp();
  const { showToast } = useToast();

  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');

  // Modal states
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState<Goal | null>(null);
  const [viewingGoal, setViewingGoal] = useState<Goal | null>(null);
  const [progressGoal, setProgressGoal] = useState<Goal | null>(null);
  const [sliderProgress, setSliderProgress] = useState<number>(0);
  const [deletingGoalId, setDeletingGoalId] = useState<string | null>(null);

  // Form states
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<GoalPriority>('MEDIUM');
  const [goalStatus, setGoalStatus] = useState<GoalStatus>('IN_PROGRESS');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [targetDate, setTargetDate] = useState(
    new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );
  const [progressVal, setProgressVal] = useState(0);

  const { data: goalsData, isLoading } = useGoals({
    teamId: activeTeam?.id,
    status: statusFilter !== 'ALL' ? (statusFilter as GoalStatus) : undefined,
    priority: priorityFilter !== 'ALL' ? (priorityFilter as GoalPriority) : undefined,
    active: true,
    page,
    size: 9,
    sort: 'targetDate,asc',
  });

  // Query tasks for viewing goal details
  const { data: viewingTasksData } = useTasks({
    goalId: viewingGoal?.id,
    active: true,
    size: 20,
  });

  const createMutation = useCreateGoal();
  const updateMutation = useUpdateGoal();
  const deleteMutation = useDeleteGoal();

  const handleOpenCreate = () => {
    setEditingGoal(null);
    setTitle('');
    setDescription('');
    setPriority('MEDIUM');
    setGoalStatus('IN_PROGRESS');
    setStartDate(new Date().toISOString().split('T')[0]);
    setTargetDate(new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]);
    setProgressVal(0);
    setIsCreateModalOpen(true);
  };

  const handleOpenEdit = (goal: Goal) => {
    setEditingGoal(goal);
    setTitle(goal.title);
    setDescription(goal.description || '');
    setPriority(goal.priority);
    setGoalStatus(goal.status);
    setStartDate(goal.startDate);
    setTargetDate(goal.targetDate);
    setProgressVal(goal.progress);
    setIsCreateModalOpen(true);
  };

  const handleSubmitGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeUser || !activeTeam) {
      showToast('error', 'Please select an active team and user');
      return;
    }
    if (!title.trim()) {
      showToast('warning', 'Goal title is required');
      return;
    }

    try {
      if (editingGoal) {
        await updateMutation.mutateAsync({
          id: editingGoal.id,
          data: {
            title,
            description: description.trim() ? description : undefined,
            priority,
            status: progressVal >= 100 ? 'COMPLETED' : goalStatus,
            progress: progressVal,
            startDate,
            targetDate,
            active: true,
          },
        });
        showToast('success', 'Goal updated successfully');
      } else {
        await createMutation.mutateAsync({
          ownerId: activeUser.id,
          teamId: activeTeam.id,
          title,
          description: description.trim() ? description : undefined,
          priority,
          status: progressVal >= 100 ? 'COMPLETED' : goalStatus,
          progress: progressVal,
          startDate,
          targetDate,
        });
        showToast('success', 'Goal created successfully!');
      }
      setIsCreateModalOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save goal', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleUpdateProgressSlider = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!progressGoal) return;

    try {
      const isCompleted = sliderProgress >= 100;
      await updateMutation.mutateAsync({
        id: progressGoal.id,
        data: {
          title: progressGoal.title,
          description: progressGoal.description || undefined,
          priority: progressGoal.priority,
          startDate: progressGoal.startDate,
          targetDate: progressGoal.targetDate,
          progress: sliderProgress,
          status: isCompleted ? 'COMPLETED' : 'IN_PROGRESS',
        },
      });
      showToast(
        'success',
        isCompleted ? 'Goal marked as Completed at 100%! (+50 score)' : `Goal progress updated to ${sliderProgress}%`
      );
      setProgressGoal(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to update progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteGoal = async () => {
    if (!deletingGoalId) return;
    try {
      await deleteMutation.mutateAsync(deletingGoalId);
      showToast('success', 'Goal deleted successfully');
      setDeletingGoalId(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to delete goal', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const filteredGoals =
    goalsData?.content?.filter((g) => g.title.toLowerCase().includes(searchQuery.toLowerCase())) || [];

  const getDaysRemaining = (targetDateStr: string) => {
    const diff = new Date(targetDateStr).getTime() - new Date().getTime();
    const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
    if (days < 0) return { label: `${Math.abs(days)}d overdue`, isOverdue: true };
    if (days === 0) return { label: 'Due today', isOverdue: false };
    return { label: `${days}d left`, isOverdue: false };
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Target className="w-5 h-5 text-indigo-400" />
            Learning & Development Goals
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Set long-term learning goals, track milestones, and break them down into actionable tasks.
          </p>
        </div>
        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />} onClick={handleOpenCreate}>
          New Goal
        </Button>
      </div>

      {/* 2. Controls & Filter Bar */}
      <Card className="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <Input
            placeholder="Search goals by title..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />

          <Select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            options={[
              { label: 'All Statuses', value: 'ALL' },
              { label: 'In Progress', value: 'IN_PROGRESS' },
              { label: 'Completed', value: 'COMPLETED' },
              { label: 'Not Started', value: 'NOT_STARTED' },
              { label: 'On Hold', value: 'ON_HOLD' },
              { label: 'Cancelled', value: 'CANCELLED' },
            ]}
          />

          <Select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            options={[
              { label: 'All Priorities', value: 'ALL' },
              { label: 'Critical', value: 'CRITICAL' },
              { label: 'High', value: 'HIGH' },
              { label: 'Medium', value: 'MEDIUM' },
              { label: 'Low', value: 'LOW' },
            ]}
          />
        </div>
      </Card>

      {/* 3. Goal Cards Grid */}
      <div>
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <Skeleton className="h-44 w-full" />
            <Skeleton className="h-44 w-full" />
            <Skeleton className="h-44 w-full" />
          </div>
        ) : filteredGoals.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredGoals.map((goal) => {
              const daysInfo = getDaysRemaining(goal.targetDate);
              const isCompleted = goal.status === 'COMPLETED';
              return (
                <Card key={goal.id} hoverable className="flex flex-col justify-between space-y-4">
                  <div className="space-y-3">
                    <div className="flex items-start justify-between gap-2">
                      <h3 className="text-sm font-semibold text-slate-100 line-clamp-1">{goal.title}</h3>
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
                    </div>

                    {goal.description && (
                      <p className="text-xs text-slate-400 line-clamp-2">{goal.description}</p>
                    )}

                    {/* Progress slider / bar */}
                    <div className="space-y-1.5 pt-1">
                      <div className="flex items-center justify-between text-[11px]">
                        <span className="text-slate-400">Progress</span>
                        <span className="font-bold text-primary-400">{goal.progress}%</span>
                      </div>
                      <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                        <div
                          className={`h-full rounded-full transition-all duration-300 ${
                            isCompleted
                              ? 'bg-emerald-500'
                              : 'bg-gradient-to-r from-primary-600 to-indigo-500'
                          }`}
                          style={{ width: `${Math.min(goal.progress, 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* Metadata */}
                    <div className="flex items-center justify-between text-[11px] text-slate-500 pt-1">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3.5 h-3.5" />
                        {formatDate(goal.targetDate)}
                      </span>
                      <span
                        className={
                          daysInfo.isOverdue && !isCompleted ? 'text-rose-400 font-medium' : 'text-slate-400'
                        }
                      >
                        {isCompleted ? 'Completed' : daysInfo.label}
                      </span>
                    </div>
                  </div>

                  {/* Actions Footer */}
                  <div className="flex items-center justify-between pt-3 border-t border-slate-800/80">
                    <Badge variant={isCompleted ? 'success' : 'primary'} size="sm">
                      {goal.status}
                    </Badge>

                    <div className="flex items-center gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => {
                          setProgressGoal(goal);
                          setSliderProgress(goal.progress);
                        }}
                        title="Update progress slider"
                      >
                        <Sliders className="w-3.5 h-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => setViewingGoal(goal)}
                        title="View details & tasks"
                      >
                        <Eye className="w-3.5 h-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleOpenEdit(goal)}
                        title="Edit goal"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="text-rose-400 hover:text-rose-300"
                        onClick={() => setDeletingGoalId(goal.id)}
                        title="Delete goal"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </Button>
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        ) : (
          <EmptyState
            icon={Filter}
            title="No goals found"
            description="Create your first development goal or adjust your search filters."
            action={
              <Button variant="primary" size="sm" onClick={handleOpenCreate}>
                Create Goal
              </Button>
            }
          />
        )}

        {goalsData && goalsData.totalPages > 1 && (
          <Pagination
            currentPage={page}
            totalPages={goalsData.totalPages}
            totalElements={goalsData.totalElements}
            onPageChange={setPage}
          />
        )}
      </div>

      {/* Create / Edit Goal Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title={editingGoal ? 'Edit Goal' : 'Create Learning Goal'}
        description="Set a concrete target date and milestones for you and your team."
      >
        <form onSubmit={handleSubmitGoal} className="space-y-4">
          <Input
            label="Goal Title *"
            placeholder="e.g. Master Binary Trees & Dynamic Programming"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />

          <Input
            label="Description (Optional)"
            placeholder="e.g. Solve 50 medium problems and build an in-memory Trie"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Priority"
              value={priority}
              onChange={(e) => setPriority(e.target.value as GoalPriority)}
              options={[
                { label: 'Medium', value: 'MEDIUM' },
                { label: 'High', value: 'HIGH' },
                { label: 'Critical', value: 'CRITICAL' },
                { label: 'Low', value: 'LOW' },
              ]}
            />

            <Select
              label="Status"
              value={goalStatus}
              onChange={(e) => setGoalStatus(e.target.value as GoalStatus)}
              options={[
                { label: 'In Progress', value: 'IN_PROGRESS' },
                { label: 'Not Started', value: 'NOT_STARTED' },
                { label: 'Completed', value: 'COMPLETED' },
                { label: 'On Hold', value: 'ON_HOLD' },
              ]}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              type="date"
              label="Start Date *"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
            />

            <Input
              type="date"
              label="Target Date *"
              value={targetDate}
              onChange={(e) => setTargetDate(e.target.value)}
              required
            />
          </div>

          <div className="space-y-1.5">
            <div className="flex justify-between text-xs text-slate-300">
              <span>Initial Progress</span>
              <span className="font-bold text-primary-400">{progressVal}%</span>
            </div>
            <input
              type="range"
              min={0}
              max={100}
              value={progressVal}
              onChange={(e) => setProgressVal(Number(e.target.value))}
              className="w-full accent-primary-500 cursor-pointer h-2 bg-slate-800 rounded-lg"
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={createMutation.isPending || updateMutation.isPending}
            >
              {editingGoal ? 'Update Goal' : 'Create Goal'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Update Progress Slider Modal */}
      {progressGoal && (
        <Modal
          isOpen={!!progressGoal}
          onClose={() => setProgressGoal(null)}
          title={`Update Progress: ${progressGoal.title}`}
          description="Slide to adjust progress. Reaching 100% will automatically mark this goal as Completed."
        >
          <form onSubmit={handleUpdateProgressSlider} className="space-y-5">
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
              <Button type="button" variant="outline" size="sm" onClick={() => setProgressGoal(null)}>
                Cancel
              </Button>
              <Button type="submit" variant="primary" size="sm" isLoading={updateMutation.isPending}>
                Save Progress
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {/* Goal Details & Associated Tasks Modal */}
      {viewingGoal && (
        <Modal
          isOpen={!!viewingGoal}
          onClose={() => setViewingGoal(null)}
          title={viewingGoal.title}
          maxWidth="lg"
        >
          <div className="space-y-5 text-xs">
            {viewingGoal.description && (
              <p className="text-slate-300 bg-slate-950/50 p-3 rounded-xl border border-slate-800">
                {viewingGoal.description}
              </p>
            )}

            <div className="grid grid-cols-3 gap-3">
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-slate-500 block">Progress</span>
                <span className="text-primary-300 font-bold text-sm">{viewingGoal.progress}%</span>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-slate-500 block">Start Date</span>
                <span className="text-slate-200">{formatDate(viewingGoal.startDate)}</span>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-slate-500 block">Target Date</span>
                <span className="text-slate-200">{formatDate(viewingGoal.targetDate)}</span>
              </div>
            </div>

            {/* Associated Tasks */}
            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-slate-200 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                Associated Tasks ({viewingTasksData?.content?.length || 0})
              </h4>
              {viewingTasksData?.content && viewingTasksData.content.length > 0 ? (
                <div className="space-y-1.5 max-h-48 overflow-y-auto">
                  {viewingTasksData.content.map((t) => (
                    <div
                      key={t.id}
                      className="p-2.5 rounded-lg bg-slate-950/50 border border-slate-800 flex items-center justify-between"
                    >
                      <div className="flex items-center gap-2">
                        <span
                          className={`w-2 h-2 rounded-full ${
                            t.status === 'COMPLETED' ? 'bg-emerald-400' : 'bg-primary-400'
                          }`}
                        />
                        <span
                          className={
                            t.status === 'COMPLETED' ? 'line-through text-slate-500' : 'text-slate-200'
                          }
                        >
                          {t.title}
                        </span>
                      </div>
                      <Badge size="sm" variant={t.status === 'COMPLETED' ? 'success' : 'default'}>
                        {t.status}
                      </Badge>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-slate-500 text-xs italic">No tasks created under this goal yet.</p>
              )}
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setViewingGoal(null)}>
                Close
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* Delete Confirmation Modal */}
      {deletingGoalId && (
        <Modal
          isOpen={!!deletingGoalId}
          onClose={() => setDeletingGoalId(null)}
          title="Delete Goal"
          description="Are you sure you want to deactivate and remove this goal? This action will archive associated tasks."
        >
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" size="sm" onClick={() => setDeletingGoalId(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              size="sm"
              isLoading={deleteMutation.isPending}
              onClick={handleDeleteGoal}
            >
              Delete Goal
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
};
