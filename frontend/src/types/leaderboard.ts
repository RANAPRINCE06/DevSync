export type LeaderboardPeriod = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL_TIME';

export interface LeaderboardEntry {
  rank: number;
  userId: string;
  userName: string;
  teamId: string;
  teamName: string;
  score: number;
  progressEntries: number;
  completedTasks: number;
  completedGoals: number;
  achievementPoints: number;
}
