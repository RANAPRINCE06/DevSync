import React, { useState } from 'react';
import {
  Users,
  UserPlus,
  Crown,
  ShieldCheck,
  UserCheck,
  Activity,
  Sparkles,
  CheckCircle2,
  Calendar,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { Select } from '@/components/ui/Select';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { useTeamMembers, useAddTeamMember } from '@/hooks/useTeams';
import { useGoals } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { formatDate } from '@/lib/utils';

export const TeamPage: React.FC = () => {
  const { activeTeam, users } = useApp();
  const { showToast } = useToast();

  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');

  const { data: membersPage, isLoading: isMembersLoading } = useTeamMembers(activeTeam?.id, { size: 50 });
  const { data: goalsPage } = useGoals({ teamId: activeTeam?.id, active: true, size: 50 });
  const { data: tasksPage } = useTasks({ teamId: activeTeam?.id, active: true, size: 50 });

  const addMemberMutation = useAddTeamMember();

  // Find users not yet in team
  const existingMemberUserIds = new Set(membersPage?.content?.map((m) => m.userId) || []);
  const availableUsersToInvite = users.filter((u) => !existingMemberUserIds.has(u.id));

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeTeam) {
      showToast('error', 'No active team selected');
      return;
    }
    if (!selectedUserId) {
      showToast('warning', 'Please select a user to add');
      return;
    }

    try {
      await addMemberMutation.mutateAsync({
        teamId: activeTeam.id,
        userId: selectedUserId,
      });
      showToast('success', 'Member added to team successfully!');
      setIsInviteModalOpen(false);
      setSelectedUserId('');
    } catch (err: unknown) {
      showToast('error', 'Failed to add team member', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const totalMembers = membersPage?.totalElements || 0;
  const totalGoals = goalsPage?.totalElements || 0;
  const completedTasks = tasksPage?.content?.filter((t) => t.status === 'COMPLETED').length || 0;

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Team Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Users className="w-5 h-5 text-sky-400" />
            {activeTeam?.name || 'Developer Team'}
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            {activeTeam?.description || 'Team workspace for daily accountability and group growth.'}
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          leftIcon={<UserPlus className="w-4 h-4" />}
          onClick={() => {
            setSelectedUserId(availableUsersToInvite[0]?.id || '');
            setIsInviteModalOpen(true);
          }}
        >
          Add Member
        </Button>
      </div>

      {/* 2. Team Overview Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Total Members</span>
            <Users className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{totalMembers}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Active engineers in workspace</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Active Goals</span>
            <Sparkles className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{totalGoals}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Shared learning objectives</p>
        </Card>

        <Card>
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Completed Tasks</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 mt-2">{completedTasks}</div>
          <p className="text-[11px] text-slate-500 mt-0.5">Sprint tasks completed</p>
        </Card>
      </div>

      {/* 3. Team Member Roster */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between w-full">
            <CardTitle>Member Directory</CardTitle>
            <span className="text-xs text-slate-500">{totalMembers} members</span>
          </div>
        </CardHeader>
        <CardContent>
          {isMembersLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-12 w-full" />
            </div>
          ) : membersPage?.content && membersPage.content.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300">
                <thead className="bg-slate-950/60 text-slate-400 uppercase text-[10px] font-semibold border-b border-slate-800">
                  <tr>
                    <th className="px-4 py-3">Member</th>
                    <th className="px-4 py-3">Email</th>
                    <th className="px-4 py-3">Role</th>
                    <th className="px-4 py-3">Joined Date</th>
                    <th className="px-4 py-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {membersPage.content.map((m) => (
                    <tr key={m.id} className="hover:bg-slate-850/50 transition-colors">
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2.5">
                          <div className="w-7 h-7 rounded-lg bg-primary-950 border border-primary-800 flex items-center justify-center text-primary-300 font-bold text-xs shrink-0">
                            {m.userName.charAt(0).toUpperCase()}
                          </div>
                          <span className="font-semibold text-slate-100">{m.userName}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-slate-400">{m.userEmail}</td>
                      <td className="px-4 py-3">
                        <Badge
                          size="sm"
                          variant={
                            m.role === 'OWNER'
                              ? 'warning'
                              : m.role === 'ADMIN'
                              ? 'primary'
                              : 'default'
                          }
                          className="flex items-center gap-1 w-fit"
                        >
                          {m.role === 'OWNER' && <Crown className="w-3 h-3 text-amber-400" />}
                          {m.role === 'ADMIN' && <ShieldCheck className="w-3 h-3 text-primary-400" />}
                          {m.role === 'MEMBER' && <UserCheck className="w-3 h-3 text-slate-400" />}
                          {m.role}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-slate-400">{formatDate(m.joinedAt)}</td>
                      <td className="px-4 py-3">
                        <Badge size="sm" variant={m.active ? 'success' : 'danger'}>
                          {m.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <EmptyState
              icon={Users}
              title="No team members yet"
              description="Invite other developers to your team to start daily synchronization."
              action={
                <Button variant="primary" size="sm" onClick={() => setIsInviteModalOpen(true)}>
                  Add Member
                </Button>
              }
            />
          )}
        </CardContent>
      </Card>

      {/* 4. Team Activity Stream */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Activity className="w-4 h-4 text-emerald-400" />
            <CardTitle>Team Activity Stream</CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {tasksPage?.content?.slice(0, 4).map((t) => (
              <div
                key={t.id}
                className="flex items-center justify-between p-3 rounded-xl bg-slate-950/40 border border-slate-800/80 text-xs"
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-6 h-6 rounded-lg bg-emerald-950 border border-emerald-800/60 flex items-center justify-center text-emerald-400 shrink-0">
                    ✓
                  </div>
                  <div>
                    <span className="text-slate-200 font-medium">{t.assigneeName}</span>{' '}
                    <span className="text-slate-400">
                      {t.status === 'COMPLETED' ? 'completed task' : 'is working on'}:
                    </span>{' '}
                    <span className="text-slate-100 font-semibold">{t.title}</span>
                  </div>
                </div>
                <span className="text-[10px] text-slate-500 flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  {formatDate(t.updatedAt)}
                </span>
              </div>
            ))}

            {(!tasksPage?.content || tasksPage.content.length === 0) && (
              <p className="text-xs text-slate-500 italic text-center py-4">
                No recent activity recorded for this team yet.
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Add / Invite Member Modal */}
      <Modal
        isOpen={isInviteModalOpen}
        onClose={() => setIsInviteModalOpen(false)}
        title="Add Member to Team"
        description="Add a registered developer to your accountability team."
      >
        <form onSubmit={handleAddMember} className="space-y-4">
          {availableUsersToInvite.length > 0 ? (
            <Select
              label="Select User *"
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              options={availableUsersToInvite.map((u) => ({
                label: `${u.name} (${u.email})`,
                value: u.id,
              }))}
              required
            />
          ) : (
            <p className="text-xs text-slate-400 p-3 rounded-xl bg-slate-950/60 border border-slate-800">
              All registered users in the database are already members of this team.
            </p>
          )}

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsInviteModalOpen(false)}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              isLoading={addMemberMutation.isPending}
              disabled={availableUsersToInvite.length === 0}
            >
              Add to Team
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
