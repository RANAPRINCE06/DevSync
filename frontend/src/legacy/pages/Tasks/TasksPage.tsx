import React, { useState, useMemo } from 'react';
import {
  CheckSquare,
  Plus,
  Search,
  Calendar,
  User as UserIcon,
  Target,
  Edit2,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Eye,
  Kanban,
  List,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useToast } from '@/contexts/ToastContext';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { useTasks, useCreateTask, useUpdateTask, useDeleteTask } from '@/hooks/useTasks';
import { useGoals } from '@/hooks/useGoals';
import { formatDate } from '@/lib/utils';
import { Task, TaskStatus, TaskPriority } from '@/types/task';

export const TasksPage: React.FC = () => {
  const { activeUser, activeTeam, users } = useApp();
  const { showToast } = useToast();

  const [viewMode, setViewMode] = useState<'KANBAN' | 'LIST'>('KANBAN');
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [goalFilter, setGoalFilter] = useState<string>('ALL');
  const [onlyOverdue, setOnlyOverdue] = useState<boolean>(false);
  const [sortBy, setSortBy] = useState<'PRIORITY' | 'DUE_DATE' | 'CREATED_AT'>('DUE_DATE');

  // Modals
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [viewingTask, setViewingTask] = useState<Task | null>(null);
  const [deletingTaskId, setDeletingTaskId] = useState<string | null>(null);

  // Form states
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [selectedGoalId, setSelectedGoalId] = useState('');
  const [selectedAssigneeId, setSelectedAssigneeId] = useState('');
  const [estimatedMinutes, setEstimatedMinutes] = useState<number>(60);
  const [dueDate, setDueDate] = useState('');

  // Queries
  const { data: tasksPage, isLoading: isTasksLoading } = useTasks({
    teamId: activeTeam?.id,
    active: true,
    size: 100,
  });

  const { data: goalsPage } = useGoals({
    teamId: activeTeam?.id,
    active: true,
    size: 50,
  });

  // Mutations
  const createTaskMutation = useCreateTask();
  const updateTaskMutation = useUpdateTask();
  const deleteTaskMutation = useDeleteTask();

  const goalsList = goalsPage?.content || [];
  const allTasks = tasksPage?.content || [];

  // Filtered & Sorted Tasks
  const filteredTasks = useMemo(() => {
    return allTasks
      .filter((t) => {
        const matchesSearch =
          t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          (t.description && t.description.toLowerCase().includes(searchQuery.toLowerCase())) ||
          (t.assigneeName && t.assigneeName.toLowerCase().includes(searchQuery.toLowerCase()));

        const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter;
        const matchesPriority = priorityFilter === 'ALL' || t.priority === priorityFilter;
        const matchesGoal = goalFilter === 'ALL' || t.goalId === goalFilter;

        const isTaskOverdue =
          t.dueDate &&
          t.status !== 'COMPLETED' &&
          new Date(t.dueDate).getTime() < new Date().setHours(0, 0, 0, 0);
        const matchesOverdue = !onlyOverdue || isTaskOverdue;

        return matchesSearch && matchesStatus && matchesPriority && matchesGoal && matchesOverdue;
      })
      .sort((a, b) => {
        if (sortBy === 'PRIORITY') {
          const pOrder: Record<TaskPriority, number> = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
          return pOrder[b.priority] - pOrder[a.priority];
        }
        if (sortBy === 'DUE_DATE') {
          if (!a.dueDate) return 1;
          if (!b.dueDate) return -1;
          return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
        }
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      });
  }, [allTasks, searchQuery, statusFilter, priorityFilter, goalFilter, onlyOverdue, sortBy]);

  const handleOpenCreate = () => {
    setEditingTask(null);
    setTitle('');
    setDescription('');
    setPriority('MEDIUM');
    setStatus('TODO');
    setSelectedGoalId(goalsList[0]?.id || '');
    setSelectedAssigneeId(activeUser?.id || users[0]?.id || '');
    setEstimatedMinutes(60);
    setDueDate('');
    setIsCreateOpen(true);
  };

  const handleOpenEdit = (t: Task) => {
    setEditingTask(t);
    setTitle(t.title);
    setDescription(t.description || '');
    setPriority(t.priority);
    setStatus(t.status);
    setSelectedGoalId(t.goalId);
    setSelectedAssigneeId(t.assigneeId);
    setEstimatedMinutes(t.estimatedMinutes || 60);
    setDueDate(t.dueDate || '');
    setIsCreateOpen(true);
  };

  const handleSaveTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      showToast('warning', 'Task title is required');
      return;
    }
    if (!selectedGoalId) {
      showToast('warning', 'Please select an associated goal');
      return;
    }
    if (!selectedAssigneeId) {
      showToast('warning', 'Please select an assignee');
      return;
    }

    try {
      if (editingTask) {
        await updateTaskMutation.mutateAsync({
          id: editingTask.id,
          data: {
            title: title.trim(),
            description: description.trim() ? description : undefined,
            status,
            priority,
            estimatedMinutes: Number(estimatedMinutes) || undefined,
            dueDate: dueDate || undefined,
            active: true,
          },
        });
        showToast('success', 'Task updated successfully!');
      } else {
        await createTaskMutation.mutateAsync({
          goalId: selectedGoalId,
          assigneeId: selectedAssigneeId,
          title: title.trim(),
          description: description.trim() ? description : undefined,
          priority,
          estimatedMinutes: Number(estimatedMinutes) || undefined,
          dueDate: dueDate || undefined,
        });
        showToast('success', 'Task created successfully!');
      }
      setIsCreateOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleToggleTaskStatus = async (task: Task, nextStatus: TaskStatus) => {
    try {
      await updateTaskMutation.mutateAsync({
        id: task.id,
        data: {
          title: task.title,
          description: task.description || undefined,
          status: nextStatus,
          priority: task.priority,
          estimatedMinutes: task.estimatedMinutes || undefined,
          actualMinutes: task.actualMinutes || undefined,
          dueDate: task.dueDate || undefined,
          active: true,
        },
      });
      showToast(
        'success',
        nextStatus === 'COMPLETED' ? 'Task marked completed! (+20 pts)' : `Task moved to ${nextStatus}`
      );
    } catch (err: unknown) {
      showToast('error', 'Failed to update status', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteTask = async () => {
    if (!deletingTaskId) return;
    try {
      await deleteTaskMutation.mutateAsync(deletingTaskId);
      showToast('info', 'Task deleted successfully');
      setDeletingTaskId(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to delete task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const kanbanColumns: { id: TaskStatus; label: string; color: string }[] = [
    { id: 'TODO', label: 'To Do', color: 'border-slate-700' },
    { id: 'IN_PROGRESS', label: 'In Progress', color: 'border-primary-500/50' },
    { id: 'COMPLETED', label: 'Completed', color: 'border-emerald-500/50' },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <CheckSquare className="w-5 h-5 text-emerald-400" />
            Sprint & Milestone Tasks
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Break down active learning goals into actionable units of work.
          </p>
        </div>

        <div className="flex items-center gap-2">
          {/* View switcher */}
          <div className="flex items-center bg-slate-900 border border-slate-800 rounded-xl p-0.5">
            <button
              onClick={() => setViewMode('KANBAN')}
              className={`flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-medium transition-all ${
                viewMode === 'KANBAN'
                  ? 'bg-primary-600/30 text-primary-300 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Kanban className="w-3.5 h-3.5" /> Kanban
            </button>
            <button
              onClick={() => setViewMode('LIST')}
              className={`flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-medium transition-all ${
                viewMode === 'LIST'
                  ? 'bg-primary-600/30 text-primary-300 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <List className="w-3.5 h-3.5" /> List
            </button>
          </div>

          <Button
            variant="primary"
            size="sm"
            leftIcon={<Plus className="w-4 h-4" />}
            onClick={handleOpenCreate}
          >
            Create Task
          </Button>
        </div>
      </div>

      {/* 2. Filter Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 p-3 rounded-2xl bg-slate-900/80 border border-slate-800">
        <div className="flex flex-wrap items-center gap-2 flex-1 max-w-2xl">
          {/* Search */}
          <div className="relative min-w-[200px] flex-1">
            <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search tasks, descriptions, assignees..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
            />
          </div>

          {/* Goal Filter */}
          <select
            value={goalFilter}
            onChange={(e) => setGoalFilter(e.target.value)}
            className="h-8 px-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
          >
            <option value="ALL">All Goals</option>
            {goalsList.map((g) => (
              <option key={g.id} value={g.id}>
                {g.title}
              </option>
            ))}
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

          {/* Overdue Toggle */}
          <button
            onClick={() => setOnlyOverdue(!onlyOverdue)}
            className={`h-8 px-2.5 rounded-xl border text-xs flex items-center gap-1.5 transition-all ${
              onlyOverdue
                ? 'bg-rose-950/40 border-rose-800 text-rose-300'
                : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:text-slate-200'
            }`}
          >
            <AlertCircle className="w-3.5 h-3.5 text-rose-400" /> Overdue Only
          </button>
        </div>

        {/* Sort selector */}
        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="h-8 px-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
          >
            <option value="DUE_DATE">Due Date</option>
            <option value="PRIORITY">Priority</option>
            <option value="CREATED_AT">Newest First</option>
          </select>
        </div>
      </div>

      {/* 3. Task Views */}
      {isTasksLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Skeleton className="h-64 rounded-2xl" />
          <Skeleton className="h-64 rounded-2xl" />
          <Skeleton className="h-64 rounded-2xl" />
        </div>
      ) : filteredTasks.length === 0 ? (
        <EmptyState
          icon={CheckSquare}
          title="No matching tasks found"
          description="Try clearing your search query or filters, or create a new task."
          action={
            <Button variant="primary" size="sm" onClick={handleOpenCreate}>
              Create Task
            </Button>
          }
        />
      ) : viewMode === 'KANBAN' ? (
        /* KANBAN BOARD */
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-start">
          {kanbanColumns.map((col) => {
            const colTasks = filteredTasks.filter((t) => t.status === col.id);
            return (
              <div
                key={col.id}
                className="rounded-2xl bg-slate-900/50 border border-slate-800/80 p-3 space-y-3"
              >
                {/* Column Header */}
                <div className="flex items-center justify-between px-1">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-200">{col.label}</span>
                    <span className="px-1.5 py-0.2 rounded-full bg-slate-800 text-[10px] font-mono text-slate-400">
                      {colTasks.length}
                    </span>
                  </div>
                </div>

                {/* Column Tasks */}
                <div className="space-y-2 min-h-[160px]">
                  {colTasks.length > 0 ? (
                    colTasks.map((t) => {
                      const isOverdue =
                        t.dueDate &&
                        t.status !== 'COMPLETED' &&
                        new Date(t.dueDate).getTime() < new Date().setHours(0, 0, 0, 0);

                      return (
                        <div
                          key={t.id}
                          className="p-3.5 rounded-xl bg-slate-950/80 border border-slate-800/90 hover:border-slate-700 transition-all space-y-2.5 group shadow-sm"
                        >
                          <div className="flex items-start justify-between gap-2">
                            <span className="text-xs font-semibold text-slate-200 line-clamp-2">
                              {t.title}
                            </span>
                            <Badge
                              size="sm"
                              variant={
                                t.priority === 'CRITICAL'
                                  ? 'danger'
                                  : t.priority === 'HIGH'
                                  ? 'warning'
                                  : 'default'
                              }
                            >
                              {t.priority}
                            </Badge>
                          </div>

                          {t.description && (
                            <p className="text-[11px] text-slate-400 line-clamp-2">
                              {t.description}
                            </p>
                          )}

                          <div className="space-y-1 pt-1 border-t border-slate-850 text-[10px] text-slate-500">
                            <div className="flex items-center justify-between">
                              <span className="flex items-center gap-1 truncate max-w-[130px]">
                                <Target className="w-3 h-3 text-indigo-400 shrink-0" />
                                {t.goalTitle}
                              </span>
                              <span className="flex items-center gap-1">
                                <UserIcon className="w-3 h-3 text-slate-400" />
                                {t.assigneeName}
                              </span>
                            </div>

                            {t.dueDate && (
                              <div
                                className={`flex items-center gap-1 pt-0.5 ${
                                  isOverdue ? 'text-rose-400 font-semibold' : 'text-slate-400'
                                }`}
                              >
                                <Calendar className="w-3 h-3" />
                                <span>Due {formatDate(t.dueDate)}</span>
                                {isOverdue && <span>(Overdue)</span>}
                              </div>
                            )}
                          </div>

                          {/* Quick Status Buttons & Actions */}
                          <div className="flex items-center justify-between pt-1 border-t border-slate-850">
                            <div className="flex items-center gap-1">
                              {col.id !== 'TODO' && (
                                <button
                                  onClick={() => handleToggleTaskStatus(t, 'TODO')}
                                  className="text-[10px] text-slate-400 hover:text-slate-200 px-1.5 py-0.5 rounded bg-slate-900 border border-slate-800"
                                >
                                  To Todo
                                </button>
                              )}
                              {col.id !== 'IN_PROGRESS' && (
                                <button
                                  onClick={() => handleToggleTaskStatus(t, 'IN_PROGRESS')}
                                  className="text-[10px] text-primary-400 hover:text-primary-300 px-1.5 py-0.5 rounded bg-primary-950/40 border border-primary-800/60"
                                >
                                  In Progress
                                </button>
                              )}
                              {col.id !== 'COMPLETED' && (
                                <button
                                  onClick={() => handleToggleTaskStatus(t, 'COMPLETED')}
                                  className="text-[10px] text-emerald-400 hover:text-emerald-300 px-1.5 py-0.5 rounded bg-emerald-950/40 border border-emerald-800/60"
                                >
                                  Complete
                                </button>
                              )}
                            </div>

                            <div className="flex items-center gap-1">
                              <button
                                onClick={() => setViewingTask(t)}
                                className="p-1 text-slate-500 hover:text-slate-300 transition-colors"
                                title="View details"
                              >
                                <Eye className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={() => handleOpenEdit(t)}
                                className="p-1 text-slate-500 hover:text-slate-300 transition-colors"
                                title="Edit task"
                              >
                                <Edit2 className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={() => setDeletingTaskId(t.id)}
                                className="p-1 text-slate-500 hover:text-rose-400 transition-colors"
                                title="Delete task"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="h-32 flex flex-col items-center justify-center text-center p-4 border border-dashed border-slate-800/80 rounded-xl text-slate-500 text-xs">
                      No tasks in {col.label.toLowerCase()}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        /* LIST VIEW */
        <div className="space-y-2">
          {filteredTasks.map((t) => {
            const isCompleted = t.status === 'COMPLETED';
            const isOverdue =
              t.dueDate &&
              !isCompleted &&
              new Date(t.dueDate).getTime() < new Date().setHours(0, 0, 0, 0);

            return (
              <div
                key={t.id}
                className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800/80 flex items-center justify-between gap-4 hover:border-slate-700 transition-all"
              >
                <div className="flex items-center gap-3 min-w-0">
                  <button
                    onClick={() => handleToggleTaskStatus(t, isCompleted ? 'TODO' : 'COMPLETED')}
                    className={`w-5 h-5 rounded-lg border flex items-center justify-center shrink-0 transition-colors ${
                      isCompleted
                        ? 'bg-emerald-600 border-emerald-500 text-white'
                        : 'border-slate-700 hover:border-emerald-500 text-transparent'
                    }`}
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" />
                  </button>

                  <div className="min-w-0">
                    <p
                      className={`text-xs font-semibold truncate ${
                        isCompleted ? 'line-through text-slate-500' : 'text-slate-200'
                      }`}
                    >
                      {t.title}
                    </p>
                    <p className="text-[10px] text-slate-500 truncate flex items-center gap-2">
                      <span>{t.goalTitle}</span>
                      <span>•</span>
                      <span>Assignee: {t.assigneeName}</span>
                      {t.dueDate && (
                        <>
                          <span>•</span>
                          <span className={isOverdue ? 'text-rose-400 font-semibold' : ''}>
                            Due {formatDate(t.dueDate)}
                          </span>
                        </>
                      )}
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-3 shrink-0">
                  <Badge
                    size="sm"
                    variant={
                      t.status === 'COMPLETED'
                        ? 'success'
                        : t.status === 'IN_PROGRESS'
                        ? 'primary'
                        : 'default'
                    }
                  >
                    {t.status}
                  </Badge>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => setViewingTask(t)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800"
                    >
                      <Eye className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleOpenEdit(t)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => setDeletingTaskId(t.id)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-950/30"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create/Edit Task Modal */}
      <Modal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        title={editingTask ? 'Edit Task' : 'Create Sprint Task'}
        description="Assign actionable work tied to learning milestones."
      >
        <form onSubmit={handleSaveTask} className="space-y-4">
          <Input
            label="Task Title *"
            placeholder="e.g. Implement binary search tree traversal"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />

          <Input
            label="Description (Optional)"
            placeholder="e.g. Include recursive and iterative approaches with unit tests"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Select
              label="Associated Goal *"
              value={selectedGoalId}
              onChange={(e) => setSelectedGoalId(e.target.value)}
              options={goalsList.map((g) => ({ label: g.title, value: g.id }))}
              required
            />

            <Select
              label="Assignee *"
              value={selectedAssigneeId}
              onChange={(e) => setSelectedAssigneeId(e.target.value)}
              options={users.map((u) => ({ label: u.name, value: u.id }))}
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <Select
              label="Priority"
              value={priority}
              onChange={(e) => setPriority(e.target.value as TaskPriority)}
              options={[
                { label: 'Low', value: 'LOW' },
                { label: 'Medium', value: 'MEDIUM' },
                { label: 'High', value: 'HIGH' },
                { label: 'Critical', value: 'CRITICAL' },
              ]}
            />

            <Select
              label="Status"
              value={status}
              onChange={(e) => setStatus(e.target.value as TaskStatus)}
              options={[
                { label: 'To Do', value: 'TODO' },
                { label: 'In Progress', value: 'IN_PROGRESS' },
                { label: 'Completed', value: 'COMPLETED' },
              ]}
            />

            <Input
              type="number"
              label="Est. Minutes"
              value={estimatedMinutes}
              onChange={(e) => setEstimatedMinutes(Number(e.target.value))}
              min={5}
            />
          </div>

          <Input
            type="date"
            label="Due Date (Optional)"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
          />

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
              isLoading={createTaskMutation.isPending || updateTaskMutation.isPending}
            >
              {editingTask ? 'Update Task' : 'Create Task'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Task Detail Modal */}
      {viewingTask && (
        <Modal
          isOpen={!!viewingTask}
          onClose={() => setViewingTask(null)}
          title={viewingTask.title}
          description={`Associated with goal: ${viewingTask.goalTitle}`}
        >
          <div className="space-y-4 text-xs">
            {viewingTask.description && (
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
                <span className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold block mb-1">
                  Description
                </span>
                <p className="text-slate-200 whitespace-pre-wrap">{viewingTask.description}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-3">
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Status</span>
                <Badge
                  size="sm"
                  className="mt-1"
                  variant={
                    viewingTask.status === 'COMPLETED'
                      ? 'success'
                      : viewingTask.status === 'IN_PROGRESS'
                      ? 'primary'
                      : 'default'
                  }
                >
                  {viewingTask.status}
                </Badge>
              </div>

              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Priority</span>
                <Badge
                  size="sm"
                  className="mt-1"
                  variant={
                    viewingTask.priority === 'CRITICAL'
                      ? 'danger'
                      : viewingTask.priority === 'HIGH'
                      ? 'warning'
                      : 'default'
                  }
                >
                  {viewingTask.priority}
                </Badge>
              </div>

              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Assignee</span>
                <span className="font-semibold text-slate-200 mt-1 block">
                  {viewingTask.assigneeName}
                </span>
              </div>

              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Due Date</span>
                <span className="font-semibold text-slate-200 mt-1 block">
                  {viewingTask.dueDate ? formatDate(viewingTask.dueDate) : 'No deadline'}
                </span>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setViewingTask(null)}>
                Close
              </Button>
              <Button
                variant="primary"
                size="sm"
                onClick={() => {
                  const current = viewingTask;
                  setViewingTask(null);
                  handleOpenEdit(current);
                }}
              >
                Edit Task
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* Delete Confirmation Modal */}
      {deletingTaskId && (
        <Modal
          isOpen={!!deletingTaskId}
          onClose={() => setDeletingTaskId(null)}
          title="Delete Task"
          description="Are you sure you want to delete this task? This action cannot be undone."
        >
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" size="sm" onClick={() => setDeletingTaskId(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              size="sm"
              onClick={handleDeleteTask}
              isLoading={deleteTaskMutation.isPending}
            >
              Confirm Delete
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
};
