import { useQuery } from '@tanstack/react-query';
import { leaderboardApi } from '@/services/leaderboardApi';
import { LeaderboardPeriod } from '@/types/leaderboard';
import { PageParams } from '@/types/api';

export const useLeaderboard = (
  teamId?: string,
  period: LeaderboardPeriod = 'ALL_TIME',
  params?: PageParams
) => {
  return useQuery({
    queryKey: ['leaderboard', teamId, period, params],
    queryFn: () =>
      teamId ? leaderboardApi.getTeamLeaderboard(teamId, period, params) : Promise.reject('No team ID'),
    enabled: !!teamId,
  });
};

export const useUserRanking = (
  teamId?: string,
  userId?: string,
  period: LeaderboardPeriod = 'ALL_TIME'
) => {
  return useQuery({
    queryKey: ['leaderboard', 'user', teamId, userId, period],
    queryFn: () =>
      teamId && userId
        ? leaderboardApi.getUserRanking(teamId, userId, period)
        : Promise.reject('Missing team or user ID'),
    enabled: !!teamId && !!userId,
  });
};
