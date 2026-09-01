import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { progressApi } from '@/services/progressApi';
import { CreateDailyProgressRequest, UpdateDailyProgressRequest, ProgressFilterParams } from '@/types/progress';

export const useProgressList = (params?: ProgressFilterParams) => {
  return useQuery({
    queryKey: ['progress', params],
    queryFn: () => progressApi.getProgressList(params),
  });
};

export const useProgress = (id?: string) => {
  return useQuery({
    queryKey: ['progress', id],
    queryFn: () => (id ? progressApi.getProgressById(id) : Promise.reject('No progress ID')),
    enabled: !!id,
  });
};

export const useCreateProgress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateDailyProgressRequest) => progressApi.createProgress(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['progress'] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};

export const useUpdateProgress = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateDailyProgressRequest }) =>
      progressApi.updateProgress(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['progress'] });
      queryClient.invalidateQueries({ queryKey: ['progress', variables.id] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};
