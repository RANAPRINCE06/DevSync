import React, { useState } from 'react';
import {
  CheckSquare,
  Plus,
  Search,
  Kanban,
  List as ListIcon,
} from 'lucide-react';
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
import { useTasks, useCreateTask, useUpdateTask, useDeleteTask } from '@/hooks/useTasks';
import { useGoals } from '@/hooks/useGoals';
import { formatDate } from '@/lib/utils';
import { filterRealUsers } from '@/lib/userFilter';
import { Task, TaskPriority, TaskStatus } from '@/types/task';

export const TasksPage: React.FC = () => {
  const { activeTeam, users } = useApp();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [viewMode, setViewMode] = useState<'KANBAN' | 'LIST'>('LIST');
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [goalFilter, setGoalFilter] = useState<string>('ALL');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // Form State
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [goalId, setGoalId] = useState('');
  const [assigneeId, setAssigneeId] = useState('');
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM');
  const [estimatedMinutes, setEstimatedMinutes] = useState('60');
  const [dueDate, setDueDate] = useState(
    new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );

  const { data: tasksPage, isLoading } = useTasks({ teamId: activeTeam?.id, active: true, size: 1000 });
  const { data: goalsPage } = useGoals({ teamId: activeTeam?.id, active: true, size: 1000 });

  const createTaskMutation = useCreateTask();
  const updateTaskMutation = useUpdateTask();
  const deleteTaskMutation = useDeleteTask();

  const allTasks = tasksPage?.content || [];
  const allGoals = goalsPage?.content || [];
  const realUsers = filterRealUsers(users);

  const filteredTasks = allTasks.filter((t) => {
    const matchesSearch =
      t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (t.description || '').toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || t.status === statusFilter;
    const matchesGoal = goalFilter === 'ALL' || t.goalId === goalFilter;
    return matchesSearch && matchesStatus && matchesGoal;
  });

  const handleCreateTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!goalId) {
      showToast('warning', 'Please select an associated goal');
      return;
    }
    const finalAssigneeId = assigneeId || user?.id;
    if (!finalAssigneeId) {
      showToast('warning', 'Please select an assignee');
      return;
    }

    try {
      await createTaskMutation.mutateAsync({
        goalId,
        assigneeId: finalAssigneeId,
        title: title.trim(),
        description: description.trim() || undefined,
        priority,
        estimatedMinutes: parseInt(estimatedMinutes, 10) || 60,
        dueDate,
      });
      showToast('success', 'Sprint task created successfully!');
      setIsCreateModalOpen(false);
      setTitle('');
      setDescription('');
    } catch (err: unknown) {
      showToast('error', 'Failed to create task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleUpdateStatus = async (task: Task, newStatus: TaskStatus) => {
    try {
      await updateTaskMutation.mutateAsync({
        id: task.id,
        data: {
          title: task.title,
          description: task.description || undefined,
          priority: task.priority,
          estimatedMinutes: task.estimatedMinutes || undefined,
          dueDate: task.dueDate || undefined,
          status: newStatus,
        },
      });
      showToast('success', `Task status updated to ${newStatus}`);
    } catch (err: unknown) {
      showToast('error', 'Failed to update task status', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteTask = async (id: string) => {
    if (!confirm('Are you sure you want to delete this task?')) return;
    try {
      await deleteTaskMutation.mutateAsync(id);
      showToast('success', 'Task removed');
    } catch (err: unknown) {
      showToast('error', 'Failed to delete task', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const kanbanColumns: { status: TaskStatus; title: string }[] = [
    { status: 'TODO', title: 'To Do' },
    { status: 'IN_PROGRESS', title: 'In Progress' },
    { status: 'COMPLETED', title: 'Completed' },
    { status: 'BLOCKED', title: 'Blocked' },
  ];

  return (
    <div className="space-y-4">
      {/* 1. Tasks Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <CheckSquare className="w-4 h-4 text-emerald-600" />
            Sprint Tasks & Kanban Board
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Manage actionable sprint tasks, track assignees, and update deliverables
          </p>
        </div>

        <div className="flex items-center gap-2">
          {/* View Toggle */}
          <div className="flex border border-[#cbd5e1] dark:border-slate-700 rounded-[2px] overflow-hidden">
            <button
              onClick={() => setViewMode('LIST')}
              className={`px-2.5 py-1 text-xs font-semibold flex items-center gap-1 ${
                viewMode === 'LIST'
                  ? 'bg-blue-600 text-white'
                  : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300'
              }`}
            >
              <ListIcon className="w-3.5 h-3.5" /> List
            </button>
            <button
              onClick={() => setViewMode('KANBAN')}
              className={`px-2.5 py-1 text-xs font-semibold flex items-center gap-1 ${
                viewMode === 'KANBAN'
                  ? 'bg-blue-600 text-white'
                  : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300'
              }`}
            >
              <Kanban className="w-3.5 h-3.5" /> Kanban
            </button>
          </div>

          <Button
            variant="primary"
            size="sm"
            leftIcon={<Plus className="w-3.5 h-3.5" />}
            onClick={() => {
              if (allGoals.length === 0) {
                showToast('warning', 'Please create a goal before creating tasks.');
              }
              setGoalId(allGoals[0]?.id || '');
              setAssigneeId(user?.id || realUsers[0]?.id || '');
              setIsCreateModalOpen(true);
            }}
          >
            Create Task
          </Button>
        </div>
      </div>

      {/* 2. Filter Bar */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-2.5 rounded-[3px] flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2 flex-1 max-w-lg">
          <div className="relative flex-1">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search tasks..."
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
            <option value="TODO">To Do</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="BLOCKED">Blocked</option>
          </select>

          <select
            value={goalFilter}
            onChange={(e) => setGoalFilter(e.target.value)}
            className="px-2 py-1 text-xs border border-[#cbd5e1] dark:border-slate-700 rounded-[2px] bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 max-w-[150px] truncate"
          >
            <option value="ALL">All Goals</option>
            {allGoals.map((g) => (
              <option key={g.id} value={g.id}>
                {g.title}
              </option>
            ))}
          </select>
        </div>

        <span className="text-[11px] text-slate-500 font-semibold">
          Showing {filteredTasks.length} of {allTasks.length} Tasks
        </span>
      </div>

      {/* 3. Task Views */}
      {viewMode === 'LIST' ? (
        <Card>
          <CardHeader>
            <CardTitle>Sprint Task Inventory</CardTitle>
            <span className="text-[11px] text-slate-500">{filteredTasks.length} Tasks Listed</span>
          </CardHeader>
          <CardContent className="p-0">
            {isLoading ? (
              <div className="p-4 space-y-2">
                <Skeleton className="h-8 w-full" />
                <Skeleton className="h-8 w-full" />
              </div>
            ) : filteredTasks.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="classic-table">
                  <thead>
                    <tr>
                      <th style={{ width: '40px' }}>#</th>
                      <th>Task Title</th>
                      <th>Goal Milestone</th>
                      <th>Assignee</th>
                      <th>Priority</th>
                      <th>Status</th>
                      <th>Est. Time</th>
                      <th>Due Date</th>
                      <th className="text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredTasks.map((t, idx) => (
                      <tr key={t.id}>
                        <td className="text-slate-400 font-mono text-[11px]">{idx + 1}</td>
                        <td>
                          <div className="font-bold text-slate-900 dark:text-slate-100">{t.title}</div>
                          {t.description && <div className="text-[11px] text-slate-500 line-clamp-1">{t.description}</div>}
                        </td>
                        <td className="text-slate-600 dark:text-slate-400 max-w-[140px] truncate">{t.goalTitle}</td>
                        <td className="text-slate-600 dark:text-slate-400 font-semibold">{t.assigneeName}</td>
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
                          <Badge
                            size="sm"
                            variant={
                              t.status === 'COMPLETED'
                                ? 'success'
                                : t.status === 'IN_PROGRESS'
                                ? 'primary'
                                : t.status === 'BLOCKED'
                                ? 'danger'
                                : 'default'
                            }
                          >
                            {t.status}
                          </Badge>
                        </td>
                        <td className="text-slate-500 text-[11px]">{t.estimatedMinutes}m</td>
                        <td className="text-slate-500 text-[11px]">{formatDate(t.dueDate)}</td>
                        <td className="text-right whitespace-nowrap">
                          {t.status !== 'COMPLETED' && (
                            <button
                              onClick={() => handleUpdateStatus(t, 'COMPLETED')}
                              className="px-2 py-0.5 text-[11px] bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border border-emerald-300 rounded-[2px] font-semibold mr-1"
                            >
                              Complete
                            </button>
                          )}
                          <button
                            onClick={() => handleDeleteTask(t.id)}
                            className="px-2 py-0.5 text-[11px] bg-red-50 hover:bg-red-100 text-red-700 border border-red-300 rounded-[2px] font-semibold"
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="p-6">
                <EmptyState
                  icon={CheckSquare}
                  title="No tasks found"
                  description="Create actionable sprint tasks associated with your learning goals."
                  action={
                    <Button variant="primary" size="sm" onClick={() => setIsCreateModalOpen(true)}>
                      Create Task
                    </Button>
                  }
                />
              </div>
            )}
          </CardContent>
        </Card>
      ) : (
        /* Classic Kanban View */
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {kanbanColumns.map((col) => {
            const colTasks = filteredTasks.filter((t) => t.status === col.status);
            return (
              <div key={col.status} className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 rounded-[3px] flex flex-col h-full shadow-xs">
                <div className="classic-header-bar flex items-center justify-between">
                  <span>{col.title}</span>
                  <Badge size="sm" variant="default">
                    {colTasks.length}
                  </Badge>
                </div>
                <div className="p-2 space-y-2 flex-1 min-h-[300px] bg-[#f8fafc] dark:bg-slate-900/60">
                  {colTasks.map((t) => (
                    <div
                      key={t.id}
                      className="p-2.5 bg-white dark:bg-slate-800 border border-[#cfd5dc] dark:border-slate-700 rounded-[2px] shadow-2xs space-y-2"
                    >
                      <div className="flex items-start justify-between gap-1">
                        <span className="font-bold text-xs text-slate-900 dark:text-slate-100">{t.title}</span>
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
                      </div>

                      {t.description && <p className="text-[11px] text-slate-500 line-clamp-2">{t.description}</p>}

                      <div className="text-[10px] text-slate-400 flex items-center justify-between pt-1 border-t border-slate-100 dark:border-slate-700">
                        <span>{t.assigneeName}</span>
                        <span>{t.estimatedMinutes}m</span>
                      </div>

                      <div className="flex items-center justify-between pt-1">
                        <select
                          value={t.status}
                          onChange={(e) => handleUpdateStatus(t, e.target.value as TaskStatus)}
                          className="text-[10px] py-0.5 px-1 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-[2px]"
                        >
                          <option value="TODO">To Do</option>
                          <option value="IN_PROGRESS">In Progress</option>
                          <option value="COMPLETED">Completed</option>
                          <option value="BLOCKED">Blocked</option>
                        </select>
                        <button
                          onClick={() => handleDeleteTask(t.id)}
                          className="text-[10px] text-red-600 hover:underline font-semibold"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}

                  {colTasks.length === 0 && (
                    <p className="text-[11px] text-slate-400 italic text-center py-6">No tasks</p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create Task Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create Sprint Task"
        description="Add a task under an existing goal milestone"
      >
        <form onSubmit={handleCreateTask} className="space-y-3">
          <Input
            label="Task Title *"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Implement Raft leader election"
            required
          />
          <Input
            label="Description (Optional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Handle heartbeat timeouts and term increments"
          />

          <Select
            label="Associated Goal *"
            value={goalId}
            onChange={(e) => setGoalId(e.target.value)}
            options={allGoals.map((g) => ({
              label: g.title,
              value: g.id,
            }))}
            required
          />

          <div className="grid grid-cols-2 gap-2">
            <Select
              label="Assignee *"
              value={assigneeId}
              onChange={(e) => setAssigneeId(e.target.value)}
              options={realUsers.map((u) => ({
                label: `${u.name} (${u.email})`,
                value: u.id,
              }))}
              required
            />
            <Select
              label="Priority *"
              value={priority}
              onChange={(e) => setPriority(e.target.value as TaskPriority)}
              options={[
                { label: 'Low', value: 'LOW' },
                { label: 'Medium', value: 'MEDIUM' },
                { label: 'High', value: 'HIGH' },
                { label: 'Critical', value: 'CRITICAL' },
              ]}
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <Input
              label="Estimated Time (Mins) *"
              type="number"
              min="5"
              max="1440"
              value={estimatedMinutes}
              onChange={(e) => setEstimatedMinutes(e.target.value)}
              required
            />
            <Input
              label="Due Date *"
              type="date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createTaskMutation.isPending}>
              Create Task
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
