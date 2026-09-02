import React, { useState } from 'react';
import {
  Calendar,
  Plus,
  Search,
  Clock,
  CheckCircle,
  FileText,
} from 'lucide-react';
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
import { useProgressList, useCreateProgress } from '@/hooks/useProgress';
import { DailyProgress, ProgressStatus } from '@/types/progress';

export const ProgressPage: React.FC = () => {
  const { user } = useAuth();
  const { activeTeam } = useApp();
  const { showToast } = useToast();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [isLogModalOpen, setIsLogModalOpen] = useState(false);
  const [viewingProgress, setViewingProgress] = useState<DailyProgress | null>(null);

  // Form State
  const [progressDate, setProgressDate] = useState(new Date().toISOString().split('T')[0]);
  const [whatStudied, setWhatStudied] = useState('');
  const [completed, setCompleted] = useState('');
  const [challenges, setChallenges] = useState('');
  const [studyMinutes, setStudyMinutes] = useState('60');
  const [status, setStatus] = useState<ProgressStatus>('COMPLETED');

  const { data: progressPage, isLoading } = useProgressList({
    userId: user?.id,
    teamId: activeTeam?.id,
    size: 1000,
  });

  const createProgressMutation = useCreateProgress();

  const allLogs = progressPage?.content || [];

  const filteredLogs = allLogs.filter((p: DailyProgress) => {
    const matchesSearch =
      p.whatStudied.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (p.completed || '').toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || p.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const totalMinutes = allLogs.reduce((acc: number, p: DailyProgress) => acc + p.studyMinutes, 0);
  const totalHours = (totalMinutes / 60).toFixed(1);
  const completedLogsCount = allLogs.filter((p: DailyProgress) => p.status === 'COMPLETED').length;

  const handleCreateProgress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !activeTeam) {
      showToast('error', 'User or team not selected');
      return;
    }

    try {
      await createProgressMutation.mutateAsync({
        userId: user.id,
        teamId: activeTeam.id,
        progressDate,
        whatStudied: whatStudied.trim(),
        completed: completed.trim() || whatStudied.trim(),
        challenges: challenges.trim() || undefined,
        studyMinutes: parseInt(studyMinutes, 10) || 60,
        status,
      });
      showToast('success', 'Daily progress record logged successfully!');
      setIsLogModalOpen(false);
      setWhatStudied('');
      setCompleted('');
      setChallenges('');
    } catch (err: unknown) {
      showToast('error', 'Failed to log progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  return (
    <div className="space-y-4">
      {/* 1. Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Calendar className="w-4 h-4 text-blue-600" />
            Daily Progress & Accountability Records
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Structured daily engineering work logs, study tracking, and blockers
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-3.5 h-3.5" />}
          onClick={() => setIsLogModalOpen(true)}
        >
          Log Daily Progress
        </Button>
      </div>

      {/* 2. Overview Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase text-slate-500">Total Focus Time</span>
            <Clock className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">
            {totalHours} <span className="text-xs font-normal text-slate-400">hours</span>
          </div>
          <span className="text-[10px] text-slate-400">{totalMinutes} total minutes logged</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase text-slate-500">Total Entries</span>
            <FileText className="w-4 h-4 text-indigo-600" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">{allLogs.length}</div>
          <span className="text-[10px] text-slate-400">Daily accountability entries</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3 rounded-[3px]">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-bold uppercase text-slate-500">Completed Sessions</span>
            <CheckCircle className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">{completedLogsCount}</div>
          <span className="text-[10px] text-slate-400">Full sessions verified</span>
        </div>
      </div>

      {/* 3. Filter Bar */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-2.5 rounded-[3px] flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2 flex-1 max-w-md">
          <div className="relative flex-1">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search progress logs by topics or deliverables..."
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
            <option value="COMPLETED">Completed</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="PARTIAL">Partial</option>
          </select>
        </div>

        <span className="text-[11px] text-slate-500 font-semibold">
          Showing {filteredLogs.length} of {allLogs.length} Entries
        </span>
      </div>

      {/* 4. Classic Data Table: Progress Log */}
      <Card>
        <CardHeader>
          <CardTitle>Work Log History</CardTitle>
          <span className="text-[11px] text-slate-500">{filteredLogs.length} Logged Sessions</span>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : filteredLogs.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="classic-table">
                <thead>
                  <tr>
                    <th style={{ width: '40px' }}>#</th>
                    <th>Date</th>
                    <th>Topics Studied / Work Done</th>
                    <th>Deliverables Completed</th>
                    <th>Focus Time</th>
                    <th>Status</th>
                    <th className="text-right">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredLogs.map((p: DailyProgress, idx: number) => (
                    <tr key={p.id}>
                      <td className="text-slate-400 font-mono text-[11px]">{idx + 1}</td>
                      <td className="font-bold text-slate-900 dark:text-slate-100 whitespace-nowrap">
                        {p.progressDate}
                      </td>
                      <td>
                        <div className="font-semibold text-slate-900 dark:text-slate-100 line-clamp-2">
                          {p.whatStudied}
                        </div>
                      </td>
                      <td className="text-slate-600 dark:text-slate-400">
                        {p.completed || p.whatCompleted || '—'}
                      </td>
                      <td className="text-slate-900 dark:text-slate-100 font-bold whitespace-nowrap">
                        {p.studyMinutes}m
                      </td>
                      <td>
                        <Badge
                          size="sm"
                          variant={
                            p.status === 'COMPLETED'
                              ? 'success'
                              : p.status === 'IN_PROGRESS'
                              ? 'primary'
                              : 'warning'
                          }
                        >
                          {p.status}
                        </Badge>
                      </td>
                      <td className="text-right">
                        <button
                          onClick={() => setViewingProgress(p)}
                          className="px-2 py-0.5 text-[11px] bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-[2px] font-semibold"
                        >
                          View Details
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
                icon={Calendar}
                title="No daily progress logged"
                description="Record what you studied or built today to keep your streak and score active."
                action={
                  <Button variant="primary" size="sm" onClick={() => setIsLogModalOpen(true)}>
                    Log Daily Progress
                  </Button>
                }
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create Log Modal */}
      <Modal
        isOpen={isLogModalOpen}
        onClose={() => setIsLogModalOpen(false)}
        title="Log Daily Progress"
        description="Record your focus topics and completed deliverables"
      >
        <form onSubmit={handleCreateProgress} className="space-y-3">
          <div className="grid grid-cols-2 gap-2">
            <Input
              label="Date *"
              type="date"
              value={progressDate}
              onChange={(e) => setProgressDate(e.target.value)}
              required
            />
            <Input
              label="Focus Time (Minutes) *"
              type="number"
              min="5"
              max="1440"
              value={studyMinutes}
              onChange={(e) => setStudyMinutes(e.target.value)}
              required
            />
          </div>

          <Input
            label="What did you study / build today? *"
            value={whatStudied}
            onChange={(e) => setWhatStudied(e.target.value)}
            placeholder="e.g. Implemented JWT filter & tested database indexes"
            required
          />

          <Input
            label="Key Deliverables Completed"
            value={completed}
            onChange={(e) => setCompleted(e.target.value)}
            placeholder="e.g. Unit tests 100% passing"
          />

          <Input
            label="Blockers / Challenges (Optional)"
            value={challenges}
            onChange={(e) => setChallenges(e.target.value)}
            placeholder="e.g. PostgreSQL index scan vs seq scan optimization"
          />

          <Select
            label="Session Status *"
            value={status}
            onChange={(e) => setStatus(e.target.value as ProgressStatus)}
            options={[
              { label: 'Completed', value: 'COMPLETED' },
              { label: 'In Progress', value: 'IN_PROGRESS' },
              { label: 'Partial', value: 'PARTIAL' },
            ]}
          />

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsLogModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createProgressMutation.isPending}>
              Save Work Log
            </Button>
          </div>
        </form>
      </Modal>

      {/* View Detail Modal */}
      {viewingProgress && (
        <Modal
          isOpen={!!viewingProgress}
          onClose={() => setViewingProgress(null)}
          title={`Work Log: ${viewingProgress.progressDate}`}
          description="Detailed breakdown of daily engineering focus"
        >
          <div className="space-y-3 text-xs">
            <div className="p-3 bg-[#f8fafc] dark:bg-slate-800 border border-[#e2e8f0] dark:border-slate-700 rounded-[2px]">
              <span className="text-[10px] uppercase font-bold text-slate-400 block">Topics Studied / Work Done</span>
              <p className="font-bold text-sm text-slate-900 dark:text-slate-100 mt-1">{viewingProgress.whatStudied}</p>
            </div>

            <div className="p-3 bg-[#f8fafc] dark:bg-slate-800 border border-[#e2e8f0] dark:border-slate-700 rounded-[2px]">
              <span className="text-[10px] uppercase font-bold text-slate-400 block">Deliverables Completed</span>
              <p className="font-semibold text-slate-800 dark:text-slate-200 mt-1">
                {viewingProgress.completed || viewingProgress.whatCompleted || 'None recorded'}
              </p>
            </div>

            {viewingProgress.challenges && (
              <div className="p-3 bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 rounded-[2px]">
                <span className="text-[10px] uppercase font-bold text-amber-700 dark:text-amber-400 block">Blockers Encountered</span>
                <p className="text-amber-900 dark:text-amber-200 mt-1">{viewingProgress.challenges}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-2">
              <div className="p-2.5 border border-[#e2e8f0] dark:border-slate-700 rounded-[2px]">
                <span className="text-[10px] text-slate-400 font-bold uppercase block">Focus Time</span>
                <span className="font-bold text-slate-800 dark:text-slate-200 mt-0.5 block">{viewingProgress.studyMinutes} Minutes</span>
              </div>
              <div className="p-2.5 border border-[#e2e8f0] dark:border-slate-700 rounded-[2px]">
                <span className="text-[10px] text-slate-400 font-bold uppercase block">Status</span>
                <span className="font-bold text-slate-800 dark:text-slate-200 mt-0.5 block">{viewingProgress.status}</span>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-200 dark:border-slate-700">
              <Button variant="outline" size="sm" onClick={() => setViewingProgress(null)}>
                Close
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
