import React, { useState } from 'react';
import { Award, Plus } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Skeleton } from '@/components/ui/Skeleton';
import { useUserAchievements, useUserTotalPoints, useCreateAchievement } from '@/hooks/useAchievements';
import { formatDate } from '@/lib/utils';
import { AchievementType } from '@/types/achievement';

export const AchievementsPage: React.FC = () => {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // Form State
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState<AchievementType>('DSA');
  const [points, setPoints] = useState('50');

  const { data: achievements = [], isLoading } = useUserAchievements(user?.id);
  const { data: pointsData } = useUserTotalPoints(user?.id);

  const createAchievementMutation = useCreateAchievement();

  const totalPoints = pointsData?.totalPoints || 0;

  const filteredAchievements = achievements.filter((a) => {
    return typeFilter === 'ALL' || a.type === typeFilter;
  });

  const handleCreateAchievement = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    try {
      await createAchievementMutation.mutateAsync({
        userId: user.id,
        type,
        title: title.trim(),
        description: description.trim() || undefined,
        points: parseInt(points, 10) || 50,
        earnedAt: new Date().toISOString(),
      });
      showToast('success', 'Badge / Achievement unlocked successfully!');
      setIsCreateModalOpen(false);
      setTitle('');
      setDescription('');
    } catch (err: unknown) {
      showToast('error', 'Failed to award badge', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const categories: { label: string; value: AchievementType | 'ALL' }[] = [
    { label: 'All Categories', value: 'ALL' },
    { label: 'DSA & Algorithms', value: 'DSA' },
    { label: 'Streaks & Consistency', value: 'STREAK' },
    { label: 'Goal Mastery', value: 'GOAL' },
    { label: 'Task Execution', value: 'TASK' },
    { label: 'Competitive Coding', value: 'CODING_PLATFORM' },
    { label: 'Team Leadership', value: 'TEAM' },
    { label: 'Special Recognition', value: 'SPECIAL' },
  ];

  return (
    <div className="space-y-4">
      {/* 1. Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Award className="w-4 h-4 text-indigo-600" />
            Gamified Achievements & Badge Catalog
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Total Points Earned: <span className="font-bold text-blue-700 dark:text-blue-400">{totalPoints} pts</span> • Unlock badges through milestones and coding streaks
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-3.5 h-3.5" />}
          onClick={() => setIsCreateModalOpen(true)}
        >
          Claim / Award Badge
        </Button>
      </div>

      {/* 2. Category Filter Tabs */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-2 rounded-[3px] flex items-center gap-1.5 overflow-x-auto text-xs">
        {categories.map((c) => (
          <button
            key={c.value}
            onClick={() => setTypeFilter(c.value)}
            className={`px-3 py-1 font-semibold rounded-[2px] whitespace-nowrap transition-colors ${
              typeFilter === c.value
                ? 'bg-blue-600 text-white'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200'
            }`}
          >
            {c.label}
          </button>
        ))}
      </div>

      {/* 3. Badges Grid */}
      <Card>
        <CardHeader>
          <CardTitle>Unlocked Badges Catalog</CardTitle>
          <span className="text-[11px] text-slate-500">{filteredAchievements.length} Badges Earned</span>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
              <Skeleton className="h-24 w-full" />
              <Skeleton className="h-24 w-full" />
              <Skeleton className="h-24 w-full" />
            </div>
          ) : filteredAchievements.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
              {filteredAchievements.map((a) => (
                <div
                  key={a.id}
                  className="p-3 bg-[#f8fafc] dark:bg-slate-800/60 border border-[#e2e8f0] dark:border-slate-700 rounded-[2px] flex items-start gap-3"
                >
                  <div className="w-10 h-10 rounded-[2px] bg-blue-700 text-white flex items-center justify-center font-bold text-xs shrink-0 shadow-xs">
                    <Award className="w-5 h-5 text-amber-300" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-1">
                      <span className="font-bold text-xs text-slate-900 dark:text-slate-100 truncate">{a.title}</span>
                      <Badge size="sm" variant="warning">
                        +{a.points} pts
                      </Badge>
                    </div>
                    {a.description && <p className="text-[11px] text-slate-500 mt-1 line-clamp-2">{a.description}</p>}
                    <div className="flex items-center justify-between text-[10px] text-slate-400 mt-2 pt-1 border-t border-slate-200 dark:border-slate-700">
                      <span>{a.type}</span>
                      <span>{formatDate(a.earnedAt)}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-6 text-center text-xs text-slate-500 italic">
              No badges unlocked in this category yet. Keep completing sprint tasks and daily progress to earn points.
            </div>
          )}
        </CardContent>
      </Card>

      {/* Award Badge Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Claim / Award Achievement Badge"
        description="Record a milestone badge unlock"
      >
        <form onSubmit={handleCreateAchievement} className="space-y-3">
          <Input
            label="Badge Title *"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. 30-Day LeetCode Streak"
            required
          />
          <Input
            label="Description (Optional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Completed 30 days of daily algorithmic problem solving"
          />

          <div className="grid grid-cols-2 gap-2">
            <Select
              label="Category / Type *"
              value={type}
              onChange={(e) => setType(e.target.value as AchievementType)}
              options={[
                { label: 'DSA & Algorithms', value: 'DSA' },
                { label: 'Streaks & Consistency', value: 'STREAK' },
                { label: 'Goal Mastery', value: 'GOAL' },
                { label: 'Task Execution', value: 'TASK' },
                { label: 'Competitive Coding', value: 'CODING_PLATFORM' },
                { label: 'Team Leadership', value: 'TEAM' },
                { label: 'Special Recognition', value: 'SPECIAL' },
              ]}
            />
            <Input
              label="Points Awarded *"
              type="number"
              min="10"
              max="500"
              value={points}
              onChange={(e) => setPoints(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createAchievementMutation.isPending}>
              Award Badge
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
