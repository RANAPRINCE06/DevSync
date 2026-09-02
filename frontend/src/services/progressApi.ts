import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import {
  DailyProgress,
  CreateDailyProgressRequest,
  UpdateDailyProgressRequest,
  ProgressFilterParams,
} from '@/types/progress';

export const progressApi = {
  getProgressList: async (params?: ProgressFilterParams): Promise<PageResponse<DailyProgress>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<DailyProgress>>>('/progress', { params });
    return res.data.data;
  },

  getProgressById: async (id: string): Promise<DailyProgress> => {
    const res = await apiClient.get<ApiResponse<DailyProgress>>(`/progress/${id}`);
    return res.data.data;
  },

  createProgress: async (data: CreateDailyProgressRequest): Promise<DailyProgress> => {
    const payload = {
      ...data,
      completed: data.completed || data.whatCompleted || data.whatStudied || 'In progress',
      challenges: data.challenges || data.blockers || undefined,
    };
    const res = await apiClient.post<ApiResponse<DailyProgress>>('/progress', payload);
    return res.data.data;
  },

  updateProgress: async (id: string, data: UpdateDailyProgressRequest): Promise<DailyProgress> => {
    const payload = {
      ...data,
      completed: data.completed || data.whatCompleted || data.whatStudied || 'In progress',
      challenges: data.challenges || data.blockers || undefined,
    };
    const res = await apiClient.put<ApiResponse<DailyProgress>>(`/progress/${id}`, payload);
    return res.data.data;
  },
};
