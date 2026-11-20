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
};
