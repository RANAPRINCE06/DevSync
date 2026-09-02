import React, { useState } from 'react';
import { Clock, Plus } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
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
import {
  useReminders,
  useCreateReminder,
  useUpdateReminder,
  useDeleteReminder,
} from '@/hooks/useReminders';
import { ReminderType } from '@/types/reminder';

export const RemindersPage: React.FC = () => {
  const { user } = useAuth();
  const { activeTeam } = useApp();
  const { showToast } = useToast();

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // Form State
  const [type, setType] = useState<ReminderType>('DAILY_PROGRESS');
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [reminderTime, setReminderTime] = useState('18:00');
  const [timezone, setTimezone] = useState('UTC');
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState(
    new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );

  const { data: remindersPage, isLoading } = useReminders({
    userId: user?.id,
    teamId: activeTeam?.id,
    size: 1000,
  });

  const createReminderMutation = useCreateReminder();
  const updateReminderMutation = useUpdateReminder();
  const deleteReminderMutation = useDeleteReminder();

  const allReminders = remindersPage?.content || [];

  const handleCreateReminder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !activeTeam) {
      showToast('error', 'User or team not selected');
      return;
    }

    try {
      const formattedTime = reminderTime.length === 5 ? `${reminderTime}:00` : reminderTime;
      await createReminderMutation.mutateAsync({
        userId: user.id,
        teamId: activeTeam.id,
        type,
        title: title.trim(),
        message: message.trim() || undefined,
        reminderTime: formattedTime,
        timezone,
        startDate,
        endDate,
      });
      showToast('success', 'Scheduled reminder created successfully!');
      setIsCreateModalOpen(false);
      setTitle('');
      setMessage('');
    } catch (err: unknown) {
      showToast('error', 'Failed to create reminder', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleToggleActive = async (id: string, currentActive: boolean) => {
    try {
      await updateReminderMutation.mutateAsync({
        id,
        data: { active: !currentActive },
      });
      showToast('success', `Reminder ${currentActive ? 'paused' : 'resumed'}`);
    } catch {
      showToast('error', 'Failed to toggle reminder status');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this reminder?')) return;
    try {
      await deleteReminderMutation.mutateAsync(id);
      showToast('success', 'Reminder removed');
    } catch {
      showToast('error', 'Failed to delete reminder');
    }
  };

  return (
    <div className="space-y-4">
      {/* 1. Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Clock className="w-4 h-4 text-blue-600" />
            Scheduled Accountability Reminders
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Automate daily standup prompts, goal check-ins, and task due date alerts
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-3.5 h-3.5" />}
          onClick={() => setIsCreateModalOpen(true)}
        >
          Create Reminder
        </Button>
      </div>

      {/* 2. Reminders Table */}
      <Card>
        <CardHeader>
          <CardTitle>Configured Schedule</CardTitle>
          <span className="text-[11px] text-slate-500">{allReminders.length} Active Schedules</span>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : allReminders.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="classic-table">
                <thead>
                  <tr>
                    <th style={{ width: '40px' }}>#</th>
                    <th>Type</th>
                    <th>Title & Description</th>
                    <th>Scheduled Time</th>
                    <th>Timezone</th>
                    <th>Date Range</th>
                    <th>Status</th>
                    <th className="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {allReminders.map((r, idx) => (
                    <tr key={r.id}>
                      <td className="text-slate-400 font-mono text-[11px]">{idx + 1}</td>
                      <td>
                        <Badge size="sm" variant="primary">
                          {r.type}
                        </Badge>
                      </td>
                      <td>
                        <div className="font-bold text-slate-900 dark:text-slate-100">{r.title}</div>
                        {r.message && <div className="text-[11px] text-slate-500 line-clamp-1">{r.message}</div>}
                      </td>
                      <td className="font-mono font-bold text-blue-700 dark:text-blue-400">{r.reminderTime}</td>
                      <td className="text-slate-600 dark:text-slate-400">{r.timezone}</td>
                      <td className="text-[11px] text-slate-500">
                        {r.startDate} → {r.endDate}
                      </td>
                      <td>
                        <Badge size="sm" variant={r.active ? 'success' : 'danger'}>
                          {r.active ? 'Active' : 'Paused'}
                        </Badge>
                      </td>
                      <td className="text-right whitespace-nowrap">
                        <button
                          onClick={() => handleToggleActive(r.id, r.active)}
                          className="px-2 py-0.5 text-[11px] bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-[2px] font-semibold text-slate-700 dark:text-slate-200 mr-1"
                        >
                          {r.active ? 'Pause' : 'Resume'}
                        </button>
                        <button
                          onClick={() => handleDelete(r.id)}
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
                icon={Clock}
                title="No reminders scheduled"
                description="Set up daily notifications to remind you to log progress and review active goals."
                action={
                  <Button variant="primary" size="sm" onClick={() => setIsCreateModalOpen(true)}>
                    Create Reminder
                  </Button>
                }
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create Reminder Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create Scheduled Reminder"
        description="Configure automated notifications for your team workflow"
      >
        <form onSubmit={handleCreateReminder} className="space-y-3">
          <Select
            label="Reminder Type *"
            value={type}
            onChange={(e) => setType(e.target.value as ReminderType)}
            options={[
              { label: 'Daily Progress Submission', value: 'DAILY_PROGRESS' },
              { label: 'Goal Milestone Review', value: 'GOAL_REVIEW' },
              { label: 'Task Due Reminder', value: 'TASK_DUE' },
              { label: 'Custom Alert', value: 'CUSTOM' },
            ]}
          />

          <Input
            label="Title *"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Evening Standup Check-in"
            required
          />

          <Input
            label="Message (Optional)"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="e.g. Please log today's focus hours and completed tasks"
          />

          <div className="grid grid-cols-2 gap-2">
            <Input
              label="Reminder Time *"
              type="time"
              value={reminderTime}
              onChange={(e) => setReminderTime(e.target.value)}
              required
            />
            <Input
              label="Timezone *"
              value={timezone}
              onChange={(e) => setTimezone(e.target.value)}
              placeholder="e.g. UTC, Asia/Kolkata"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <Input
              label="Start Date *"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
            />
            <Input
              label="End Date *"
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createReminderMutation.isPending}>
              Schedule Reminder
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
