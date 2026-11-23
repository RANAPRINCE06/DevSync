export type CodingPlatform =
  | 'LEETCODE'
  | 'CODEFORCES'
  | 'CODECHEF'
  | 'HACKERRANK'
  | 'GITHUB'
  | 'ATCODER';

export type PlatformCapability =
  | 'PROFILE_LINK_SUPPORTED'
  | 'AUTOMATED_SYNC_SUPPORTED'
  | 'AUTHENTICATED_SYNC_REQUIRED'
  | 'COMMUNITY_API_SUPPORTED'
  | 'CONTEST_RATING_SUPPORTED'
  | 'PROBLEM_COUNTS_SUPPORTED'
  | 'DIFFICULTY_BREAKDOWN_SUPPORTED'
  | 'ACTIVITY_HISTORY_SUPPORTED';

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
  capabilities?: PlatformCapability[];
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
  highestRatingPlatform?: CodingPlatform | null;
  bestRank: number | null;
  connectedPlatformsCount: number;
  platformBreakdown: Record<string, PlatformSummary>;
}

export interface ConnectPlatformRequest {
  platform?: CodingPlatform;
  username?: string;
  profileUrl?: string;
}

export interface ProfilePreviewRequest {
  profileUrl: string;
}

export interface PreviewMetrics {
  problemsSolved: number | null;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  currentRating: number | null;
  maximumRating: number | null;
  globalRank: number | null;
  platformRank: string | null;
  contestsParticipated: number | null;
  publicRepos: number | null;
  totalStars: number | null;
  followers: number | null;
  additionalStats: Record<string, unknown> | null;
}

export interface ProfilePreviewResponse {
  platform: CodingPlatform;
  platformDisplayName: string;
  username: string;
  canonicalProfileUrl: string;
  profileExists: boolean;
  displayName: string | null;
  avatarUrl: string | null;
  autoSyncSupported: boolean;
  capabilityNotice: string | null;
  warning: string | null;
  alreadyConnected: boolean;
  metrics: PreviewMetrics | null;
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
  syncSupported: boolean;
  communityApi?: boolean;
  requiresAuth?: boolean;
  enterpriseOnly?: boolean;
  syncNote?: string;
  capabilities?: PlatformCapability[];
}

export interface PlatformMetadata {
  platform: CodingPlatform;
  displayName: string;
  profileUrlBase: string;
  available: boolean;
  syncSupported: boolean;
  communityApi: boolean;
  capabilities: PlatformCapability[];
  syncNote: string;
}

export interface AnalyticsQueryParams {
  platform?: CodingPlatform;
  from?: string;
  to?: string;
  period?: '7d' | '30d' | '90d' | '1y' | 'all';
}

export interface PlatformOverviewItem {
  platform: CodingPlatform;
  username: string;
  problemsSolved: number | null;
  currentRating: number | null;
  globalRank: number | null;
  contestsParticipated: number | null;
  lastSyncedAt: string | null;
  syncStatus?: SyncStatus;
  active?: boolean;
  autoSyncSupported?: boolean;
  additionalStats?: Record<string, unknown> | null;
}

export interface CodingAnalyticsOverview {
  totalProblemsSolved: number;
  connectedPlatformsCount: number;
  highestRating: number | null;
  highestRatingPlatform: CodingPlatform | null;
  totalContestsParticipated: number;
  totalSnapshotsCollected: number;
  firstSnapshotAt: string | null;
  lastSnapshotAt: string | null;
  platformOverviews: PlatformOverviewItem[];
  totalGitHubRepositories?: number | null;
  totalGitHubStars?: number | null;
  activeSyncPlatformsCount?: number;
  latestSuccessfulSyncAt?: string | null;
  syncHealthStatus?: 'HEALTHY' | 'ATTENTION_NEEDED' | 'NORMAL';
}

export interface RatingPoint {
  timestamp: string;
  rating: number;
  globalRank: number | null;
}

export interface PlatformRatingSeries {
  platform: CodingPlatform;
  username: string;
  currentRating: number | null;
  maximumRating: number | null;
  personalBestRating: number | null;
  ratingChange: number | null;
  dataAvailable: boolean;
  history: RatingPoint[];
}

export interface RatingHistoryResponse {
  period: string;
  from: string | null;
  to: string | null;
  platforms: PlatformRatingSeries[];
}

export interface PlatformProblemBreakdown {
  platform: CodingPlatform;
  username: string;
  totalSolved: number;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  percentageOfTotal: number;
  deltaInPeriod: number | null;
}

export interface ProblemDistributionResponse {
  period: string;
  from: string | null;
  to: string | null;
  totalPlatformReportedSolved: number;
  easySolved: number;
  mediumSolved: number;
  hardSolved: number;
  unclassifiedSolved: number;
  solvedDeltaInPeriod: number;
  verifiedUniqueDeduplication: boolean;
  deduplicationNote: string;
  platformBreakdowns: PlatformProblemBreakdown[];
}

export interface SnapshotActivityRecord {
  timestamp: string;
  platform: CodingPlatform;
  username: string;
  problemsSolved: number | null;
  currentRating: number | null;
  solvedChange: number | null;
  ratingChange: number | null;
}

export interface CodingActivityResponse {
  period: string;
  from: string | null;
  to: string | null;
  dailySubmissionActivitySupported: boolean;
  coverageNotice: string;
  totalSnapshotsInPeriod: number;
  activeDaysWithSnapshots: number;
  firstSnapshotAt: string | null;
  lastSnapshotAt: string | null;
  syncCadenceDescription: string;
  snapshotTimeline: SnapshotActivityRecord[];
}

export interface ContestRatingPoint {
  timestamp: string;
  rating: number | null;
  contestsCount: number | null;
}

export interface PlatformContestRecord {
  platform: CodingPlatform;
  username: string;
  contestsSupported: boolean;
  contestsParticipated: number | null;
  currentRating: number | null;
  bestRating: number | null;
  ratingChange: number | null;
  contestHistory: ContestRatingPoint[];
}

export interface ContestPerformanceResponse {
  period: string;
  from: string | null;
  to: string | null;
  totalContestsParticipated: number;
  anyContestsSupported: boolean;
  platformContests: PlatformContestRecord[];
}

export interface PerformanceInsightItem {
  id: string;
  platform: CodingPlatform | null;
  category: 'RATING' | 'SOLVED' | 'CONTEST' | 'DATA_COVERAGE';
  type: 'POSITIVE' | 'NEUTRAL' | 'INFORMATIONAL' | 'ATTENTION';
  title: string;
  message: string;
  metricValue: string;
}

export interface PerformanceInsightsResponse {
  period: string;
  from: string | null;
  to: string | null;
  insightsCount: number;
  insights: PerformanceInsightItem[];
}
