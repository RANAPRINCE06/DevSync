import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import { Achievement, CreateAchievementRequest, UpdateAchievementRequest, AchievementFilterParams } from '@/types/achievement';

export const achievementApi = {
  getAchievements: async (params?: AchievementFilterParams): Promise<PageResponse<Achievement>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<Achievement>>>('/achievements', { params });
    return res.data.data;
  },

  getAchievementById: async (id: string): Promise<Achievement> => {
    const res = await apiClient.get<ApiResponse<Achievement>>(`/achievements/${id}`);
    return res.data.data;
  },

  getUserAchievements: async (userId: string): Promise<Achievement[]> => {
    const res = await apiClient.get<ApiResponse<Achievement[]>>(`/achievements/user/${userId}`);
    return res.data.data;
  },

  getUserTotalPoints: async (userId: string): Promise<{ totalPoints: number }> => {
    const res = await apiClient.get<ApiResponse<{ totalPoints: number }>>(`/achievements/user/${userId}/points`);
    return res.data.data;
  },

  createAchievement: async (data: CreateAchievementRequest): Promise<Achievement> => {
    const res = await apiClient.post<ApiResponse<Achievement>>('/achievements', data);
    return res.data.data;
  },

  updateAchievement: async (id: string, data: UpdateAchievementRequest): Promise<Achievement> => {
    const res = await apiClient.put<ApiResponse<Achievement>>(`/achievements/${id}`, data);
    return res.data.data;
  },

  deleteAchievement: async (id: string): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/achievements/${id}`);
  },
};
