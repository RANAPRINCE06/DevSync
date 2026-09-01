import React, { useState } from 'react';
import {
  Medal,
  Plus,
  Flame,
  Award,
  Sparkles,
  Calendar,
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
import { Tabs } from '@/components/ui/Tabs';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { Pagination } from '@/components/ui/Pagination';
import { useAchievements, useCreateAchievement, useUserTotalPoints } from '@/hooks/useAchievements';
import { formatDate } from '@/lib/utils';
import { AchievementType } from '@/types/achievement';

export const AchievementsPage: React.FC = () => {
  const { activeUser, users } = useApp();
  const { showToast } = useToast();

  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [page, setPage] = useState(0);

  // Award Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [targetUserId, setTargetUserId] = useState(activeUser?.id || '');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState<AchievementType>('STREAK');
  const [points, setPoints] = useState<number>(50);

  const { data: achievementsData, isLoading } = useAchievements({
    userId: activeUser?.id,
    type: selectedCategory !== 'ALL' ? (selectedCategory as AchievementType) : undefined,
    active: true,
    page,
    size: 12,
    sort: 'earnedAt,desc',
  });

  const { data: totalPointsData } = useUserTotalPoints(activeUser?.id);
  const createMutation = useCreateAchievement();

  const categoryTabs = [
    { id: 'ALL', label: 'All Badges' },
    { id: 'STREAK', label: 'Streaks' },
    { id: 'DSA', label: 'DSA' },
    { id: 'GOAL', label: 'Goals' },
    { id: 'TASK', label: 'Tasks' },
    { id: 'CODING_PLATFORM', label: 'Platforms' },
    { id: 'TEAM', label: 'Team' },
    { id: 'SPECIAL', label: 'Special' },
  ];

  const handleOpenAward = () => {
    setTargetUserId(activeUser?.id || users[0]?.id || '');
    setTitle('');
    setDescription('');
    setType('STREAK');
    setPoints(50);
    setIsModalOpen(true);
  };

  const handleAwardSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetUserId) {
      showToast('warning', 'Please select a user');
      return;
    }
    if (!title.trim()) {
      showToast('warning', 'Achievement title is required');
      return;
    }

    try {
      await createMutation.mutateAsync({
        userId: targetUserId,
        title,
        description: description.trim() ? description : undefined,
        type,
        points: Number(points) || 0,
        earnedAt: new Date().toISOString(),
      });
      showToast('success', `Achievement awarded! (+${points} pts)`);
      setIsModalOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to award achievement', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const totalPoints = totalPointsData?.totalPoints || 0;
  const earnedCount = achievementsData?.totalElements || 0;

  const getTier = (pts: number) => {
    if (pts >= 1000) return { name: 'Diamond Tier', color: 'text-cyan-400 bg-cyan-950/60 border-cyan-800' };
    if (pts >= 500) return { name: 'Platinum Tier', color: 'text-violet-300 bg-violet-950/60 border-violet-800' };
    if (pts >= 250) return { name: 'Gold Tier', color: 'text-amber-300 bg-amber-950/60 border-amber-800' };
    if (pts >= 100) return { name: 'Silver Tier', color: 'text-slate-300 bg-slate-800 border-slate-700' };
    return { name: 'Bronze Tier', color: 'text-amber-500 bg-amber-950/40 border-amber-900' };
  };

  const currentTier = getTier(totalPoints);

  const getCategoryIcon = (categoryType: AchievementType) => {
    switch (categoryType) {
      case 'STREAK':
        return '🔥';
      case 'DSA':
        return '🧩';
      case 'GOAL':
        return '🎯';
      case 'TASK':
        return '⚡';
      case 'CODING_PLATFORM':
        return '💻';
      case 'TEAM':
        return '🤝';
      case 'SPECIAL':
      default:
        return '🏆';
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Medal className="w-5 h-5 text-primary-400" />
            Achievements & Developer Milestones
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Celebrate streaks, problem-solving milestones, and personal coding bests.
          </p>
        </div>

        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />} onClick={handleOpenAward}>
          Award Badge
        </Button>
      </div>

      {/* 2. Overview Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Total Achievement Points</span>
            <Flame className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">
            {totalPoints} <span className="text-xs font-normal text-amber-400">pts</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-0.5">Applied to team leaderboard</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Badges Unlocked</span>
            <Award className="w-4 h-4 text-primary-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{earnedCount}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Verified developer milestones</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Current Rank Tier</span>
            <Sparkles className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="mt-2">
            <span
              className={`inline-block px-2.5 py-1 rounded-lg text-xs font-bold border ${currentTier.color}`}
            >
              {currentTier.name}
            </span>
          </div>
          <p className="text-[11px] text-slate-500 mt-1">Based on aggregate points</p>
        </Card>
      </div>

      {/* 3. Category Filter Tabs */}
      <div className="overflow-x-auto pb-1">
        <Tabs
          tabs={categoryTabs}
          activeTab={selectedCategory}
          onChange={(tabId) => {
            setSelectedCategory(tabId);
            setPage(0);
          }}
        />
      </div>

      {/* 4. Badges Grid */}
      <div>
        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <Skeleton className="h-36 w-full" />
            <Skeleton className="h-36 w-full" />
            <Skeleton className="h-36 w-full" />
          </div>
        ) : achievementsData?.content && achievementsData.content.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {achievementsData.content.map((ach) => (
              <Card
                key={ach.id}
                hoverable
                className="flex items-start gap-4 p-4 bg-slate-900/80 border-slate-800"
              >
                <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-primary-950 to-slate-900 border border-primary-800/80 flex items-center justify-center text-2xl shadow-inner shrink-0">
                  {getCategoryIcon(ach.type)}
                </div>

                <div className="space-y-1.5 min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-1">
                    <h3 className="text-xs font-bold text-slate-100 truncate">{ach.title}</h3>
                    <Badge size="sm" variant="primary" className="shrink-0 font-mono">
                      +{ach.points} pts
                    </Badge>
                  </div>

                  {ach.description && (
                    <p className="text-[11px] text-slate-400 line-clamp-2">{ach.description}</p>
                  )}

                  <div className="flex items-center justify-between text-[10px] text-slate-500 pt-2 border-t border-slate-800/60">
                    <span className="uppercase font-semibold tracking-wider text-[9px] text-slate-400">
                      {ach.type}
                    </span>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3 h-3" />
                      {formatDate(ach.earnedAt)}
                    </span>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <EmptyState
            icon={Filter}
            title="No achievements in this category"
            description="Log daily progress and complete tasks to earn milestone badges."
            action={
              <Button variant="primary" size="sm" onClick={handleOpenAward}>
                Award Badge
              </Button>
            }
          />
        )}

        {achievementsData && achievementsData.totalPages > 1 && (
          <Pagination
            currentPage={page}
            totalPages={achievementsData.totalPages}
            totalElements={achievementsData.totalElements}
            onPageChange={setPage}
          />
        )}
      </div>

      {/* Award Badge Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Award Milestone Badge"
        description="Record a verified milestone achievement for a developer."
      >
        <form onSubmit={handleAwardSubmit} className="space-y-4">
          <Select
            label="Award To *"
            value={targetUserId}
            onChange={(e) => setTargetUserId(e.target.value)}
            options={users.map((u) => ({ label: `${u.name} (${u.email})`, value: u.id }))}
            required
          />

          <Input
            label="Badge Title *"
            placeholder="e.g. 14-Day Consistency Champion"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />

          <Input
            label="Description (Optional)"
            placeholder="e.g. Logged daily study without missing a single day for 2 weeks"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />

          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Category"
              value={type}
              onChange={(e) => setType(e.target.value as AchievementType)}
              options={[
                { label: 'Streak', value: 'STREAK' },
                { label: 'DSA', value: 'DSA' },
                { label: 'Goal', value: 'GOAL' },
                { label: 'Task', value: 'TASK' },
                { label: 'Coding Platform', value: 'CODING_PLATFORM' },
                { label: 'Team', value: 'TEAM' },
                { label: 'Special', value: 'SPECIAL' },
              ]}
            />

            <Input
              type="number"
              label="Points"
              min={0}
              value={points}
              onChange={(e) => setPoints(Number(e.target.value))}
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createMutation.isPending}>
              Award Badge
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
