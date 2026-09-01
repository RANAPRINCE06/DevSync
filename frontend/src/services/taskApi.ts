import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import { Task, CreateTaskRequest, UpdateTaskRequest, TaskFilterParams } from '@/types/task';

export const taskApi = {
  getTasks: async (params?: TaskFilterParams): Promise<PageResponse<Task>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<Task>>>('/tasks', { params });
    return res.data.data;
  },

  getTaskById: async (id: string): Promise<Task> => {
    const res = await apiClient.get<ApiResponse<Task>>(`/tasks/${id}`);
    return res.data.data;
  },

  createTask: async (data: CreateTaskRequest): Promise<Task> => {
    const res = await apiClient.post<ApiResponse<Task>>('/tasks', data);
    return res.data.data;
  },

  updateTask: async (id: string, data: UpdateTaskRequest): Promise<Task> => {
    const res = await apiClient.put<ApiResponse<Task>>(`/tasks/${id}`, data);
    return res.data.data;
  },

  deleteTask: async (id: string): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/tasks/${id}`);
  },
};
