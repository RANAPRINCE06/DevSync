import React, { useState } from 'react';
import {
  Code2,
  Plus,
  ExternalLink,
  Edit2,
  Trash2,
  CheckCircle2,
  Trophy,
  Award,
  Flame,
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
import {
  useCodingProfiles,
  useCreateCodingProfile,
  useUpdateCodingProfile,
  useDeleteCodingProfile,
} from '@/hooks/useCodingProfiles';
import { CodingPlatform, CodingProfile } from '@/types/codingProfile';

export const CodingProfilesPage: React.FC = () => {
  const { activeUser } = useApp();
  const { showToast } = useToast();

  // Modal States
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProfile, setEditingProfile] = useState<CodingProfile | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Form Fields
  const [platform, setPlatform] = useState<CodingPlatform>('LEETCODE');
  const [username, setUsername] = useState('');
  const [profileUrl, setProfileUrl] = useState('');
  const [rating, setRating] = useState<number | undefined>(undefined);
  const [rank, setRank] = useState<string>('');
  const [problemsSolved, setProblemsSolved] = useState<number | undefined>(undefined);
  const [contestsParticipated, setContestsParticipated] = useState<number | undefined>(undefined);

  const { data: profilesPage, isLoading } = useCodingProfiles({
    userId: activeUser?.id,
    size: 20,
  });

  const createMutation = useCreateCodingProfile();
  const updateMutation = useUpdateCodingProfile();
  const deleteMutation = useDeleteCodingProfile();

  const handleOpenLink = () => {
    setEditingProfile(null);
    setPlatform('LEETCODE');
    setUsername('');
    setProfileUrl('');
    setRating(undefined);
    setRank('');
    setProblemsSolved(undefined);
    setContestsParticipated(undefined);
    setIsModalOpen(true);
  };

  const handleOpenEdit = (p: CodingProfile) => {
    setEditingProfile(p);
    setPlatform(p.platform);
    setUsername(p.username);
    setProfileUrl(p.profileUrl || '');
    setRating(p.rating || undefined);
    setRank(p.rank || '');
    setProblemsSolved(p.problemsSolved || undefined);
    setContestsParticipated(p.contestsParticipated || undefined);
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeUser) {
      showToast('error', 'Select an active user first');
      return;
    }
    if (!username.trim()) {
      showToast('warning', 'Platform username is required');
      return;
    }

    try {
      if (editingProfile) {
        await updateMutation.mutateAsync({
          id: editingProfile.id,
          data: {
            username,
            profileUrl: profileUrl.trim() ? profileUrl : undefined,
            rating: rating !== undefined ? Number(rating) : undefined,
            rank: rank.trim() ? rank : undefined,
            problemsSolved: problemsSolved !== undefined ? Number(problemsSolved) : undefined,
            contestsParticipated: contestsParticipated !== undefined ? Number(contestsParticipated) : undefined,
            active: true,
          },
        });
        showToast('success', 'Coding profile updated successfully');
      } else {
        await createMutation.mutateAsync({
          userId: activeUser.id,
          platform,
          username,
          profileUrl: profileUrl.trim() ? profileUrl : undefined,
          rating: rating !== undefined ? Number(rating) : undefined,
          rank: rank.trim() ? rank : undefined,
          problemsSolved: problemsSolved !== undefined ? Number(problemsSolved) : undefined,
          contestsParticipated: contestsParticipated !== undefined ? Number(contestsParticipated) : undefined,
        });
        showToast('success', `${platform} profile linked successfully!`);
      }
      setIsModalOpen(false);
    } catch (err: unknown) {
      showToast('error', 'Failed to link profile', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDelete = async () => {
    if (!deletingId) return;
    try {
      await deleteMutation.mutateAsync(deletingId);
      showToast('success', 'Profile unlinked successfully');
      setDeletingId(null);
    } catch (err: unknown) {
      showToast('error', 'Failed to unlink profile', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const profiles = profilesPage?.content || [];

  // Aggregate stats
  const totalSolved = profiles.reduce((acc, p) => acc + (p.problemsSolved || 0), 0);
  const highestRating = Math.max(...profiles.map((p) => p.rating || 0), 0);
  const totalContests = profiles.reduce((acc, p) => acc + (p.contestsParticipated || 0), 0);

  const getPlatformBrand = (plat: CodingPlatform) => {
    switch (plat) {
      case 'LEETCODE':
        return { name: 'LeetCode', color: 'text-amber-400 bg-amber-950/40 border-amber-800' };
      case 'CODEFORCES':
        return { name: 'Codeforces', color: 'text-rose-400 bg-rose-950/40 border-rose-800' };
      case 'CODECHEF':
        return { name: 'CodeChef', color: 'text-amber-600 bg-amber-950/40 border-amber-800' };
      case 'HACKERRANK':
        return { name: 'HackerRank', color: 'text-emerald-400 bg-emerald-950/40 border-emerald-800' };
      case 'GITHUB':
        return { name: 'GitHub', color: 'text-slate-200 bg-slate-800 border-slate-700' };
      case 'OTHER':
      default:
        return { name: 'Other', color: 'text-primary-400 bg-primary-950/40 border-primary-800' };
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Code2 className="w-5 h-5 text-indigo-400" />
            Coding Profiles & Competitive Stats
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Connect LeetCode, Codeforces, CodeChef, and GitHub profiles to aggregate problem-solving metrics.
          </p>
        </div>

        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />} onClick={handleOpenLink}>
          Link Profile
        </Button>
      </div>

      {/* 2. Overview Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Total Solved</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{totalSolved}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Problems solved across platforms</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Peak Rating</span>
            <Trophy className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{highestRating || 'N/A'}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Highest competitive rating</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Contests Attended</span>
            <Flame className="w-4 h-4 text-rose-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{totalContests}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Contests participated</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Connected Accounts</span>
            <Award className="w-4 h-4 text-primary-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{profiles.length}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Verified coding handles</p>
        </Card>
      </div>

      {/* 3. Platform Cards Grid */}
      <div>
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <Skeleton className="h-44 w-full" />
            <Skeleton className="h-44 w-full" />
          </div>
        ) : profiles.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {profiles.map((p) => {
              const brand = getPlatformBrand(p.platform);
              return (
                <Card key={p.id} hoverable className="flex flex-col justify-between space-y-4">
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <span className={`px-2.5 py-1 rounded-lg text-xs font-bold border ${brand.color}`}>
                        {brand.name}
                      </span>
                      <Badge size="sm" variant={p.active ? 'success' : 'default'}>
                        {p.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </div>

                    <div>
                      <h3 className="text-sm font-semibold text-slate-100 font-mono">@{p.username}</h3>
                      {p.profileUrl && (
                        <a
                          href={p.profileUrl}
                          target="_blank"
                          rel="noreferrer noopener"
                          className="text-[11px] text-primary-400 hover:text-primary-300 flex items-center gap-1 mt-0.5 transition-colors"
                        >
                          Visit Profile <ExternalLink className="w-3 h-3" />
                        </a>
                      )}
                    </div>

                    {/* Stats */}
                    <div className="grid grid-cols-3 gap-2 pt-2 border-t border-slate-850 text-xs">
                      <div className="p-2 rounded-lg bg-slate-950/50 border border-slate-800">
                        <span className="text-[10px] text-slate-500 block">Rating</span>
                        <span className="font-bold text-amber-300">{p.rating || '—'}</span>
                      </div>
                      <div className="p-2 rounded-lg bg-slate-950/50 border border-slate-800">
                        <span className="text-[10px] text-slate-500 block">Solved</span>
                        <span className="font-bold text-emerald-400">{p.problemsSolved || '—'}</span>
                      </div>
                      <div className="p-2 rounded-lg bg-slate-950/50 border border-slate-800">
                        <span className="text-[10px] text-slate-500 block">Contests</span>
                        <span className="font-bold text-primary-300">{p.contestsParticipated || '—'}</span>
                      </div>
                    </div>
                  </div>

                  {/* Actions Footer */}
                  <div className="flex items-center justify-end gap-1 pt-2 border-t border-slate-850">
                    <Button variant="ghost" size="icon" onClick={() => handleOpenEdit(p)} title="Edit profile">
                      <Edit2 className="w-3.5 h-3.5" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="text-rose-400"
                      onClick={() => setDeletingId(p.id)}
                      title="Unlink profile"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </Button>
                  </div>
                </Card>
              );
            })}
          </div>
        ) : (
          <EmptyState
            icon={Code2}
            title="No profiles linked"
            description="Link your LeetCode, Codeforces, or GitHub profiles to show off your competitive stats."
            action={
              <Button variant="primary" size="sm" onClick={handleOpenLink}>
                Link Profile
              </Button>
            }
          />
        )}
      </div>

      {/* Link / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingProfile ? 'Edit Coding Profile' : 'Link Coding Profile'}
        description="Aggregate your competitive coding problem count, contest rating, and rank."
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Platform *"
              value={platform}
              onChange={(e) => setPlatform(e.target.value as CodingPlatform)}
              options={[
                { label: 'LeetCode', value: 'LEETCODE' },
                { label: 'Codeforces', value: 'CODEFORCES' },
                { label: 'CodeChef', value: 'CODECHEF' },
                { label: 'HackerRank', value: 'HACKERRANK' },
                { label: 'GitHub', value: 'GITHUB' },
                { label: 'Other', value: 'OTHER' },
              ]}
              disabled={!!editingProfile}
            />

            <Input
              label="Handle / Username *"
              placeholder="e.g. tourist, neal_wu"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <Input
            label="Profile URL (Optional)"
            placeholder="https://leetcode.com/username"
            value={profileUrl}
            onChange={(e) => setProfileUrl(e.target.value)}
          />

          <div className="grid grid-cols-2 gap-3">
            <Input
              type="number"
              label="Problems Solved"
              min={0}
              placeholder="e.g. 450"
              value={problemsSolved !== undefined ? problemsSolved : ''}
              onChange={(e) =>
                setProblemsSolved(e.target.value ? Number(e.target.value) : undefined)
              }
            />

            <Input
              type="number"
              label="Current Rating"
              min={0}
              placeholder="e.g. 1850"
              value={rating !== undefined ? rating : ''}
              onChange={(e) => (e.target.value ? setRating(Number(e.target.value)) : setRating(undefined))}
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              type="number"
              label="Contests Participated"
              min={0}
              placeholder="e.g. 24"
              value={contestsParticipated !== undefined ? contestsParticipated : ''}
              onChange={(e) =>
                setContestsParticipated(e.target.value ? Number(e.target.value) : undefined)
              }
            />

            <Input
              label="Global / Platform Rank"
              placeholder="e.g. Top 2%, Specialist, Knight"
              value={rank}
              onChange={(e) => setRank(e.target.value)}
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
              {editingProfile ? 'Update Profile' : 'Link Profile'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      {deletingId && (
        <Modal
          isOpen={!!deletingId}
          onClose={() => setDeletingId(null)}
          title="Unlink Profile"
          description="Are you sure you want to unlink this competitive coding profile?"
        >
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" size="sm" onClick={() => setDeletingId(null)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              size="sm"
              isLoading={deleteMutation.isPending}
              onClick={handleDelete}
            >
              Unlink Profile
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
};
