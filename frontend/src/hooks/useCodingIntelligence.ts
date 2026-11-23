import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { codingIntelligenceService } from '@/services/codingIntelligenceService';
import { ConnectPlatformRequest } from '@/types/codingIntelligence';
import { useToast } from '@/contexts/ToastContext';

export const CODING_INTELLIGENCE_KEYS = {
  all: ['codingIntelligence'] as const,
  accounts: () => [...CODING_INTELLIGENCE_KEYS.all, 'accounts'] as const,
  statistics: () => [...CODING_INTELLIGENCE_KEYS.all, 'statistics'] as const,
  syncStatus: () => [...CODING_INTELLIGENCE_KEYS.all, 'syncStatus'] as const,
  history: (accountId: string) => [...CODING_INTELLIGENCE_KEYS.all, 'history', accountId] as const,
  analyticsOverview: () => [...CODING_INTELLIGENCE_KEYS.all, 'analytics', 'overview'] as const,
  ratingHistory: (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) =>
    [...CODING_INTELLIGENCE_KEYS.all, 'analytics', 'rating-history', params] as const,
  problemDistribution: (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) =>
    [...CODING_INTELLIGENCE_KEYS.all, 'analytics', 'problem-distribution', params] as const,
  codingActivity: (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) =>
    [...CODING_INTELLIGENCE_KEYS.all, 'analytics', 'activity', params] as const,
  contestPerformance: (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) =>
    [...CODING_INTELLIGENCE_KEYS.all, 'analytics', 'contest-performance', params] as const,
  performanceInsights: (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) =>
    [...CODING_INTELLIGENCE_KEYS.all, 'analytics', 'insights', params] as const,
  platforms: () => [...CODING_INTELLIGENCE_KEYS.all, 'platforms'] as const,
};

export const useConnectedAccounts = () => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.accounts(),
    queryFn: () => codingIntelligenceService.getConnectedAccounts(),
    refetchInterval: (query) => {
      const hasPending = query.state.data?.some((acc) => acc.syncStatus === 'PENDING');
      return hasPending ? 3000 : false;
    },
  });
};

export const useCodingStatistics = () => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.statistics(),
    queryFn: () => codingIntelligenceService.getAggregatedStatistics(),
  });
};

export const usePlatformHistory = (accountId: string | null) => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.history(accountId || ''),
    queryFn: () => codingIntelligenceService.getAccountHistory(accountId!),
    enabled: Boolean(accountId),
  });
};

export const usePreviewCodingProfile = () => {
  return useMutation({
    mutationFn: (profileUrl: string) => codingIntelligenceService.previewProfile(profileUrl),
  });
};

export const useConnectPlatform = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (request: ConnectPlatformRequest) =>
      codingIntelligenceService.connectAccount(request),
    onSuccess: (data) => {
      showToast('success', `Successfully linked ${data.platform} account (@${data.username})`);
      queryClient.invalidateQueries({ queryKey: CODING_INTELLIGENCE_KEYS.all });
    },
    onError: (error: Error) => {
      showToast('error', error.message || 'Failed to connect platform profile');
    },
  });
};

export const useDisconnectPlatform = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (accountId: string) =>
      codingIntelligenceService.disconnectAccount(accountId),
    onSuccess: () => {
      showToast('info', 'Platform account disconnected');
      queryClient.invalidateQueries({ queryKey: CODING_INTELLIGENCE_KEYS.all });
    },
    onError: (error: Error) => {
      showToast('error', error.message || 'Failed to disconnect account');
    },
  });
};

export const useSyncPlatform = () => {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: (accountId: string) =>
      codingIntelligenceService.syncAccount(accountId),
    onSuccess: (data) => {
      if (data.syncStatus === 'FAILED') {
        showToast('warning', `Sync failed for ${data.platform}: ${data.lastSyncError || 'Unknown error'}`);
      } else {
        showToast('success', `Successfully synchronized ${data.platform} (@${data.username})`);
      }
      queryClient.invalidateQueries({ queryKey: CODING_INTELLIGENCE_KEYS.all });
    },
    onError: (error: Error) => {
      showToast('error', error.message || 'Failed to synchronize platform profile');
    },
  });
};

export const useCodingAnalyticsOverview = () => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.analyticsOverview(),
    queryFn: () => codingIntelligenceService.getAnalyticsOverview(),
  });
};

export const useRatingHistory = (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.ratingHistory(params),
    queryFn: () => codingIntelligenceService.getRatingHistory(params),
  });
};

export const useProblemDistribution = (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.problemDistribution(params),
    queryFn: () => codingIntelligenceService.getProblemDistribution(params),
  });
};

export const useCodingActivity = (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.codingActivity(params),
    queryFn: () => codingIntelligenceService.getCodingActivity(params),
  });
};

export const useContestPerformance = (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.contestPerformance(params),
    queryFn: () => codingIntelligenceService.getContestPerformance(params),
  });
};

export const usePerformanceInsights = (params?: import('@/types/codingIntelligence').AnalyticsQueryParams) => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.performanceInsights(params),
    queryFn: () => codingIntelligenceService.getPerformanceInsights(params),
  });
};

export const useSupportedPlatforms = () => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.platforms(),
    queryFn: () => codingIntelligenceService.getSupportedPlatforms(),
    staleTime: 1000 * 60 * 60, // 1 hour
  });
};
