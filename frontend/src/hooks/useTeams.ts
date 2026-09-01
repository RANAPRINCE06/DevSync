import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { teamApi } from '@/services/teamApi';
import { CreateTeamRequest, UpdateTeamRequest } from '@/types/team';
import { PageParams } from '@/types/api';

export const useTeams = (params?: PageParams) => {
  return useQuery({
    queryKey: ['teams', params],
    queryFn: () => teamApi.getTeams(params),
  });
};

export const useTeam = (id?: string) => {
  return useQuery({
    queryKey: ['teams', id],
    queryFn: () => (id ? teamApi.getTeamById(id) : Promise.reject('No team ID')),
    enabled: !!id,
  });
};

export const useTeamMembers = (teamId?: string, params?: PageParams) => {
  return useQuery({
    queryKey: ['teams', teamId, 'members', params],
    queryFn: () => (teamId ? teamApi.getTeamMembers(teamId, params) : Promise.reject('No team ID')),
    enabled: !!teamId,
  });
};

export const useCreateTeam = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTeamRequest) => teamApi.createTeam(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teams'] });
    },
  });
};

export const useUpdateTeam = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateTeamRequest }) => teamApi.updateTeam(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['teams'] });
      queryClient.invalidateQueries({ queryKey: ['teams', variables.id] });
    },
  });
};

export const useAddTeamMember = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ teamId, userId }: { teamId: string; userId: string }) => teamApi.addTeamMember(teamId, userId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['teams', variables.teamId, 'members'] });
    },
  });
};
