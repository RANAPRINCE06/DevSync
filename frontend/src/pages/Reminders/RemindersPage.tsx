import React, { useState, useMemo } from 'react';
import {
  AlarmClock,
  Plus,
  Search,
  Calendar,
  Clock,
  Edit2,
  Trash2,
  PauseCircle,
  PlayCircle,
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
import { ErrorState } from '@/components/ui/ErrorState';
import {
  useReminders,
  useCreateReminder,
  useUpdateReminder,
  useDeleteReminder,
} from '@/hooks/useReminders';
import { formatDate } from '@/lib/utils';
import { Reminder, ReminderType, ReminderStatus } from '@/types/reminder';

export const RemindersPage: React.FC = () => {
  const { activeUser, activeTeam } = useApp();
  const { showToast } = useToast();

  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Modals
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingReminder, setEditingReminder] = useState<Reminder | null>(null);
  const [deletingReminderId, setDeletingReminderId] = useState<string | null>(null);

  // Form states
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [type, setType] = useState<ReminderType>('DAILY_PROGRESS');
  const [reminderTime, setReminderTime] = useState('09:00:00');
  const [timezone, setTimezone] = useState(activeUser?.timezone || 'UTC');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState('');

  // Queries
  const {
    data: remindersPage,
    isLoading,
    isError,
    refetch,
  } = useReminders({
    userId: activeUser?.id,
    teamId: activeTeam?.id,
    size: 50,
  });

  // Mutations
  const createReminderMutation = useCreateReminder();
  const updateReminderMutation = useUpdateReminder();
  const deleteReminderMutation = useDeleteReminder();

  const allReminders = remindersPage?.content || [];

  const filteredReminders = useMemo(() => {
    return allReminders.filter((r) => {
      const matchesSearch =
        r.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (r.message && r.message.toLowerCase().includes(searchQuery.toLowerCase()));

      const matchesType = typeFilter === 'ALL' || r.type === typeFilter;
      const matchesStatus = statusFilter === 'ALL' || r.status === statusFilter;
      return matchesSearch && matchesType && matchesStatus;
    });
  }, [allReminders, searchQuery, typeFilter, statusFilter]);

  const handleOpenCreate = () => {
    setEditingReminder(null);
    setTitle('');
    setMessage('');
    setType('DAILY_PROGRESS');
    setReminderTime('09:00:00');
    setTimezone(activeUser?.timezone || 'UTC');
    setStartDate(new Date().toISOString().split('T')[0]);
    setEndDate('');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (r: Reminder) => {
    setEditingReminder(r);
    setTitle(r.title);
    setMessage(r.message || '');
    setType(r.type);
    setReminderTime(r.reminderTime);
    setTimezone(r.timezone);
    setStartDate(r.startDate);
    setEndDate(r.endDate || '');
    setIsModalOpen(true);
  };

  const handleSaveReminder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      showToast('warning', 'Reminder title is required');
      return;
    }
    if (!activeUser || !activeTeam) {
      showToast('error', 'Active user and team required');
      return;
    }

    try {
      // Ensure reminderTime formatted as HH:mm:ss
      const formattedTime = reminderTime.length === 5 ? `${reminderTime}:00` : reminderTime;

      if (editingReminder) {
        await updateReminderMutation.mutateAsync({
          id: editingReminder.id,
          data: {
            title: title.trim(),
            message: message.trim() ? message : undefined,
            reminderTime: formattedTime,
            timezone,
            startDate,
            endDate: endDate || undefined,
          },
        });
        showToast('success', 'Reminder schedule updated successfully!');
      } else {
        await createReminderMutation.mutateAsync({
          userId: activeUser.id,
          teamId: activeTeam.id,
          title: title.trim(),
          message: message.trim() ? message : undefined,
          type,
          reminderTime: formattedTime,
          timezone,
          startDate,
          endDate: endDate || undefined,
        });
        showToast('success', 'Scheduled reminder created successfully!');
      }
      setIsModalOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save reminder', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleToggleStatus = async (r: Reminder) => {
    const nextStatus: ReminderStatus = r.status === 'ACTIVE' ? 'PAUSED' : 'ACTIVE';
    try {
      await updateReminderMutation.mutateAsync({
        id: r.id,
        data: {
          status: nextStatus,
          active: nextStatus === 'ACTIVE',
        },
      });
      showToast('info', `Reminder ${nextStatus === 'ACTIVE' ? 'resumed' : 'paused'}`);
    } catch (err: unknown) {
      showToast('error', 'Failed to update status', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteReminder = async () => {
    if (!deletingReminderId) return;
    try {
      await deleteReminderMutation.mutateAsync(deletingReminderId);
      showToast('info', 'Reminder schedule deactivated');
      setDeletingReminderId(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to delete reminder', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <AlarmClock className="w-5 h-5 text-amber-400" />
            Scheduled Reminders & Check-ins
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Configure automated daily study prompts, goal deadlines, and task alerts.
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={handleOpenCreate}
        >
          Create Reminder
        </Button>
      </div>

      {/* 2. Filter Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 p-3 rounded-2xl bg-slate-900/80 border border-slate-800">
        <div className="flex flex-wrap items-center gap-2 flex-1 max-w-xl">
          <div className="relative min-w-[200px] flex-1">
            <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search reminder title or message..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
            />
          </div>

          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="h-8 px-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
          >
            <option value="ALL">All Types</option>
            <option value="DAILY_PROGRESS">Daily Progress</option>
            <option value="GOAL_CHECKIN">Goal Check-in</option>
            <option value="TASK_DUE">Task Due</option>
            <option value="CUSTOM">Custom</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-8 px-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="PAUSED">Paused</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      {/* 3. Reminder List */}
      {isLoading ? (
        <div className="space-y-3">
          <Skeleton className="h-20 rounded-2xl" />
          <Skeleton className="h-20 rounded-2xl" />
        </div>
      ) : isError ? (
        <ErrorState
          title="Failed to load scheduled reminders"
          description="Could not connect to the backend server. Please try again."
          onRetry={() => refetch()}
        />
      ) : filteredReminders.length === 0 ? (
        <EmptyState
          icon={AlarmClock}
          title="No scheduled reminders"
          description="Never miss a study milestone or team sync by scheduling daily reminders."
          action={
            <Button variant="primary" size="sm" onClick={handleOpenCreate}>
              Create Reminder
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredReminders.map((r) => {
            const isActive = r.status === 'ACTIVE' && r.active;
            return (
              <Card
                key={r.id}
                hoverable
                className={`p-4 flex flex-col justify-between space-y-3 transition-all ${
                  isActive ? 'border-slate-800' : 'opacity-70 border-dashed border-slate-800'
                }`}
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-slate-100">{r.title}</span>
                      <Badge
                        size="sm"
                        variant={
                          r.type === 'DAILY_PROGRESS'
                            ? 'primary'
                            : r.type === 'GOAL_CHECKIN'
                            ? 'warning'
                            : 'default'
                        }
                      >
                        {r.type.replace('_', ' ')}
                      </Badge>
                    </div>

                    <button
                      onClick={() => handleToggleStatus(r)}
                      className={`p-1 rounded-lg transition-colors ${
                        isActive
                          ? 'text-emerald-400 hover:bg-emerald-950/30'
                          : 'text-slate-500 hover:text-emerald-400'
                      }`}
                      title={isActive ? 'Pause reminder' : 'Resume reminder'}
                    >
                      {isActive ? (
                        <PlayCircle className="w-4 h-4" />
                      ) : (
                        <PauseCircle className="w-4 h-4" />
                      )}
                    </button>
                  </div>

                  {r.message && <p className="text-xs text-slate-400 mt-1">{r.message}</p>}
                </div>

                <div className="space-y-1.5 pt-2 border-t border-slate-850 text-xs text-slate-400">
                  <div className="flex items-center justify-between">
                    <span className="flex items-center gap-1 font-mono text-primary-300">
                      <Clock className="w-3.5 h-3.5" />
                      {r.reminderTime.substring(0, 5)} {r.timezone}
                    </span>
                    <span className="flex items-center gap-1 text-[11px] text-slate-500">
                      <Calendar className="w-3 h-3" />
                      Since {formatDate(r.startDate)}
                    </span>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-slate-850">
                  <Badge size="sm" variant={isActive ? 'success' : 'default'}>
                    {r.status}
                  </Badge>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleOpenEdit(r)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-slate-100 hover:bg-slate-800 transition-colors"
                      title="Edit reminder"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => setDeletingReminderId(r.id)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-950/30 transition-colors"
                      title="Deactivate reminder"
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

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingReminder ? 'Edit Reminder Schedule' : 'Create Scheduled Reminder'}
        description="Set recurring alerts for daily progress, goals, and tasks."
      >
        <form onSubmit={handleSaveReminder} className="space-y-4">
          <Input
            label="Reminder Title *"
            placeholder="e.g. Evening DSA & System Design Check-in"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />

          <Input
            label="Message (Optional)"
            placeholder="e.g. Don't forget to record today's focus hours and solved problems"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Select
              label="Reminder Type"
              value={type}
              onChange={(e) => setType(e.target.value as ReminderType)}
              options={[
                { label: 'Daily Progress Prompt', value: 'DAILY_PROGRESS' },
                { label: 'Goal Check-in', value: 'GOAL_CHECKIN' },
                { label: 'Task Due Alert', value: 'TASK_DUE' },
                { label: 'Custom Alert', value: 'CUSTOM' },
              ]}
            />

            <Input
              type="time"
              label="Reminder Time (HH:mm) *"
              value={reminderTime.substring(0, 5)}
              onChange={(e) => setReminderTime(e.target.value)}
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Input
              label="Timezone *"
              value={timezone}
              onChange={(e) => setTimezone(e.target.value)}
              placeholder="e.g. UTC, Asia/Kolkata, America/New_York"
              required
            />

            <Input
              type="date"
              label="Start Date *"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
            />
          </div>

          <Input
            type="date"
            label="End Date (Optional)"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
          />

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={createReminderMutation.isPending || updateReminderMutation.isPending}
            >
              {editingReminder ? 'Update Reminder' : 'Create Reminder'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      {deletingReminderId && (
        <Modal
          isOpen={!!deletingReminderId}
          onClose={() => setDeletingReminderId(null)}
          title="Deactivate Reminder"
          description="Are you sure you want to deactivate this scheduled reminder?"
        >
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" size="sm" onClick={() => setDeletingReminderId(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              size="sm"
              onClick={handleDeleteReminder}
              isLoading={deleteReminderMutation.isPending}
            >
              Confirm Deactivation
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
};
