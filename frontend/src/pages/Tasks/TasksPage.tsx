import React, { useState } from 'react';
import {
  CheckSquare,
  Plus,
  Edit2,
  Trash2,
  CheckCircle2,
  List,
  Kanban,
  User as UserIcon,
  Filter,
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
import { Pagination } from '@/components/ui/Pagination';
import { useTasks, useCreateTask, useUpdateTask, useDeleteTask } from '@/hooks/useTasks';
import { useGoals } from '@/hooks/useGoals';
import { formatDate } from '@/lib/utils';
import { Task, TaskPriority, TaskStatus } from '@/types/task';

export const TasksPage: React.FC = () => {
  const { activeUser, activeTeam, users } = useApp();
  const { showToast } = useToast();

  const [viewMode, setViewMode] = useState<'list' | 'kanban'>('kanban');
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [selectedGoalFilter, setSelectedGoalFilter] = useState<string>('ALL');

  // Modal States
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [deletingTaskId, setDeletingTaskId] = useState<string | null>(null);

  // Form Fields
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDescription, setTaskDescription] = useState('');
  const [goalId, setGoalId] = useState('');
  const [assigneeId, setAssigneeId] = useState('');
  const [taskPriority, setTaskPriority] = useState<TaskPriority>('MEDIUM');
  const [taskStatus, setTaskStatus] = useState<TaskStatus>('TODO');
  const [estimatedMinutes, setEstimatedMinutes] = useState(60);
  const [dueDate, setDueDate] = useState(
    new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );

  const { data: tasksData, isLoading } = useTasks({
    teamId: activeTeam?.id,
    goalId: selectedGoalFilter !== 'ALL' ? selectedGoalFilter : undefined,
    status: statusFilter !== 'ALL' ? (statusFilter as TaskStatus) : undefined,
    priority: priorityFilter !== 'ALL' ? (priorityFilter as TaskPriority) : undefined,
    active: true,
    page,
    size: viewMode === 'kanban' ? 50 : 10,
    sort: 'dueDate,asc',
  });

  const { data: goalsData } = useGoals({ teamId: activeTeam?.id, active: true, size: 50 });

  const createMutation = useCreateTask();
  const updateMutation = useUpdateTask();
  const deleteMutation = useDeleteTask();

  const handleOpenCreate = () => {
    setEditingTask(null);
    setTaskTitle('');
    setTaskDescription('');
    setGoalId(goalsData?.content?.[0]?.id || '');
    setAssigneeId(activeUser?.id || users[0]?.id || '');
    setTaskPriority('MEDIUM');
    setTaskStatus('TODO');
    setEstimatedMinutes(60);
    setDueDate(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]);
    setIsCreateModalOpen(true);
  };

  const handleOpenEdit = (task: Task) => {
    setEditingTask(task);
    setTaskTitle(task.title);
    setTaskDescription(task.description || '');
    setGoalId(task.goalId);
    setAssigneeId(task.assigneeId);
    setTaskPriority(task.priority);
    setTaskStatus(task.status);
    setEstimatedMinutes(task.estimatedMinutes || 60);
    setDueDate(task.dueDate || new Date().toISOString().split('T')[0]);
    setIsCreateModalOpen(true);
  };

  const handleSubmitTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!goalId) {
      showToast('warning', 'Please select an associated goal for this task');
      return;
    }
    if (!assigneeId) {
      showToast('warning', 'Please select an assignee for this task');
      return;
    }
    if (!taskTitle.trim()) {
      showToast('warning', 'Task title is required');
      return;
    }

    try {
      if (editingTask) {
        await updateMutation.mutateAsync({
          id: editingTask.id,
          data: {
            title: taskTitle,
            description: taskDescription.trim() ? taskDescription : undefined,
            status: taskStatus,
            priority: taskPriority,
            estimatedMinutes: Number(estimatedMinutes) || undefined,
            dueDate,
            active: true,
          },
        });
        showToast('success', 'Task updated successfully');
      } else {
        await createMutation.mutateAsync({
          goalId,
          assigneeId,
          title: taskTitle,
          description: taskDescription.trim() ? taskDescription : undefined,
          status: taskStatus,
          priority: taskPriority,
          estimatedMinutes: Number(estimatedMinutes) || undefined,
          dueDate,
        });
        showToast('success', 'Task created successfully!');
      }
      setIsCreateModalOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleToggleComplete = async (task: Task) => {
    try {
      const isCompleted = task.status === 'COMPLETED';
      const newStatus: TaskStatus = isCompleted ? 'TODO' : 'COMPLETED';
      await updateMutation.mutateAsync({
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
      showToast('success', isCompleted ? 'Task moved to Todo' : 'Task completed! (+20 leaderboard score)');
    } catch (err: unknown) {
      showToast('error', 'Failed to update task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteTask = async () => {
    if (!deletingTaskId) return;
    try {
      await deleteMutation.mutateAsync(deletingTaskId);
      showToast('success', 'Task deleted successfully');
      setDeletingTaskId(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to delete task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const filteredTasks =
    tasksData?.content?.filter((t) => t.title.toLowerCase().includes(searchQuery.toLowerCase())) || [];

  const kanbanColumns: { status: TaskStatus; label: string; color: string }[] = [
    { status: 'TODO', label: 'To Do', color: 'border-slate-700 bg-slate-900/50' },
    { status: 'IN_PROGRESS', label: 'In Progress', color: 'border-primary-800/60 bg-primary-950/20' },
    { status: 'COMPLETED', label: 'Completed', color: 'border-emerald-800/60 bg-emerald-950/20' },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Header & View Toggle */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <CheckSquare className="w-5 h-5 text-emerald-400" />
            Task Management & Sprints
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Organize, prioritize, and complete development tasks tied to your team goals.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center p-1 bg-slate-900 border border-slate-800 rounded-xl">
            <button
              onClick={() => setViewMode('kanban')}
              className={`flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-medium transition-colors ${
                viewMode === 'kanban'
                  ? 'bg-slate-800 text-slate-100 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Kanban className="w-3.5 h-3.5" />
              Kanban
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-medium transition-colors ${
                viewMode === 'list'
                  ? 'bg-slate-800 text-slate-100 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <List className="w-3.5 h-3.5" />
              List
            </button>
          </div>

          <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />} onClick={handleOpenCreate}>
            New Task
          </Button>
        </div>
      </div>

      {/* 2. Search & Filters */}
      <Card className="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
          <Input
            placeholder="Search tasks..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />

          <Select
            value={selectedGoalFilter}
            onChange={(e) => setSelectedGoalFilter(e.target.value)}
            options={[
              { label: 'All Goals', value: 'ALL' },
              ...(goalsData?.content?.map((g) => ({ label: g.title, value: g.id })) || []),
            ]}
          />

          <Select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            options={[
              { label: 'All Statuses', value: 'ALL' },
              { label: 'To Do', value: 'TODO' },
              { label: 'In Progress', value: 'IN_PROGRESS' },
              { label: 'Completed', value: 'COMPLETED' },
              { label: 'Blocked', value: 'BLOCKED' },
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

      {/* 3. Main View: Kanban or List */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Skeleton className="h-64 w-full" />
          <Skeleton className="h-64 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      ) : viewMode === 'kanban' ? (
        /* KANBAN VIEW */
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {kanbanColumns.map((col) => {
            const colTasks = filteredTasks.filter((t) => t.status === col.status);
            return (
              <div key={col.status} className={`p-4 rounded-2xl border ${col.color} flex flex-col gap-3 min-h-[400px]`}>
                <div className="flex items-center justify-between pb-2 border-b border-slate-800">
                  <span className="text-xs font-bold text-slate-200">{col.label}</span>
                  <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 font-mono">
                    {colTasks.length}
                  </span>
                </div>

                <div className="space-y-3 flex-1">
                  {colTasks.map((task) => (
                    <Card key={task.id} hoverable className="p-3.5 space-y-3 bg-slate-900/90 border-slate-800">
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-start gap-2 min-w-0">
                          <button
                            onClick={() => handleToggleComplete(task)}
                            className={`mt-0.5 w-4 h-4 rounded border flex items-center justify-center transition-colors shrink-0 ${
                              task.status === 'COMPLETED'
                                ? 'bg-emerald-600 border-emerald-500 text-white'
                                : 'border-slate-700 hover:border-emerald-500 text-transparent'
                            }`}
                          >
                            <CheckCircle2 className="w-3.5 h-3.5" />
                          </button>
                          <span
                            className={`text-xs font-semibold ${
                              task.status === 'COMPLETED' ? 'line-through text-slate-500' : 'text-slate-100'
                            }`}
                          >
                            {task.title}
                          </span>
                        </div>

                        <Badge
                          size="sm"
                          variant={
                            task.priority === 'CRITICAL' || task.priority === 'HIGH'
                              ? 'danger'
                              : task.priority === 'MEDIUM'
                              ? 'warning'
                              : 'default'
                          }
                        >
                          {task.priority}
                        </Badge>
                      </div>

                      <p className="text-[11px] text-slate-400 truncate bg-slate-950/40 px-2 py-1 rounded-lg border border-slate-800/60">
                        🎯 {task.goalTitle}
                      </p>

                      <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1 border-t border-slate-800/60">
                        <span className="flex items-center gap-1">
                          <UserIcon className="w-3 h-3 text-slate-400" />
                          {task.assigneeName}
                        </span>

                        <div className="flex items-center gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleOpenEdit(task)}
                            title="Edit task"
                          >
                            <Edit2 className="w-3 h-3" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="text-rose-400"
                            onClick={() => setDeletingTaskId(task.id)}
                            title="Delete task"
                          >
                            <Trash2 className="w-3 h-3" />
                          </Button>
                        </div>
                      </div>
                    </Card>
                  ))}

                  {colTasks.length === 0 && (
                    <div className="flex flex-col items-center justify-center p-6 text-center text-slate-500 text-xs border border-dashed border-slate-800/80 rounded-xl">
                      No {col.label.toLowerCase()} tasks
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        /* LIST VIEW */
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <CardTitle>Tasks Roster</CardTitle>
              <span className="text-xs text-slate-500">{filteredTasks.length} tasks</span>
            </div>
          </CardHeader>
          <CardContent>
            {filteredTasks.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs text-slate-300">
                  <thead className="bg-slate-950/60 text-slate-400 uppercase text-[10px] font-semibold border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">Task</th>
                      <th className="px-4 py-3">Goal</th>
                      <th className="px-4 py-3">Assignee</th>
                      <th className="px-4 py-3">Priority</th>
                      <th className="px-4 py-3">Due Date</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/60">
                    {filteredTasks.map((t) => (
                      <tr key={t.id} className="hover:bg-slate-850/50 transition-colors">
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleToggleComplete(t)}
                              className={`w-4 h-4 rounded border flex items-center justify-center transition-colors shrink-0 ${
                                t.status === 'COMPLETED'
                                  ? 'bg-emerald-600 border-emerald-500 text-white'
                                  : 'border-slate-700 hover:border-emerald-500 text-transparent'
                              }`}
                            >
                              <CheckCircle2 className="w-3.5 h-3.5" />
                            </button>
                            <span
                              className={`font-medium ${
                                t.status === 'COMPLETED' ? 'line-through text-slate-500' : 'text-slate-100'
                              }`}
                            >
                              {t.title}
                            </span>
                          </div>
                        </td>
                        <td className="px-4 py-3 text-slate-400 max-w-xs truncate">{t.goalTitle}</td>
                        <td className="px-4 py-3 text-slate-300">{t.assigneeName}</td>
                        <td className="px-4 py-3">
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
                        <td className="px-4 py-3 text-slate-400">{formatDate(t.dueDate)}</td>
                        <td className="px-4 py-3">
                          <Badge size="sm" variant={t.status === 'COMPLETED' ? 'success' : 'primary'}>
                            {t.status}
                          </Badge>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <div className="flex items-center justify-end gap-1">
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleOpenEdit(t)}
                              title="Edit task"
                            >
                              <Edit2 className="w-3.5 h-3.5" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="text-rose-400"
                              onClick={() => setDeletingTaskId(t.id)}
                              title="Delete task"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <EmptyState
                icon={Filter}
                title="No tasks found"
                description="Create actionable development tasks to meet your learning goals."
                action={
                  <Button variant="primary" size="sm" onClick={handleOpenCreate}>
                    Create Task
                  </Button>
                }
              />
            )}

            {tasksData && tasksData.totalPages > 1 && (
              <Pagination
                currentPage={page}
                totalPages={tasksData.totalPages}
                totalElements={tasksData.totalElements}
                onPageChange={setPage}
              />
            )}
          </CardContent>
        </Card>
      )}

      {/* Create / Edit Task Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title={editingTask ? 'Edit Task' : 'Create New Task'}
        description="Attach this task to a learning goal and assign it to a team member."
      >
        <form onSubmit={handleSubmitTask} className="space-y-4">
          <Input
            label="Task Title *"
            placeholder="e.g. Implement DFS algorithm in Java with unit tests"
            value={taskTitle}
            onChange={(e) => setTaskTitle(e.target.value)}
            required
          />

          <Input
            label="Description (Optional)"
            placeholder="e.g. Practice 3 questions on LeetCode"
            value={taskDescription}
            onChange={(e) => setTaskDescription(e.target.value)}
          />

          {!editingTask && (
            <Select
              label="Associated Goal *"
              value={goalId}
              onChange={(e) => setGoalId(e.target.value)}
              options={
                goalsData?.content?.map((g) => ({ label: g.title, value: g.id })) || [
                  { label: 'No goals available', value: '' },
                ]
              }
              required
            />
          )}

          {!editingTask && (
            <Select
              label="Assignee *"
              value={assigneeId}
              onChange={(e) => setAssigneeId(e.target.value)}
              options={users.map((u) => ({ label: `${u.name} (${u.email})`, value: u.id }))}
              required
            />
          )}

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Priority"
              value={taskPriority}
              onChange={(e) => setTaskPriority(e.target.value as TaskPriority)}
              options={[
                { label: 'Medium', value: 'MEDIUM' },
                { label: 'High', value: 'HIGH' },
                { label: 'Critical', value: 'CRITICAL' },
                { label: 'Low', value: 'LOW' },
              ]}
            />

            <Select
              label="Status"
              value={taskStatus}
              onChange={(e) => setTaskStatus(e.target.value as TaskStatus)}
              options={[
                { label: 'To Do', value: 'TODO' },
                { label: 'In Progress', value: 'IN_PROGRESS' },
                { label: 'Completed', value: 'COMPLETED' },
                { label: 'Blocked', value: 'BLOCKED' },
              ]}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              type="number"
              label="Estimated Minutes"
              min={0}
              max={1440}
              value={estimatedMinutes}
              onChange={(e) => setEstimatedMinutes(Number(e.target.value))}
            />

            <Input
              type="date"
              label="Due Date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
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
              {editingTask ? 'Update Task' : 'Create Task'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      {deletingTaskId && (
        <Modal
          isOpen={!!deletingTaskId}
          onClose={() => setDeletingTaskId(null)}
          title="Delete Task"
          description="Are you sure you want to delete this task? This will remove it from the active backlog."
        >
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" size="sm" onClick={() => setDeletingTaskId(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              size="sm"
              isLoading={deleteMutation.isPending}
              onClick={handleDeleteTask}
            >
              Delete Task
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
};
