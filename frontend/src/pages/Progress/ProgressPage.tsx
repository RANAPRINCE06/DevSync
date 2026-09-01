import React, { useState } from 'react';
import {
  TrendingUp,
  Plus,
  Calendar,
  Clock,
  CheckCircle2,
  Edit2,
  Eye,
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
import { useProgressList, useCreateProgress, useUpdateProgress } from '@/hooks/useProgress';
import { formatMinutes, formatDate } from '@/lib/utils';
import { DailyProgress, ProgressStatus } from '@/types/progress';

export const ProgressPage: React.FC = () => {
  const { activeUser, activeTeam } = useApp();
  const { showToast } = useToast();

  const [page, setPage] = useState(0);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Modal States
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingEntry, setEditingEntry] = useState<DailyProgress | null>(null);
  const [viewingEntry, setViewingEntry] = useState<DailyProgress | null>(null);

  // Form Fields
  const [progressDate, setProgressDate] = useState(new Date().toISOString().split('T')[0]);
  const [whatStudied, setWhatStudied] = useState('');
  const [whatCompleted, setWhatCompleted] = useState('');
  const [blockers, setBlockers] = useState('');
  const [studyMinutes, setStudyMinutes] = useState(60);
  const [status, setStatus] = useState<ProgressStatus>('IN_PROGRESS');

  const { data: progressData, isLoading } = useProgressList({
    userId: activeUser?.id,
    teamId: activeTeam?.id,
    fromDate: fromDate || undefined,
    toDate: toDate || undefined,
    status: selectedStatus !== 'ALL' ? (selectedStatus as ProgressStatus) : undefined,
    page,
    size: 10,
    sort: 'progressDate,desc',
  });

  const createMutation = useCreateProgress();
  const updateMutation = useUpdateProgress();

  const handleOpenCreate = () => {
    setEditingEntry(null);
    setProgressDate(new Date().toISOString().split('T')[0]);
    setWhatStudied('');
    setWhatCompleted('');
    setBlockers('');
    setStudyMinutes(60);
    setStatus('IN_PROGRESS');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (entry: DailyProgress) => {
    setEditingEntry(entry);
    setProgressDate(entry.progressDate);
    setWhatStudied(entry.whatStudied);
    setWhatCompleted(entry.whatCompleted || '');
    setBlockers(entry.blockers || '');
    setStudyMinutes(entry.studyMinutes);
    setStatus(entry.status);
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeUser || !activeTeam) {
      showToast('error', 'Select an active user and team first');
      return;
    }
    if (!whatStudied.trim()) {
      showToast('warning', 'What studied field is required');
      return;
    }

    try {
      if (editingEntry) {
        await updateMutation.mutateAsync({
          id: editingEntry.id,
          data: {
            whatStudied,
            whatCompleted: whatCompleted.trim() ? whatCompleted : undefined,
            blockers: blockers.trim() ? blockers : undefined,
            studyMinutes: Number(studyMinutes) || 0,
            status,
          },
        });
        showToast('success', 'Progress log updated successfully');
      } else {
        await createMutation.mutateAsync({
          userId: activeUser.id,
          teamId: activeTeam.id,
          progressDate,
          whatStudied,
          whatCompleted: whatCompleted.trim() ? whatCompleted : undefined,
          blockers: blockers.trim() ? blockers : undefined,
          studyMinutes: Number(studyMinutes) || 0,
          status,
        });
        showToast('success', 'Daily progress recorded! (+10 leaderboard score)');
      }
      setIsModalOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const filteredEntries =
    progressData?.content?.filter(
      (p) =>
        p.whatStudied.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (p.whatCompleted && p.whatCompleted.toLowerCase().includes(searchQuery.toLowerCase()))
    ) || [];

  // Summary Metrics
  const totalMinutes =
    progressData?.content?.reduce((acc, curr) => acc + (curr.studyMinutes || 0), 0) || 0;
  const completedEntriesCount =
    progressData?.content?.filter((p) => p.status === 'COMPLETED').length || 0;

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-primary-400" />
            Daily Progress & Accountability
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Log daily study sessions, track consistency, and share progress with your team.
          </p>
        </div>
        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />} onClick={handleOpenCreate}>
          Log Progress
        </Button>
      </div>

      {/* 2. Top Summary Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Total Focus Time</span>
            <Clock className="w-4 h-4 text-primary-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{formatMinutes(totalMinutes)}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Across current filtered entries</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Completed Sessions</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{completedEntriesCount}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Full sessions delivered</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Total Entries Logged</span>
            <Calendar className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{progressData?.totalElements || 0}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Historical accountability logs</p>
        </Card>
      </div>

      {/* 3. Search & Filter Bar */}
      <Card className="p-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3">
          <Input
            placeholder="Search activity or topics..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />

          <Select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            options={[
              { label: 'All Statuses', value: 'ALL' },
              { label: 'In Progress', value: 'IN_PROGRESS' },
              { label: 'Completed', value: 'COMPLETED' },
              { label: 'Partial', value: 'PARTIAL' },
            ]}
          />

          <Input
            type="date"
            placeholder="From Date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />

          <Input
            type="date"
            placeholder="To Date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
        </div>
      </Card>

      {/* 4. Progress Logs Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between w-full">
            <CardTitle>Progress History</CardTitle>
            <span className="text-xs text-slate-500">{progressData?.totalElements || 0} total entries</span>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
            </div>
          ) : filteredEntries.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300">
                <thead className="bg-slate-950/60 text-slate-400 uppercase text-[10px] font-semibold border-b border-slate-800">
                  <tr>
                    <th className="px-4 py-3">Date</th>
                    <th className="px-4 py-3">What Studied / Worked On</th>
                    <th className="px-4 py-3">Completed</th>
                    <th className="px-4 py-3">Focus</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {filteredEntries.map((p) => (
                    <tr key={p.id} className="hover:bg-slate-850/50 transition-colors">
                      <td className="px-4 py-3 font-medium text-slate-200 whitespace-nowrap">
                        {formatDate(p.progressDate)}
                      </td>
                      <td className="px-4 py-3 max-w-xs truncate text-slate-200">{p.whatStudied}</td>
                      <td className="px-4 py-3 max-w-xs truncate text-slate-400">
                        {p.whatCompleted || '—'}
                      </td>
                      <td className="px-4 py-3 font-semibold text-primary-300 whitespace-nowrap">
                        {formatMinutes(p.studyMinutes)}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
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
                      <td className="px-4 py-3 text-right whitespace-nowrap">
                        <div className="flex items-center justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => setViewingEntry(p)}
                            title="View details"
                          >
                            <Eye className="w-3.5 h-3.5" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleOpenEdit(p)}
                            title="Edit entry"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
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
              title="No progress entries found"
              description="Record your daily learnings or adjust your date filters."
              action={
                <Button variant="primary" size="sm" onClick={handleOpenCreate}>
                  Log Today's Progress
                </Button>
              }
            />
          )}

          {progressData && progressData.totalPages > 1 && (
            <Pagination
              currentPage={page}
              totalPages={progressData.totalPages}
              totalElements={progressData.totalElements}
              onPageChange={setPage}
            />
          )}
        </CardContent>
      </Card>

      {/* Create / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingEntry ? 'Edit Progress Entry' : 'Log Daily Progress'}
        description="Share what you studied and built to stay accountable with your team."
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            type="date"
            label="Progress Date *"
            value={progressDate}
            onChange={(e) => setProgressDate(e.target.value)}
            disabled={!!editingEntry}
            required
          />

          <Input
            label="What did you study / work on today? *"
            placeholder="e.g. Solved Graph algorithms, studied Spring JPA specs"
            value={whatStudied}
            onChange={(e) => setWhatStudied(e.target.value)}
            required
          />

          <Input
            label="Completed Work (Optional)"
            placeholder="e.g. Completed 2 LeetCode Medium problems"
            value={whatCompleted}
            onChange={(e) => setWhatCompleted(e.target.value)}
          />

          <Input
            label="Any Blockers? (Optional)"
            placeholder="e.g. Need help with database indexing"
            value={blockers}
            onChange={(e) => setBlockers(e.target.value)}
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              type="number"
              label="Study Minutes"
              min={0}
              max={1440}
              value={studyMinutes}
              onChange={(e) => setStudyMinutes(Number(e.target.value))}
            />

            <Select
              label="Status"
              value={status}
              onChange={(e) => setStatus(e.target.value as ProgressStatus)}
              options={[
                { label: 'In Progress', value: 'IN_PROGRESS' },
                { label: 'Completed', value: 'COMPLETED' },
                { label: 'Partial', value: 'PARTIAL' },
              ]}
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={createMutation.isPending || updateMutation.isPending}
            >
              {editingEntry ? 'Update Entry' : 'Submit Progress'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* View Details Modal */}
      {viewingEntry && (
        <Modal
          isOpen={!!viewingEntry}
          onClose={() => setViewingEntry(null)}
          title={`Progress Log — ${formatDate(viewingEntry.progressDate)}`}
        >
          <div className="space-y-4 text-xs">
            <div className="p-3 rounded-xl bg-slate-950/50 border border-slate-800 space-y-1">
              <span className="text-slate-500 block font-medium">What Studied / Worked On</span>
              <p className="text-slate-100 text-sm">{viewingEntry.whatStudied}</p>
            </div>

            {viewingEntry.whatCompleted && (
              <div className="p-3 rounded-xl bg-slate-950/50 border border-slate-800 space-y-1">
                <span className="text-slate-500 block font-medium">Completed Items</span>
                <p className="text-slate-200">{viewingEntry.whatCompleted}</p>
              </div>
            )}

            {viewingEntry.blockers && (
              <div className="p-3 rounded-xl bg-amber-950/20 border border-amber-800/40 space-y-1">
                <span className="text-amber-400 block font-medium">Blockers & Challenges</span>
                <p className="text-amber-200">{viewingEntry.blockers}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-3 pt-2">
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-slate-500 block">Focus Minutes</span>
                <span className="text-primary-300 font-bold text-sm">
                  {formatMinutes(viewingEntry.studyMinutes)}
                </span>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-slate-500 block">Status</span>
                <Badge variant={viewingEntry.status === 'COMPLETED' ? 'success' : 'primary'} size="sm">
                  {viewingEntry.status}
                </Badge>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setViewingEntry(null)}>
                Close
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
