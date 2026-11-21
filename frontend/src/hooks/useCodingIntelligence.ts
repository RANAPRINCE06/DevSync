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
};

export const useConnectedAccounts = () => {
  return useQuery({
    queryKey: CODING_INTELLIGENCE_KEYS.accounts(),
    queryFn: () => codingIntelligenceService.getConnectedAccounts(),
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
