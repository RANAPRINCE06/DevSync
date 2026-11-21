import React from 'react';
import { CodingPlatform } from '@/types/codingIntelligence';
import { Filter, Calendar } from 'lucide-react';

interface AnalyticsHeaderProps {
  selectedPeriod: '7d' | '30d' | '90d' | '1y' | 'all';
  onPeriodChange: (period: '7d' | '30d' | '90d' | '1y' | 'all') => void;
  selectedPlatform: CodingPlatform | 'ALL';
  onPlatformChange: (platform: CodingPlatform | 'ALL') => void;
  availablePlatforms: CodingPlatform[];
}

export const AnalyticsHeader: React.FC<AnalyticsHeaderProps> = ({
  selectedPeriod,
  onPeriodChange,
  selectedPlatform,
  onPlatformChange,
  availablePlatforms,
}) => {
  const periods: { label: string; value: '7d' | '30d' | '90d' | '1y' | 'all' }[] = [
    { label: 'Last 7 Days', value: '7d' },
    { label: 'Last 30 Days', value: '30d' },
    { label: 'Last 90 Days', value: '90d' },
    { label: 'Past Year', value: '1y' },
    { label: 'All Time', value: 'all' },
  ];

  return (
    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-4 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm">
      <div className="flex items-center gap-2 flex-wrap">
        <div className="flex items-center gap-1.5 text-xs font-semibold text-surface-500 uppercase tracking-wider mr-1">
          <Filter className="w-3.5 h-3.5" />
          <span>Platform:</span>
        </div>
        <button
          onClick={() => onPlatformChange('ALL')}
          className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
            selectedPlatform === 'ALL'
              ? 'bg-primary-500 text-white shadow-sm'
              : 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700'
          }`}
        >
          All Connected
        </button>
        {availablePlatforms.map((p) => (
          <button
            key={p}
            onClick={() => onPlatformChange(p)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
              selectedPlatform === p
                ? 'bg-primary-500 text-white shadow-sm'
                : 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700'
            }`}
          >
            {p}
          </button>
        ))}
      </div>

      <div className="flex items-center gap-1.5 self-end sm:self-auto">
        <Calendar className="w-3.5 h-3.5 text-surface-400" />
        <div className="flex items-center bg-surface-100 dark:bg-surface-800 p-0.5 rounded-lg border border-surface-200/60 dark:border-surface-700/60">
          {periods.map((p) => (
            <button
              key={p.value}
              onClick={() => onPeriodChange(p.value)}
              className={`px-2.5 py-1 rounded-md text-xs font-medium transition-all ${
                selectedPeriod === p.value
                  ? 'bg-white dark:bg-surface-900 text-surface-900 dark:text-white shadow-xs font-semibold'
                  : 'text-surface-600 dark:text-surface-400 hover:text-surface-900 dark:hover:text-surface-200'
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
