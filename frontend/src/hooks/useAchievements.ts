import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { achievementApi } from '@/services/achievementApi';
import { CreateAchievementRequest, UpdateAchievementRequest, AchievementFilterParams } from '@/types/achievement';

export const useAchievements = (params?: AchievementFilterParams) => {
  return useQuery({
    queryKey: ['achievements', params],
    queryFn: () => achievementApi.getAchievements(params),
  });
};

export const useAchievement = (id?: string) => {
  return useQuery({
    queryKey: ['achievements', id],
    queryFn: () => (id ? achievementApi.getAchievementById(id) : Promise.reject('No achievement ID')),
    enabled: !!id,
  });
};

export const useUserAchievements = (userId?: string) => {
  return useQuery({
    queryKey: ['achievements', 'user', userId],
    queryFn: () => (userId ? achievementApi.getUserAchievements(userId) : Promise.resolve([])),
    enabled: !!userId,
  });
};

export const useUserTotalPoints = (userId?: string) => {
  return useQuery({
    queryKey: ['achievements', 'user', userId, 'points'],
    queryFn: () => (userId ? achievementApi.getUserTotalPoints(userId) : Promise.resolve({ totalPoints: 0 })),
    enabled: !!userId,
  });
};

export const useCreateAchievement = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateAchievementRequest) => achievementApi.createAchievement(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['achievements'] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};

export const useUpdateAchievement = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateAchievementRequest }) =>
      achievementApi.updateAchievement(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['achievements'] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};
