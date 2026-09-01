import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import { CodingProfile, CreateCodingProfileRequest, UpdateCodingProfileRequest, CodingProfileFilterParams } from '@/types/codingProfile';

export const codingProfileApi = {
  getCodingProfiles: async (params?: CodingProfileFilterParams): Promise<PageResponse<CodingProfile>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<CodingProfile>>>('/coding-profiles', { params });
    return res.data.data;
  },

  getCodingProfileById: async (id: string): Promise<CodingProfile> => {
    const res = await apiClient.get<ApiResponse<CodingProfile>>(`/coding-profiles/${id}`);
    return res.data.data;
  },

  getUserCodingProfiles: async (userId: string): Promise<CodingProfile[]> => {
    const res = await apiClient.get<ApiResponse<CodingProfile[]>>(`/coding-profiles/user/${userId}`);
    return res.data.data;
  },

  createCodingProfile: async (data: CreateCodingProfileRequest): Promise<CodingProfile> => {
    const res = await apiClient.post<ApiResponse<CodingProfile>>('/coding-profiles', data);
    return res.data.data;
  },

  updateCodingProfile: async (id: string, data: UpdateCodingProfileRequest): Promise<CodingProfile> => {
    const res = await apiClient.put<ApiResponse<CodingProfile>>(`/coding-profiles/${id}`, data);
    return res.data.data;
  },

  deleteCodingProfile: async (id: string): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/coding-profiles/${id}`);
  },
};
