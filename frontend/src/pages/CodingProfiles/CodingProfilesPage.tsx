import React, { useState } from 'react';
import { Code2, Plus, ExternalLink } from 'lucide-react';
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
import {
  useUserCodingProfiles,
  useCreateCodingProfile,
  useDeleteCodingProfile,
} from '@/hooks/useCodingProfiles';
import { CodingPlatform } from '@/types/codingProfile';

export const CodingProfilesPage: React.FC = () => {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [isLinkModalOpen, setIsLinkModalOpen] = useState(false);

  // Form State
  const [platform, setPlatform] = useState<CodingPlatform>('LEETCODE');
  const [username, setUsername] = useState('');
  const [profileUrl, setProfileUrl] = useState('');
  const [rating, setRating] = useState('1850');
  const [problemsSolved, setProblemsSolved] = useState('450');
  const [rank, setRank] = useState('Top 5%');
  const [contestsParticipated, setContestsParticipated] = useState('24');

  const { data: profiles = [], isLoading } = useUserCodingProfiles(user?.id);
  const createProfileMutation = useCreateCodingProfile();
  const deleteProfileMutation = useDeleteCodingProfile();

  const handleLinkProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    try {
      await createProfileMutation.mutateAsync({
        userId: user.id,
        platform,
        username: username.trim(),
        profileUrl: profileUrl.trim() || undefined,
        rating: parseInt(rating, 10) || 0,
        problemsSolved: parseInt(problemsSolved, 10) || 0,
        rank: rank.trim() || undefined,
        contestsParticipated: parseInt(contestsParticipated, 10) || 0,
      });
      showToast('success', `${platform} profile linked successfully!`);
      setIsLinkModalOpen(false);
      setUsername('');
      setProfileUrl('');
    } catch (err: unknown) {
      showToast('error', 'Failed to link profile', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDeleteProfile = async (id: string) => {
    if (!confirm('Are you sure you want to remove this coding profile link?')) return;
    try {
      await deleteProfileMutation.mutateAsync(id);
      showToast('success', 'Profile link removed');
    } catch (err: unknown) {
      showToast('error', 'Failed to remove profile', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  return (
    <div className="space-y-4">
      {/* 1. Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Code2 className="w-4 h-4 text-blue-600" />
            Competitive Coding Accounts & Profiles
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Link and display accounts from LeetCode, Codeforces, CodeChef, HackerRank, and GitHub
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<Plus className="w-3.5 h-3.5" />}
          onClick={() => setIsLinkModalOpen(true)}
        >
          Link Coding Account
        </Button>
      </div>

      {/* 2. Classic Data Table */}
      <Card>
        <CardHeader>
          <CardTitle>Linked Competitive Accounts</CardTitle>
          <span className="text-[11px] text-slate-500">{profiles.length} Accounts Connected</span>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : profiles.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="classic-table">
                <thead>
                  <tr>
                    <th style={{ width: '40px' }}>#</th>
                    <th>Platform</th>
                    <th>Username</th>
                    <th>Rating</th>
                    <th>Problems Solved</th>
                    <th>Contests</th>
                    <th>Rank / Tier</th>
                    <th className="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {profiles.map((p, idx) => (
                    <tr key={p.id}>
                      <td className="text-slate-400 font-mono text-[11px]">{idx + 1}</td>
                      <td>
                        <Badge size="sm" variant="primary">
                          {p.platform}
                        </Badge>
                      </td>
                      <td className="font-bold text-slate-900 dark:text-slate-100">{p.username}</td>
                      <td className="font-bold text-blue-700 dark:text-blue-400">{p.rating || '—'}</td>
                      <td className="font-semibold text-slate-800 dark:text-slate-200">{p.problemsSolved || 0}</td>
                      <td className="text-slate-600 dark:text-slate-400">{p.contestsParticipated || 0}</td>
                      <td className="text-slate-500 text-[11px]">{p.rank || '—'}</td>
                      <td className="text-right whitespace-nowrap">
                        {p.profileUrl && (
                          <a
                            href={p.profileUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-[2px] font-semibold text-slate-700 dark:text-slate-200 mr-1"
                          >
                            Profile <ExternalLink className="w-3 h-3" />
                          </a>
                        )}
                        <button
                          onClick={() => handleDeleteProfile(p.id)}
                          className="px-2 py-0.5 text-[11px] bg-red-50 hover:bg-red-100 text-red-700 border border-red-300 rounded-[2px] font-semibold"
                        >
                          Remove
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
                icon={Code2}
                title="No coding profiles linked"
                description="Connect your LeetCode, Codeforces, or GitHub profiles to showcase your ratings and problem counts."
                action={
                  <Button variant="primary" size="sm" onClick={() => setIsLinkModalOpen(true)}>
                    Link Profile
                  </Button>
                }
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Link Modal */}
      <Modal
        isOpen={isLinkModalOpen}
        onClose={() => setIsLinkModalOpen(false)}
        title="Link Competitive Coding Profile"
        description="Add a coding account to your public profile"
      >
        <form onSubmit={handleLinkProfile} className="space-y-3">
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
          />

          <Input
            label="Username / Handle *"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="e.g. touriste_dev"
            required
          />

          <Input
            label="Public Profile URL (Optional)"
            type="url"
            value={profileUrl}
            onChange={(e) => setProfileUrl(e.target.value)}
            placeholder="https://leetcode.com/username"
          />

          <div className="grid grid-cols-2 gap-2">
            <Input
              label="Rating (Optional)"
              type="number"
              value={rating}
              onChange={(e) => setRating(e.target.value)}
            />
            <Input
              label="Problems Solved"
              type="number"
              value={problemsSolved}
              onChange={(e) => setProblemsSolved(e.target.value)}
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <Input
              label="Rank / Tier (Optional)"
              value={rank}
              onChange={(e) => setRank(e.target.value)}
              placeholder="e.g. Candidate Master / Top 5%"
            />
            <Input
              label="Contests Participated"
              type="number"
              value={contestsParticipated}
              onChange={(e) => setContestsParticipated(e.target.value)}
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsLinkModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={createProfileMutation.isPending}>
              Save Profile Link
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
