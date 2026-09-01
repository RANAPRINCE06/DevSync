import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { codingProfileApi } from '@/services/codingProfileApi';
import { CreateCodingProfileRequest, UpdateCodingProfileRequest, CodingProfileFilterParams } from '@/types/codingProfile';

export const useCodingProfiles = (params?: CodingProfileFilterParams) => {
  return useQuery({
    queryKey: ['codingProfiles', params],
    queryFn: () => codingProfileApi.getCodingProfiles(params),
  });
};

export const useCodingProfile = (id?: string) => {
  return useQuery({
    queryKey: ['codingProfiles', id],
    queryFn: () => (id ? codingProfileApi.getCodingProfileById(id) : Promise.reject('No profile ID')),
    enabled: !!id,
  });
};

export const useUserCodingProfiles = (userId?: string) => {
  return useQuery({
    queryKey: ['codingProfiles', 'user', userId],
    queryFn: () => (userId ? codingProfileApi.getUserCodingProfiles(userId) : Promise.resolve([])),
    enabled: !!userId,
  });
};

export const useCreateCodingProfile = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCodingProfileRequest) => codingProfileApi.createCodingProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['codingProfiles'] });
    },
  });
};

export const useUpdateCodingProfile = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateCodingProfileRequest }) =>
      codingProfileApi.updateCodingProfile(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['codingProfiles'] });
    },
  });
};
