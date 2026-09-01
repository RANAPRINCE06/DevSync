export type TeamRole = 'OWNER' | 'ADMIN' | 'MEMBER';

export interface Team {
  id: string;
  name: string;
  description?: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TeamMember {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  userAvatarUrl?: string | null;
  teamId: string;
  role: TeamRole;
  active: boolean;
  joinedAt: string;
}

export interface CreateTeamRequest {
  name: string;
  description?: string;
  creatorUserId: string;
}

export interface UpdateTeamRequest {
  name: string;
  description?: string;
}
