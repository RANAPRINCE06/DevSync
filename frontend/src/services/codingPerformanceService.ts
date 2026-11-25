import { apiClient } from '@/services/client';
import { ApiResponse } from '@/types/api';
import { CodingPlatform } from '@/types/codingIntelligence';
import {
  PlatformAchievementsSummaryResponse,
  PlatformContestHistoryResponse,
  GitHubContributionResponse,
  GitHubCommitActivityResponse,
  GitHubRepositoryResponse,
} from '@/types/codingPerformance';

export const codingPerformanceService = {
  /**
   * Retrieves aggregated platform achievements and verified badges
   */
  async getAchievements(): Promise<PlatformAchievementsSummaryResponse> {
    const response = await apiClient.get<ApiResponse<PlatformAchievementsSummaryResponse>>('/coding/achievements');
    return response.data.data;
  },

  /**
   * Retrieves contest history for all supported contest platforms
   */
  async getAllContestHistories(): Promise<Record<string, PlatformContestHistoryResponse>> {
    const response = await apiClient.get<ApiResponse<Record<string, PlatformContestHistoryResponse>>>('/coding/contests');
    return response.data.data;
  },

  /**
   * Retrieves contest history for a specific platform
   */
  async getContestHistory(platform: CodingPlatform): Promise<PlatformContestHistoryResponse> {
    const response = await apiClient.get<ApiResponse<PlatformContestHistoryResponse>>(`/coding/contests/${platform}`);
    return response.data.data;
  },

  /**
   * Retrieves GitHub 52-week contribution heatmap matrix and streaks
   */
  async getGitHubContributions(): Promise<GitHubContributionResponse> {
    const response = await apiClient.get<ApiResponse<GitHubContributionResponse>>('/coding/github/contributions');
    return response.data.data;
  },

  /**
   * Retrieves GitHub commit activity over a timeframe (e.g. '30d', '90d', '1y')
   */
  async getGitHubCommitActivity(period: string = '90d'): Promise<GitHubCommitActivityResponse> {
    const response = await apiClient.get<ApiResponse<GitHubCommitActivityResponse>>(`/coding/github/commits?period=${period}`);
    return response.data.data;
  },

  /**
   * Retrieves repository insights and recent commits
   */
  async getGitHubRepositories(): Promise<GitHubRepositoryResponse[]> {
    const response = await apiClient.get<ApiResponse<GitHubRepositoryResponse[]>>('/coding/github/repositories');
    return response.data.data;
  },
};
