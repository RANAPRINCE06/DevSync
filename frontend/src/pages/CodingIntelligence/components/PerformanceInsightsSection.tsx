import React from 'react';
import { PerformanceInsightsResponse } from '@/types/codingIntelligence';
import { Lightbulb, TrendingUp, AlertTriangle, Info, CheckCircle2 } from 'lucide-react';

interface PerformanceInsightsSectionProps {
  insightsData?: PerformanceInsightsResponse;
  isLoading: boolean;
}

export const PerformanceInsightsSection: React.FC<PerformanceInsightsSectionProps> = ({
  insightsData,
  isLoading,
}) => {
  if (isLoading) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm animate-pulse space-y-3">
        <div className="h-5 bg-surface-200 dark:bg-surface-800 rounded w-1/4"></div>
        <div className="h-16 bg-surface-100 dark:bg-surface-800/60 rounded"></div>
        <div className="h-16 bg-surface-100 dark:bg-surface-800/60 rounded"></div>
      </div>
    );
  }

  const insights = insightsData?.insights || [];

  if (insights.length === 0) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm text-center">
        <Lightbulb className="w-8 h-8 text-surface-400 mx-auto mb-2 opacity-60" />
        <p className="text-sm font-semibold text-surface-900 dark:text-surface-100">No Insights Available Yet</p>
        <p className="text-xs text-surface-500 mt-1 max-w-sm mx-auto">
          Insights are generated automatically once at least two historical snapshots have been synchronized across your connected platforms.
        </p>
      </div>
    );
  }

  const getIcon = (type: string) => {
    switch (type) {
      case 'POSITIVE':
        return <TrendingUp className="w-4 h-4 text-emerald-500 shrink-0" />;
      case 'ATTENTION':
        return <AlertTriangle className="w-4 h-4 text-amber-500 shrink-0" />;
      case 'INFORMATIONAL':
        return <Info className="w-4 h-4 text-blue-500 shrink-0" />;
      default:
        return <CheckCircle2 className="w-4 h-4 text-primary-500 shrink-0" />;
    }
  };

  const getBadgeStyle = (type: string) => {
    switch (type) {
      case 'POSITIVE':
        return 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20';
      case 'ATTENTION':
        return 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20';
      case 'INFORMATIONAL':
        return 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20';
      default:
        return 'bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 border-surface-200';
    }
  };

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-amber-500/10 text-amber-500">
            <Lightbulb className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-bold text-surface-900 dark:text-white">
              Data-Backed Performance Insights
            </h3>
            <p className="text-xs text-surface-500">
              Factual trends derived strictly from persisted historical snapshots
            </p>
          </div>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-300">
          {insights.length} Insight{insights.length === 1 ? '' : 's'}
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {insights.map((item) => (
          <div
            key={item.id}
            className="p-3.5 rounded-xl border border-surface-200/80 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/40 flex items-start gap-3 hover:border-surface-300 dark:hover:border-surface-700 transition-colors"
          >
            {getIcon(item.type)}
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between gap-2 mb-1">
                <span className="text-xs font-semibold text-surface-900 dark:text-surface-100 truncate">
                  {item.title}
                </span>
                {item.platform && (
                  <span className="text-[10px] font-mono uppercase px-1.5 py-0.5 rounded bg-surface-200/70 dark:bg-surface-800 text-surface-600 dark:text-surface-300 shrink-0">
                    {item.platform}
                  </span>
                )}
              </div>
              <p className="text-xs text-surface-600 dark:text-surface-400 leading-relaxed">
                {item.message}
              </p>
            </div>
            {item.metricValue && (
              <span
                className={`text-xs font-bold px-2 py-0.5 rounded-md border shrink-0 ${getBadgeStyle(
                  item.type
                )}`}
              >
                {item.metricValue}
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
