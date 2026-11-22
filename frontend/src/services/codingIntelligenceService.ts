import { apiClient } from '@/services/client';
import { ApiResponse } from '@/types/api';
import {
  PlatformAccount,
  PlatformStatSnapshot,
  PlatformSyncStatus,
  CodingStatistics,
  ConnectPlatformRequest,
} from '@/types/codingIntelligence';

export const codingIntelligenceService = {
  /**
   * Retrieves all connected active platform accounts for current user
   */
  async getConnectedAccounts(): Promise<PlatformAccount[]> {
    const response = await apiClient.get<ApiResponse<PlatformAccount[]>>('/coding/accounts');
    return response.data.data;
  },

  /**
   * Connects a platform profile to the current user
   */
  async connectAccount(request: ConnectPlatformRequest): Promise<PlatformAccount> {
    const response = await apiClient.post<ApiResponse<PlatformAccount>>('/coding/accounts', request);
    return response.data.data;
  },

  /**
   * Deactivates / disconnects a platform account
   */
  async disconnectAccount(accountId: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/coding/accounts/${accountId}`);
  },

  /**
   * Triggers manual synchronization of external profile statistics
   */
  async syncAccount(accountId: string): Promise<PlatformAccount> {
    const response = await apiClient.post<ApiResponse<PlatformAccount>>(`/coding/accounts/${accountId}/sync`);
    return response.data.data;
  },

  /**
   * Retrieves sync statuses for all connected platforms
   */
  async getSyncStatuses(): Promise<PlatformSyncStatus[]> {
    const response = await apiClient.get<ApiResponse<PlatformSyncStatus[]>>('/coding/accounts/status');
    return response.data.data;
  },

  /**
   * Retrieves aggregated coding intelligence statistics
   */
  async getAggregatedStatistics(): Promise<CodingStatistics> {
    const response = await apiClient.get<ApiResponse<CodingStatistics>>('/coding/statistics');
    return response.data.data;
  },

  /**
   * Retrieves historical snapshots for a given connected account
   */
  async getAccountHistory(accountId: string): Promise<PlatformStatSnapshot[]> {
    const response = await apiClient.get<ApiResponse<PlatformStatSnapshot[]>>(`/coding/accounts/${accountId}/history`);
    return response.data.data;
  },

  /**
   * Retrieves high-level analytics overview
   */
  async getAnalyticsOverview(): Promise<import('@/types/codingIntelligence').CodingAnalyticsOverview> {
    const response = await apiClient.get<ApiResponse<import('@/types/codingIntelligence').CodingAnalyticsOverview>>('/coding/analytics/overview');
    return response.data.data;
  },

  /**
   * Retrieves rating progression history
   */
  async getRatingHistory(params?: import('@/types/codingIntelligence').AnalyticsQueryParams): Promise<import('@/types/codingIntelligence').RatingHistoryResponse> {
    const response = await apiClient.get<ApiResponse<import('@/types/codingIntelligence').RatingHistoryResponse>>('/coding/analytics/rating-history', {
      params,
    });
    return response.data.data;
  },

  /**
   * Retrieves problem-solving distribution
   */
  async getProblemDistribution(params?: import('@/types/codingIntelligence').AnalyticsQueryParams): Promise<import('@/types/codingIntelligence').ProblemDistributionResponse> {
    const response = await apiClient.get<ApiResponse<import('@/types/codingIntelligence').ProblemDistributionResponse>>('/coding/analytics/problem-distribution', {
      params,
    });
    return response.data.data;
  },

  /**
   * Retrieves coding snapshot activity timeline
   */
  async getCodingActivity(params?: import('@/types/codingIntelligence').AnalyticsQueryParams): Promise<import('@/types/codingIntelligence').CodingActivityResponse> {
    const response = await apiClient.get<ApiResponse<import('@/types/codingIntelligence').CodingActivityResponse>>('/coding/analytics/activity', {
      params,
    });
    return response.data.data;
  },

  /**
   * Retrieves contest performance metrics
   */
  async getContestPerformance(params?: import('@/types/codingIntelligence').AnalyticsQueryParams): Promise<import('@/types/codingIntelligence').ContestPerformanceResponse> {
    const response = await apiClient.get<ApiResponse<import('@/types/codingIntelligence').ContestPerformanceResponse>>('/coding/analytics/contest-performance', {
      params,
    });
    return response.data.data;
  },

  /**
   * Retrieves factual data-backed performance insights
   */
  async getPerformanceInsights(params?: import('@/types/codingIntelligence').AnalyticsQueryParams): Promise<import('@/types/codingIntelligence').PerformanceInsightsResponse> {
    const response = await apiClient.get<ApiResponse<import('@/types/codingIntelligence').PerformanceInsightsResponse>>('/coding/analytics/insights', {
      params,
    });
    return response.data.data;
  },

  /**
   * Retrieves supported platforms metadata and capability flags
   */
  async getSupportedPlatforms(): Promise<import('@/types/codingIntelligence').PlatformMetadata[]> {
    const response = await apiClient.get<ApiResponse<import('@/types/codingIntelligence').PlatformMetadata[]>>('/coding/platforms');
    return response.data.data;
  },
};
