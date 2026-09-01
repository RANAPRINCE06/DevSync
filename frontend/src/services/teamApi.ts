import { apiClient } from './client';
import { ApiResponse, PageResponse, PageParams } from '@/types/api';
import { Team, TeamMember, CreateTeamRequest, UpdateTeamRequest } from '@/types/team';

export const teamApi = {
  getTeams: async (params?: PageParams): Promise<PageResponse<Team>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<Team>>>('/teams', { params });
    return res.data.data;
  },

  getTeamById: async (id: string): Promise<Team> => {
    const res = await apiClient.get<ApiResponse<Team>>(`/teams/${id}`);
    return res.data.data;
  },

  createTeam: async (data: CreateTeamRequest): Promise<Team> => {
    const res = await apiClient.post<ApiResponse<Team>>('/teams', data);
    return res.data.data;
  },

  updateTeam: async (id: string, data: UpdateTeamRequest): Promise<Team> => {
    const res = await apiClient.put<ApiResponse<Team>>(`/teams/${id}`, data);
    return res.data.data;
  },

  addTeamMember: async (teamId: string, userId: string): Promise<TeamMember> => {
    const res = await apiClient.post<ApiResponse<TeamMember>>(`/teams/${teamId}/members/${userId}`);
    return res.data.data;
  },

  getTeamMembers: async (teamId: string, params?: PageParams): Promise<PageResponse<TeamMember>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<TeamMember>>>(`/teams/${teamId}/members`, { params });
    return res.data.data;
  },
};
