import { apiClient } from './client';
import { ApiResponse, PageResponse, PageParams } from '@/types/api';
import { User, CreateUserRequest, UpdateUserRequest } from '@/types/user';

export const userApi = {
  getUsers: async (params?: PageParams): Promise<PageResponse<User>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<User>>>('/users', { params });
    return res.data.data;
  },

  getUserById: async (id: string): Promise<User> => {
    const res = await apiClient.get<ApiResponse<User>>(`/users/${id}`);
    return res.data.data;
  },

  createUser: async (data: CreateUserRequest): Promise<User> => {
    const res = await apiClient.post<ApiResponse<User>>('/users', data);
    return res.data.data;
  },

  updateUser: async (id: string, data: UpdateUserRequest): Promise<User> => {
    const res = await apiClient.put<ApiResponse<User>>(`/users/${id}`, data);
    return res.data.data;
  },
};
