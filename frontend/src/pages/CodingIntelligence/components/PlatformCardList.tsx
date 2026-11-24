import React from 'react';
import { CodingPlatform, PlatformConfig, PlatformAccount } from '@/types/codingIntelligence';
import { PlatformPerformanceCard } from './PlatformPerformanceCard';

interface PlatformCardListProps {
  platformConfigs: Record<CodingPlatform, PlatformConfig>;
  accounts: PlatformAccount[];
  syncingAccountId: string | null;
  disconnectingAccountId: string | null;
  activeHistoryAccountId: string | null;
  onConnect: (platform: CodingPlatform) => void;
  onDisconnect: (accountId: string) => void;
  onSync: (accountId: string) => void;
  onToggleHistory: (accountId: string) => void;
}

export const PlatformCardList: React.FC<PlatformCardListProps> = ({
  platformConfigs,
  accounts,
  syncingAccountId,
  disconnectingAccountId,
  activeHistoryAccountId,
  onConnect,
  onDisconnect,
  onSync,
  onToggleHistory,
}) => {
  const accountMap = new Map<CodingPlatform, PlatformAccount>(
    accounts.filter((a) => a.active).map((a) => [a.platform, a])
  );

  const platformKeys: CodingPlatform[] = [
    'LEETCODE',
    'CODEFORCES',
    'GITHUB',
    'ATCODER',
    'GEEKSFORGEEKS',
    'CODECHEF',
    'HACKERRANK',
  ];

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-bold text-surface-900 dark:text-white uppercase tracking-wider">
          Connected Platform Integrations
        </h3>
        <span className="text-xs text-surface-500">
          {accountMap.size} of {platformKeys.length} platforms linked
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {platformKeys.map((key) => {
          const config = platformConfigs[key];
          const account = accountMap.get(key);
          const isSyncing = syncingAccountId === account?.id;
          const isDisconnecting = disconnectingAccountId === account?.id;
          const isHistoryOpen = activeHistoryAccountId === account?.id;

          return (
            <PlatformPerformanceCard
              key={key}
              platformKey={key}
              config={config}
              account={account}
              isSyncing={isSyncing}
              isDisconnecting={isDisconnecting}
              isHistoryOpen={isHistoryOpen}
              onConnect={onConnect}
              onDisconnect={onDisconnect}
              onSync={onSync}
              onToggleHistory={onToggleHistory}
            />
          );
        })}
      </div>
    </div>
  );
};
