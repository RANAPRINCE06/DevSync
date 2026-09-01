export type ProgressStatus = 'IN_PROGRESS' | 'COMPLETED' | 'PARTIAL';

export interface DailyProgress {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  teamId: string;
  teamName: string;
  progressDate: string;
  whatStudied: string;
  whatCompleted?: string | null;
  blockers?: string | null;
  studyMinutes: number;
  status: ProgressStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDailyProgressRequest {
  userId: string;
  teamId: string;
  progressDate: string;
  whatStudied: string;
  whatCompleted?: string;
  blockers?: string;
  studyMinutes: number;
  status: ProgressStatus;
}

export interface UpdateDailyProgressRequest {
  whatStudied: string;
  whatCompleted?: string;
  blockers?: string;
  studyMinutes: number;
  status: ProgressStatus;
}

export interface ProgressFilterParams {
  userId?: string;
  teamId?: string;
  date?: string;
  fromDate?: string;
  toDate?: string;
  status?: ProgressStatus;
  page?: number;
  size?: number;
  sort?: string;
}
