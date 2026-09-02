import React, { useState } from 'react';
import {
  Users,
  Plus,
  UserPlus,
  Mail,
  Crown,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
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
  useTeamMembers,
  useAddTeamMember,
} from '@/hooks/useTeams';
import { formatDate } from '@/lib/utils';
import { TeamMember } from '@/types/team';
import { filterRealTeamMembers, filterRealUsers } from '@/lib/userFilter';

export const TeamPage: React.FC = () => {
  const { activeTeam, teams, users, createTeam } = useApp();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [isAddMemberModalOpen, setIsAddMemberModalOpen] = useState(false);
  const [isCreateTeamModalOpen, setIsCreateTeamModalOpen] = useState(false);

  // Add Member State
  const [selectedUserId, setSelectedUserId] = useState('');

  // Create Team State
  const [newTeamName, setNewTeamName] = useState('');
  const [newTeamDescription, setNewTeamDescription] = useState('');
  const [isCreatingTeam, setIsCreatingTeam] = useState(false);

  const { data: rawMembersPage, isLoading: isMembersLoading } = useTeamMembers(activeTeam?.id, { size: 1000 });
  const addMemberMutation = useAddTeamMember();

  const rawMembers = rawMembersPage?.content || [];

  // Strictly filter out internal system/admin accounts
  const realMembers = filterRealTeamMembers(rawMembers);
  const realUsers = filterRealUsers(users);

  // Available users to add who are not already in this team
  const availableUsersToAdd = realUsers.filter(
    (u) => !realMembers.some((m) => m.userId === u.id)
  );

  // Identify Team Lead / Owner
  const teamLead =
    realMembers.find((m) => m.role === 'OWNER' || m.role === 'ADMIN') ||
    (realMembers.length > 0 ? realMembers[0] : null);

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeTeam || !selectedUserId) {
      showToast('warning', 'Please select a user to add');
      return;
    }

    try {
      await addMemberMutation.mutateAsync({
        teamId: activeTeam.id,
        userId: selectedUserId,
      });
      showToast('success', 'Team member added successfully!');
      setIsAddMemberModalOpen(false);
      setSelectedUserId('');
    } catch (err: unknown) {
      showToast('error', 'Failed to add member', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleCreateTeam = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTeamName.trim()) return;

    try {
      setIsCreatingTeam(true);
      await createTeam(newTeamName.trim(), newTeamDescription.trim() || undefined);
      showToast('success', 'Team created successfully! You are now the Team Lead.');
      setIsCreateTeamModalOpen(false);
      setNewTeamName('');
      setNewTeamDescription('');
    } catch (err: unknown) {
      showToast('error', 'Failed to create team', err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setIsCreatingTeam(false);
    }
  };

  return (
    <div className="space-y-4">
      {/* 1. Header Bar */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Users className="w-4 h-4 text-blue-600" />
            {activeTeam?.name || 'Developer Team Workspace'}
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            {activeTeam?.description || 'Collaborative engineering squad synchronization'} • Total Members: <span className="font-bold text-slate-800 dark:text-slate-200">{realMembers.length}</span>
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            leftIcon={<Plus className="w-3.5 h-3.5 text-blue-600" />}
            onClick={() => setIsCreateTeamModalOpen(true)}
          >
            Create New Team
          </Button>

          <Button
            variant="primary"
            size="sm"
            leftIcon={<UserPlus className="w-3.5 h-3.5" />}
            onClick={() => {
              if (availableUsersToAdd.length === 0) {
                showToast('info', 'All registered developers are already members of this team.');
              }
              setSelectedUserId(availableUsersToAdd[0]?.id || '');
              setIsAddMemberModalOpen(true);
            }}
          >
            Add Member
          </Button>
        </div>
      </div>

      {/* 2. Team Lead & Leadership Banner */}
      <div className="bg-white dark:bg-slate-900 border-2 border-blue-600/60 dark:border-blue-500/60 rounded-[3px] p-3.5 shadow-xs">
        <div className="flex items-center justify-between">
          <span className="text-[10px] font-bold uppercase tracking-wider text-blue-700 dark:text-blue-400 flex items-center gap-1.5">
            <Crown className="w-3.5 h-3.5 text-amber-500" />
            Designated Team Lead / Workspace Owner
          </span>
          <Badge size="sm" variant="primary">
            TEAM LEAD
          </Badge>
        </div>

        <div className="flex items-center gap-3 mt-2.5">
          <div className="w-10 h-10 rounded-[2px] bg-blue-700 text-white flex items-center justify-center font-bold text-sm shadow-xs">
            {teamLead?.userName?.charAt(0).toUpperCase() || user?.name?.charAt(0).toUpperCase() || 'L'}
          </div>
          <div>
            <div className="font-bold text-sm text-slate-900 dark:text-slate-100 flex items-center gap-2">
              {teamLead?.userName || user?.name || 'Workspace Creator'}
              {teamLead?.userId === user?.id && (
                <span className="text-[10px] bg-emerald-100 text-emerald-800 font-semibold px-1.5 py-0.2 rounded-[2px] border border-emerald-300">
                  YOU
                </span>
              )}
            </div>
            <div className="text-xs text-slate-500 dark:text-slate-400 flex items-center gap-1 mt-0.5">
              <Mail className="w-3 h-3" />
              {teamLead?.userEmail || user?.email || 'admin@workspace'}
            </div>
          </div>
        </div>
      </div>

      {/* 3. Team Member Roster Data Table (Unlimited list) */}
      <Card>
        <CardHeader>
          <CardTitle>Team Member Roster</CardTitle>
          <span className="text-[11px] text-slate-500">{realMembers.length} Active Members</span>
        </CardHeader>
        <CardContent className="p-0">
          {isMembersLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
            </div>
          ) : realMembers.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="classic-table">
                <thead>
                  <tr>
                    <th style={{ width: '40px' }}>#</th>
                    <th>Developer Name</th>
                    <th>Email Address</th>
                    <th>Team Role</th>
                    <th>Joined Workspace</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {realMembers.map((member: TeamMember, idx: number) => {
                    const isSelf = user && member.userId === user.id;
                    const isOwner = member.role === 'OWNER';
                    return (
                      <tr key={member.id || member.userId} className={isSelf ? 'bg-blue-50/60 dark:bg-blue-950/20' : ''}>
                        <td className="text-slate-400 font-mono text-[11px]">{idx + 1}</td>
                        <td>
                          <div className="flex items-center gap-2">
                            <div className="w-5 h-5 rounded-[2px] bg-slate-200 dark:bg-slate-700 text-slate-800 dark:text-slate-200 flex items-center justify-center font-bold text-[10px]">
                              {member.userName?.charAt(0).toUpperCase() || 'U'}
                            </div>
                            <span className="font-bold text-slate-900 dark:text-slate-100">{member.userName}</span>
                            {isSelf && (
                              <Badge size="sm" variant="primary">
                                YOU
                              </Badge>
                            )}
                          </div>
                        </td>
                        <td className="text-slate-600 dark:text-slate-400">{member.userEmail}</td>
                        <td>
                          <Badge
                            size="sm"
                            variant={isOwner ? 'warning' : member.role === 'ADMIN' ? 'primary' : 'default'}
                          >
                            {isOwner ? 'LEAD / OWNER' : member.role}
                          </Badge>
                        </td>
                        <td className="text-slate-500 text-[11px]">{formatDate(member.joinedAt)}</td>
                        <td>
                          <Badge size="sm" variant={member.active ? 'success' : 'danger'}>
                            {member.active ? 'ACTIVE' : 'INACTIVE'}
                          </Badge>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="p-6">
              <EmptyState
                icon={Users}
                title="No team members in workspace"
                description="Add developers from your organization to collaborate on shared goals and tasks."
                action={
                  <Button variant="primary" size="sm" onClick={() => setIsAddMemberModalOpen(true)}>
                    Add First Member
                  </Button>
                }
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* 4. Available Team Workspaces Switcher Panel */}
      <Card>
        <CardHeader>
          <CardTitle>Available Team Workspaces</CardTitle>
          <span className="text-[11px] text-slate-500">{teams.length} Workspaces</span>
        </CardHeader>
        <CardContent className="p-0">
          <div className="divide-y divide-slate-200 dark:divide-slate-800">
            {teams.map((t) => (
              <div
                key={t.id}
                className={`p-3 flex items-center justify-between text-xs transition-colors ${
                  activeTeam?.id === t.id ? 'bg-blue-50/50 dark:bg-blue-950/20' : 'hover:bg-slate-50 dark:hover:bg-slate-800/40'
                }`}
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-slate-900 dark:text-slate-100">{t.name}</span>
                    {activeTeam?.id === t.id && (
                      <Badge size="sm" variant="primary">
                        CURRENT ACTIVE
                      </Badge>
                    )}
                  </div>
                  {t.description && <p className="text-slate-500 text-[11px] mt-0.5">{t.description}</p>}
                </div>

                {activeTeam?.id !== t.id && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      window.location.reload();
                    }}
                  >
                    Switch to Workspace
                  </Button>
                )}
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Add Member Modal */}
      <Modal
        isOpen={isAddMemberModalOpen}
        onClose={() => setIsAddMemberModalOpen(false)}
        title={`Add Member to "${activeTeam?.name}"`}
        description="Select a registered developer to join this workspace"
      >
        <form onSubmit={handleAddMember} className="space-y-3">
          <Select
            label="Select Developer *"
            value={selectedUserId}
            onChange={(e) => setSelectedUserId(e.target.value)}
            options={availableUsersToAdd.map((u) => ({
              label: `${u.name} (${u.email})`,
              value: u.id,
            }))}
            required
          />

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsAddMemberModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={addMemberMutation.isPending}>
              Add to Team
            </Button>
          </div>
        </form>
      </Modal>

      {/* Create Team Modal */}
      <Modal
        isOpen={isCreateTeamModalOpen}
        onClose={() => setIsCreateTeamModalOpen(false)}
        title="Create New Team Workspace"
        description="Initialize a new team workspace. You will be assigned as the Team Lead."
      >
        <form onSubmit={handleCreateTeam} className="space-y-3">
          <Input
            label="Team Workspace Name *"
            value={newTeamName}
            onChange={(e) => setNewTeamName(e.target.value)}
            placeholder="e.g. Distributed Systems Lab"
            required
          />

          <Input
            label="Description (Optional)"
            value={newTeamDescription}
            onChange={(e) => setNewTeamDescription(e.target.value)}
            placeholder="e.g. Backend performance, Raft, and consensus engineering"
          />

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateTeamModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={isCreatingTeam}>
              Create Team & Become Lead
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
