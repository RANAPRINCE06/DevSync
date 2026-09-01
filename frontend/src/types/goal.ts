export type GoalStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ON_HOLD' | 'CANCELLED';
export type GoalPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface Goal {
  id: string;
  ownerId: string;
  ownerName: string;
  ownerEmail: string;
  teamId: string;
  teamName: string;
  title: string;
  description?: string | null;
  status: GoalStatus;
  priority: GoalPriority;
  progress: number;
  startDate: string;
  targetDate: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateGoalRequest {
  ownerId: string;
  teamId: string;
  title: string;
  description?: string;
  status?: GoalStatus;
  priority?: GoalPriority;
  progress?: number;
  startDate: string;
  targetDate: string;
}

export interface UpdateGoalRequest {
  title: string;
  description?: string;
  status?: GoalStatus;
  priority?: GoalPriority;
  progress?: number;
  startDate: string;
  targetDate: string;
  active?: boolean;
}

export interface GoalFilterParams {
  ownerId?: string;
  teamId?: string;
  status?: GoalStatus;
  priority?: GoalPriority;
  active?: boolean;
  startDate?: string;
  targetDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}
