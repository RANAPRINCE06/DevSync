export type CodingPlatform =
  | 'LEETCODE'
  | 'CODEFORCES'
  | 'CODECHEF'
  | 'HACKERRANK'
  | 'GITHUB'
  | 'ATCODER';

export type SyncStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'NEVER_SYNCED';

export interface PlatformAccount {
  id: string;
  userId: string;
  platform: CodingPlatform;
  platformDisplayName?: string;
  username: string;
  profileUrl: string;
  active: boolean;
  syncStatus: SyncStatus;
  lastSyncedAt: string | null;
  lastSyncAttemptAt: string | null;
  lastSyncError: string | null;
  latestSnapshot?: PlatformStatSnapshot | null;
  createdAt: string;
  updatedAt: string;
}

export interface PlatformStatSnapshot {
  id: string;
  platformAccountId: string;
  currentRating: number | null;
  maximumRating: number | null;
  globalRank: number | null;
  countryRank: number | null;
  problemsSolved: number | null;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  contestsParticipated: number | null;
  additionalStats: Record<string, unknown> | null;
  snapshotAt: string;
  createdAt: string;
}

export interface PlatformSyncStatus {
  id: string;
  platform: CodingPlatform;
  username: string;
  syncStatus: SyncStatus;
  lastSyncedAt: string | null;
  lastSyncAttemptAt: string | null;
  lastSyncError: string | null;
}

export interface PlatformSummary {
  platform: CodingPlatform;
  username: string;
  profileUrl: string;
  syncStatus: SyncStatus;
  currentRating: number | null;
  maxRating: number | null;
  globalRank: number | null;
  problemsSolved: number | null;
  lastSyncedAt: string | null;
}

export interface CodingStatistics {
  totalProblemsSolved: number | null;
  totalContestsParticipated: number | null;
  highestRating: number | null;
  bestRank: number | null;
  connectedPlatformsCount: number;
  platformBreakdown: Record<string, PlatformSummary>;
}

export interface ConnectPlatformRequest {
  platform: CodingPlatform;
  username: string;
}

export interface PlatformConfig {
  name: string;
  displayName: string;
  platform: CodingPlatform;
  urlPrefix: string;
  iconName: string;
  color: string;
  bgLight: string;
  description: string;
  usernameRegex: RegExp;
  usernamePlaceholder: string;
}
