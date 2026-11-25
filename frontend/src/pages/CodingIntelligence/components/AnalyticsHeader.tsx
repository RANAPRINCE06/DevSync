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
    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-3.5 rounded-[3px] bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 shadow-xs">
      <div className="flex items-center gap-1.5 flex-wrap">
        <div className="flex items-center gap-1.5 text-[11px] font-bold text-slate-500 uppercase tracking-wider mr-1">
          <Filter className="w-3.5 h-3.5" />
          <span>Platform:</span>
        </div>
        <button
          onClick={() => onPlatformChange('ALL')}
          className={`px-2.5 py-1 rounded-[2px] text-xs font-semibold transition-colors ${
            selectedPlatform === 'ALL'
              ? 'bg-[#1d4ed8] text-white shadow-2xs'
              : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700'
          }`}
        >
          All Connected
        </button>
        {availablePlatforms.map((p) => (
          <button
            key={p}
            onClick={() => onPlatformChange(p)}
            className={`px-2.5 py-1 rounded-[2px] text-xs font-semibold transition-colors ${
              selectedPlatform === p
                ? 'bg-[#1d4ed8] text-white shadow-2xs'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700'
            }`}
          >
            {p}
          </button>
        ))}
      </div>

      <div className="flex items-center gap-1.5 self-end sm:self-auto">
        <Calendar className="w-3.5 h-3.5 text-slate-400" />
        <div className="flex items-center bg-slate-100 dark:bg-slate-800 p-0.5 rounded-[3px] border border-slate-200 dark:border-slate-700">
          {periods.map((p) => (
            <button
              key={p.value}
              onClick={() => onPeriodChange(p.value)}
              className={`px-2 py-0.5 rounded-[2px] text-[11px] font-semibold transition-all ${
                selectedPeriod === p.value
                  ? 'bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 shadow-2xs'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
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
