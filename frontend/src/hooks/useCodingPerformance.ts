import { useQuery } from '@tanstack/react-query';
import { codingPerformanceService } from '@/services/codingPerformanceService';
import { CodingPlatform } from '@/types/codingIntelligence';

export const CODING_PERFORMANCE_KEYS = {
  all: ['codingPerformance'] as const,
  achievements: () => [...CODING_PERFORMANCE_KEYS.all, 'achievements'] as const,
  allContests: () => [...CODING_PERFORMANCE_KEYS.all, 'contests'] as const,
  contestByPlatform: (platform: CodingPlatform) => [...CODING_PERFORMANCE_KEYS.all, 'contests', platform] as const,
  githubContributions: () => [...CODING_PERFORMANCE_KEYS.all, 'github', 'contributions'] as const,
  githubCommits: (period: string) => [...CODING_PERFORMANCE_KEYS.all, 'github', 'commits', period] as const,
  githubRepositories: () => [...CODING_PERFORMANCE_KEYS.all, 'github', 'repositories'] as const,
};

export function useAchievements() {
  return useQuery({
    queryKey: CODING_PERFORMANCE_KEYS.achievements(),
    queryFn: () => codingPerformanceService.getAchievements(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useAllContestHistories() {
  return useQuery({
    queryKey: CODING_PERFORMANCE_KEYS.allContests(),
    queryFn: () => codingPerformanceService.getAllContestHistories(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useContestHistory(platform: CodingPlatform) {
  return useQuery({
    queryKey: CODING_PERFORMANCE_KEYS.contestByPlatform(platform),
    queryFn: () => codingPerformanceService.getContestHistory(platform),
    staleTime: 5 * 60 * 1000,
  });
}

export function useGitHubContributions() {
  return useQuery({
    queryKey: CODING_PERFORMANCE_KEYS.githubContributions(),
    queryFn: () => codingPerformanceService.getGitHubContributions(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useGitHubCommitActivity(period: string = '90d') {
  return useQuery({
    queryKey: CODING_PERFORMANCE_KEYS.githubCommits(period),
    queryFn: () => codingPerformanceService.getGitHubCommitActivity(period),
    staleTime: 5 * 60 * 1000,
  });
}

export function useGitHubRepositories() {
  return useQuery({
    queryKey: CODING_PERFORMANCE_KEYS.githubRepositories(),
    queryFn: () => codingPerformanceService.getGitHubRepositories(),
    staleTime: 5 * 60 * 1000,
  });
}
