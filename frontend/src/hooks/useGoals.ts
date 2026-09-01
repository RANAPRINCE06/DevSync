import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { goalApi } from '@/services/goalApi';
import { CreateGoalRequest, UpdateGoalRequest, GoalFilterParams } from '@/types/goal';

export const useGoals = (params?: GoalFilterParams) => {
  return useQuery({
    queryKey: ['goals', params],
    queryFn: () => goalApi.getGoals(params),
  });
};

export const useGoal = (id?: string) => {
  return useQuery({
    queryKey: ['goals', id],
    queryFn: () => (id ? goalApi.getGoalById(id) : Promise.reject('No goal ID')),
    enabled: !!id,
  });
};

export const useCreateGoal = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateGoalRequest) => goalApi.createGoal(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};

export const useUpdateGoal = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateGoalRequest }) => goalApi.updateGoal(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['goals'] });
      queryClient.invalidateQueries({ queryKey: ['goals', variables.id] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};

export const useDeleteGoal = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => goalApi.deleteGoal(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals'] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};
