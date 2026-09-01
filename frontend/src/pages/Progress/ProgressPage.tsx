import React, { useState, useMemo } from 'react';
import {
  TrendingUp,
  Plus,
  Search,
  Calendar,
  Clock,
  AlertCircle,
  Eye,
  Edit2,
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
  useProgressList,
  useCreateProgress,
  useUpdateProgress,
} from '@/hooks/useProgress';
import { formatDate, formatMinutes } from '@/lib/utils';
import { DailyProgress, ProgressStatus } from '@/types/progress';

export const ProgressPage: React.FC = () => {
  const { activeUser, activeTeam } = useApp();
  const { showToast } = useToast();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  // Modals
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProgress, setEditingProgress] = useState<DailyProgress | null>(null);
  const [viewingProgress, setViewingProgress] = useState<DailyProgress | null>(null);

  // Form states
  const [progressDate, setProgressDate] = useState(new Date().toISOString().split('T')[0]);
  const [whatStudied, setWhatStudied] = useState('');
  const [whatCompleted, setWhatCompleted] = useState('');
  const [blockers, setBlockers] = useState('');
  const [studyMinutes, setStudyMinutes] = useState<number>(60);
  const [status, setStatus] = useState<ProgressStatus>('IN_PROGRESS');

  // Queries
  const {
    data: progressPage,
    isLoading,
    isError,
    refetch,
  } = useProgressList({
    userId: activeUser?.id,
    teamId: activeTeam?.id,
    fromDate: fromDate || undefined,
    toDate: toDate || undefined,
    size: 50,
  });

  // Mutations
  const createProgressMutation = useCreateProgress();
  const updateProgressMutation = useUpdateProgress();

  const allEntries = progressPage?.content || [];

  // Filtered Entries
  const filteredEntries = useMemo(() => {
    return allEntries.filter((p) => {
      const matchesSearch =
        p.whatStudied.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (p.whatCompleted && p.whatCompleted.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (p.blockers && p.blockers.toLowerCase().includes(searchQuery.toLowerCase()));

      const matchesStatus = statusFilter === 'ALL' || p.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [allEntries, searchQuery, statusFilter]);

  // Analytics Metrics
  const totalMinutes = allEntries.reduce((acc, curr) => acc + curr.studyMinutes, 0);
  const completedCount = allEntries.filter((p) => p.status === 'COMPLETED').length;

  const handleOpenCreate = () => {
    setEditingProgress(null);
    setProgressDate(new Date().toISOString().split('T')[0]);
    setWhatStudied('');
    setWhatCompleted('');
    setBlockers('');
    setStudyMinutes(60);
    setStatus('IN_PROGRESS');
    setIsModalOpen(true);
  };

  const handleOpenEdit = (p: DailyProgress) => {
    setEditingProgress(p);
    setProgressDate(p.progressDate);
    setWhatStudied(p.whatStudied);
    setWhatCompleted(p.whatCompleted || '');
    setBlockers(p.blockers || '');
    setStudyMinutes(p.studyMinutes);
    setStatus(p.status);
    setIsModalOpen(true);
  };

  const handleSaveProgress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!whatStudied.trim()) {
      showToast('warning', 'What studied is required');
      return;
    }
    if (!activeUser || !activeTeam) {
      showToast('error', 'Active user and team required');
      return;
    }

    try {
      if (editingProgress) {
        await updateProgressMutation.mutateAsync({
          id: editingProgress.id,
          data: {
            whatStudied,
            whatCompleted: whatCompleted.trim() ? whatCompleted : undefined,
            blockers: blockers.trim() ? blockers : undefined,
            studyMinutes: Number(studyMinutes) || 0,
            status,
          },
        });
        showToast('success', 'Progress entry updated successfully!');
      } else {
        await createProgressMutation.mutateAsync({
          userId: activeUser.id,
          teamId: activeTeam.id,
          progressDate,
          whatStudied,
          whatCompleted: whatCompleted.trim() ? whatCompleted : undefined,
          blockers: blockers.trim() ? blockers : undefined,
          studyMinutes: Number(studyMinutes) || 0,
          status,
        });
        showToast('success', 'Daily progress logged successfully! (+10 pts)');
      }
      setIsModalOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to save progress', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-primary-400" />
            Daily Progress & Accountability
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Log your daily learning, technical tasks, and work session focus hours.
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={handleOpenCreate}
        >
          Log Daily Progress
        </Button>
      </div>

      {/* 2. Summary Analytics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <span className="text-xs font-medium text-slate-400">Total Focus Time</span>
          <div className="text-2xl font-bold text-slate-100 mt-1">{formatMinutes(totalMinutes)}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Across all recorded sessions</p>
        </Card>

        <Card>
          <span className="text-xs font-medium text-slate-400">Completed Sessions</span>
          <div className="text-2xl font-bold text-emerald-400 mt-1">{completedCount}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Marked as Completed status</p>
        </Card>

        <Card>
          <span className="text-xs font-medium text-slate-400">Total Log Entries</span>
          <div className="text-2xl font-bold text-primary-300 mt-1">{allEntries.length}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">+10 leaderboard points per entry</p>
        </Card>
      </div>

      {/* 3. Filter Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 p-3 rounded-2xl bg-slate-900/80 border border-slate-800">
        <div className="flex flex-wrap items-center gap-2 flex-1 max-w-2xl">
          {/* Search */}
          <div className="relative min-w-[200px] flex-1">
            <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search progress logs, topics, blockers..."
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
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="PARTIAL">Partial</option>
          </select>

          {/* Date range filters */}
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className="h-8 px-2 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
            title="From Date"
          />
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className="h-8 px-2 rounded-xl bg-slate-950/60 border border-slate-800 text-xs text-slate-300 focus:outline-none"
            title="To Date"
          />
        </div>
      </div>

      {/* 4. Progress Entries Table / List */}
      {isLoading ? (
        <div className="space-y-3">
          <Skeleton className="h-16 rounded-2xl" />
          <Skeleton className="h-16 rounded-2xl" />
          <Skeleton className="h-16 rounded-2xl" />
        </div>
      ) : isError ? (
        <ErrorState
          title="Failed to load progress records"
          description="Could not connect to the backend server. Please try again."
          onRetry={() => refetch()}
        />
      ) : filteredEntries.length === 0 ? (
        <EmptyState
          icon={TrendingUp}
          title="No progress entries found"
          description="Start building your streak by logging today's focus work."
          action={
            <Button variant="primary" size="sm" onClick={handleOpenCreate}>
              Log Daily Progress
            </Button>
          }
        />
      ) : (
        <div className="space-y-3">
          {filteredEntries.map((p) => (
            <Card key={p.id} hoverable className="p-4 space-y-2.5 group">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                <div className="flex items-center gap-2.5">
                  <span className="text-xs font-bold text-slate-100 flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-primary-400" />
                    {formatDate(p.progressDate)}
                  </span>
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
                  <span className="text-xs font-semibold text-primary-300 flex items-center gap-1">
                    <Clock className="w-3 h-3" /> {formatMinutes(p.studyMinutes)}
                  </span>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => setViewingProgress(p)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors"
                    title="View details"
                  >
                    <Eye className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleOpenEdit(p)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors"
                    title="Edit entry"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs pt-1">
                <div>
                  <span className="text-[10px] font-semibold text-slate-500 uppercase tracking-wider block">
                    What Studied / Worked On
                  </span>
                  <p className="text-slate-200 mt-0.5">{p.whatStudied}</p>
                </div>

                {p.whatCompleted && (
                  <div>
                    <span className="text-[10px] font-semibold text-slate-500 uppercase tracking-wider block">
                      Completed Deliverables
                    </span>
                    <p className="text-slate-200 mt-0.5">{p.whatCompleted}</p>
                  </div>
                )}
              </div>

              {p.blockers && (
                <div className="p-2 rounded-lg bg-amber-950/20 border border-amber-900/30 text-xs text-amber-300 flex items-center gap-2">
                  <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                  <span>Blockers: {p.blockers}</span>
                </div>
              )}
            </Card>
          ))}
        </div>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingProgress ? 'Edit Progress Entry' : "Log Today's Progress"}
        description="Share daily learnings with your team to stay aligned and maintain accountability."
      >
        <form onSubmit={handleSaveProgress} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Input
              type="date"
              label="Progress Date *"
              value={progressDate}
              onChange={(e) => setProgressDate(e.target.value)}
              disabled={!!editingProgress}
              required
            />

            <Input
              type="number"
              label="Focus Minutes *"
              min={0}
              max={1440}
              value={studyMinutes}
              onChange={(e) => setStudyMinutes(Number(e.target.value))}
              required
            />
          </div>

          <Input
            label="What did you study / work on? *"
            placeholder="e.g. Practiced Dynamic Programming, completed Sprint backend endpoints"
            value={whatStudied}
            onChange={(e) => setWhatStudied(e.target.value)}
            required
          />

          <Input
            label="What items were completed? (Optional)"
            placeholder="e.g. 4 LeetCode mediums, updated OpenAPI spec"
            value={whatCompleted}
            onChange={(e) => setWhatCompleted(e.target.value)}
          />

          <Input
            label="Any Blockers or Impediments? (Optional)"
            placeholder="e.g. Waiting on PR review for DB migration"
            value={blockers}
            onChange={(e) => setBlockers(e.target.value)}
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

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => setIsModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={createProgressMutation.isPending || updateProgressMutation.isPending}
            >
              {editingProgress ? 'Update Entry' : 'Log Progress'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* View Detail Modal */}
      {viewingProgress && (
        <Modal
          isOpen={!!viewingProgress}
          onClose={() => setViewingProgress(null)}
          title={`Progress Log — ${formatDate(viewingProgress.progressDate)}`}
          description="Detailed daily work session record"
        >
          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              <span className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold block mb-1">
                What Studied
              </span>
              <p className="text-slate-200">{viewingProgress.whatStudied}</p>
            </div>

            {viewingProgress.whatCompleted && (
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800">
                <span className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold block mb-1">
                  Completed Items
                </span>
                <p className="text-slate-200">{viewingProgress.whatCompleted}</p>
              </div>
            )}

            {viewingProgress.blockers && (
              <div className="p-3 rounded-xl bg-amber-950/20 border border-amber-900/40">
                <span className="text-[10px] text-amber-400 uppercase tracking-wider font-semibold block mb-1">
                  Blockers
                </span>
                <p className="text-amber-300">{viewingProgress.blockers}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-3 pt-1">
              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Focus Minutes</span>
                <span className="font-bold text-primary-300 mt-1 block">
                  {formatMinutes(viewingProgress.studyMinutes)}
                </span>
              </div>

              <div className="p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="text-[10px] text-slate-500 block">Status</span>
                <Badge
                  size="sm"
                  className="mt-1"
                  variant={
                    viewingProgress.status === 'COMPLETED'
                      ? 'success'
                      : viewingProgress.status === 'IN_PROGRESS'
                      ? 'primary'
                      : 'warning'
                  }
                >
                  {viewingProgress.status}
                </Badge>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
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
