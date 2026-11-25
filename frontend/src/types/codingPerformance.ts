import { CodingPlatform } from './codingIntelligence';

export interface PlatformAchievementResponse {
  id: string;
  platform: CodingPlatform;
  platformDisplayName: string;
  achievementId: string;
  name: string;
  description: string | null;
  imageUrl: string | null;
  category: string;
  level: string | null;
  earnedDate: string | null;
  sourceUrl: string | null;
  lastVerifiedAt: string;
}

export interface PlatformAchievementsSummaryResponse {
  totalEarned: number;
  platformBreakdown: Record<string, number>;
  recentAchievements: PlatformAchievementResponse[];
  allAchievements: PlatformAchievementResponse[];
}

export interface ContestRecordResponse {
  id: string;
  platform: CodingPlatform;
  contestId: string;
  contestName: string;
  contestDate: string;
  contestRank: number | null;
  participantsCount: number | null;
  previousRating: number | null;
  ratingChange: number | null;
  newRating: number;
  problemsSolved: number | null;
  performanceScore: number | null;
}

export interface PlatformContestHistoryResponse {
  platform: CodingPlatform;
  platformDisplayName: string;
  currentRating: number | null;
  highestRating: number | null;
  totalContests: number;
  contests: ContestRecordResponse[];
  ratingTierDistribution: Record<string, number>;
}

export interface GitHubContributionDay {
  date: string;
  count: number;
  intensity: number; // 0 to 4
}

export interface GitHubContributionResponse {
  publicRepos: number;
  totalStars: number;
  totalContributions: number;
  activeDays: number;
  currentStreak: number;
  longestStreak: number;
  dailyContributions: GitHubContributionDay[];
}

export interface DailyCommitPoint {
  date: string;
  count: number;
}

export interface MonthlyCommitPoint {
  month: string;
  count: number;
}

export interface RepoCommitBreakdown {
  repoName: string;
  commits: number;
}

export interface GitHubCommitActivityResponse {
  totalCommits: number;
  last7DaysCommits: number;
  last30DaysCommits: number;
  last90DaysCommits: number;
  dailyCommits: DailyCommitPoint[];
  monthlyCommits: MonthlyCommitPoint[];
  repoBreakdown: RepoCommitBreakdown[];
}

export interface GitHubRepositoryResponse {
  id: string;
  repoName: string;
  primaryLanguage: string | null;
  starsCount: number;
  forksCount: number;
  recentCommitsCount: number;
  lastUpdatedAt: string | null;
  repoUrl: string;
  description: string | null;
}
