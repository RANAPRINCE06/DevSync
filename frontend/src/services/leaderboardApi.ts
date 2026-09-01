import { apiClient } from './client';
import { ApiResponse, PageResponse, PageParams } from '@/types/api';
import { LeaderboardEntry, LeaderboardPeriod } from '@/types/leaderboard';

export const leaderboardApi = {
  getTeamLeaderboard: async (
    teamId: string,
    period: LeaderboardPeriod = 'ALL_TIME',
    params?: PageParams
  ): Promise<PageResponse<LeaderboardEntry>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<LeaderboardEntry>>>(`/leaderboard/teams/${teamId}`, {
      params: { period, ...params },
    });
    return res.data.data;
  },

  getUserRanking: async (
    teamId: string,
    userId: string,
    period: LeaderboardPeriod = 'ALL_TIME'
  ): Promise<LeaderboardEntry> => {
    const res = await apiClient.get<ApiResponse<LeaderboardEntry>>(`/leaderboard/teams/${teamId}/me/${userId}`, {
      params: { period },
    });
    return res.data.data;
  },

  getLeaderboardForPeriod: async (
    teamId: string,
    period: LeaderboardPeriod,
    params?: PageParams
  ): Promise<PageResponse<LeaderboardEntry>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<LeaderboardEntry>>>(
      `/leaderboard/teams/${teamId}/period/${period}`,
      { params }
    );
    return res.data.data;
  },
};
