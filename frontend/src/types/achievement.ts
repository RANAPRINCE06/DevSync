export type AchievementType = 'STREAK' | 'DSA' | 'GOAL' | 'TASK' | 'CODING_PLATFORM' | 'TEAM' | 'SPECIAL';

export interface Achievement {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  title: string;
  description?: string | null;
  type: AchievementType;
  icon?: string | null;
  points: number;
  earnedAt: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAchievementRequest {
  userId: string;
  title: string;
  description?: string;
  type: AchievementType;
  icon?: string;
  points: number;
  earnedAt: string;
}

export interface UpdateAchievementRequest {
  title: string;
  description?: string;
  type: AchievementType;
  icon?: string;
  points: number;
  earnedAt: string;
  active?: boolean;
}

export interface AchievementFilterParams {
  userId?: string;
  type?: AchievementType;
  active?: boolean;
  earnedFrom?: string;
  earnedTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}
