import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import { Goal, CreateGoalRequest, UpdateGoalRequest, GoalFilterParams } from '@/types/goal';

export const goalApi = {
  getGoals: async (params?: GoalFilterParams): Promise<PageResponse<Goal>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<Goal>>>('/goals', { params });
    return res.data.data;
  },

  getGoalById: async (id: string): Promise<Goal> => {
    const res = await apiClient.get<ApiResponse<Goal>>(`/goals/${id}`);
    return res.data.data;
  },

  createGoal: async (data: CreateGoalRequest): Promise<Goal> => {
    const res = await apiClient.post<ApiResponse<Goal>>('/goals', data);
    return res.data.data;
  },

  updateGoal: async (id: string, data: UpdateGoalRequest): Promise<Goal> => {
    const res = await apiClient.put<ApiResponse<Goal>>(`/goals/${id}`, data);
    return res.data.data;
  },

  deleteGoal: async (id: string): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/goals/${id}`);
  },
};
