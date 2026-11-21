import React from 'react';
import { CodingActivityResponse } from '@/types/codingIntelligence';
import { Activity, Clock, ShieldAlert } from 'lucide-react';

interface CodingActivitySectionProps {
  activityData?: CodingActivityResponse;
  isLoading: boolean;
}

export const CodingActivitySection: React.FC<CodingActivitySectionProps> = ({
  activityData,
  isLoading,
}) => {
  if (isLoading) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm animate-pulse h-60 flex items-center justify-center">
        <div className="flex items-center gap-2 text-surface-500">
          <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Loading telemetry timeline...</span>
        </div>
      </div>
    );
  }

  const timeline = activityData?.snapshotTimeline || [];

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
            <Activity className="w-5 h-5 text-emerald-500" />
            Snapshot Telemetry & Activity Coverage
          </h3>
          <p className="text-xs text-surface-500">
            Recorded synchronization checkpoints across connected coding profiles
          </p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-300">
            {activityData?.activeDaysWithSnapshots ?? 0} Active Tracking Days
          </span>
        </div>
      </div>

      {/* Honest Data Model Notice */}
      <div className="p-3.5 rounded-xl bg-amber-50/60 dark:bg-amber-950/20 border border-amber-200/60 dark:border-amber-900/40 flex items-start gap-2.5 text-xs text-amber-800 dark:text-amber-300">
        <ShieldAlert className="w-4 h-4 shrink-0 mt-0.5 text-amber-600 dark:text-amber-400" />
        <div className="space-y-1">
          <p className="font-semibold text-[11px] uppercase tracking-wider">
            Daily Submission Activity Policy (No Fabricated Heatmaps)
          </p>
          <p className="text-[11px] leading-relaxed">
            {activityData?.coverageNotice ||
              'Current platform public adapters capture periodic aggregate snapshots. Granular daily submission heatmaps require authenticated platform OAuth tokens or webhook event ingestion.'}
          </p>
        </div>
      </div>

      {/* Summary Stats Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">Snapshots Recorded</span>
          <span className="text-lg font-bold text-surface-900 dark:text-white">
            {activityData?.totalSnapshotsInPeriod ?? 0}
          </span>
        </div>
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">Sync Cadence</span>
          <span className="text-xs font-semibold text-surface-700 dark:text-surface-300 block mt-1">
            Every 6 hours
          </span>
        </div>
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">First Checkpoint</span>
          <span className="text-xs font-mono text-surface-600 dark:text-surface-400 block mt-1 truncate">
            {activityData?.firstSnapshotAt ? new Date(activityData.firstSnapshotAt).toLocaleDateString() : '—'}
          </span>
        </div>
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">Latest Checkpoint</span>
          <span className="text-xs font-mono text-surface-600 dark:text-surface-400 block mt-1 truncate">
            {activityData?.lastSnapshotAt ? new Date(activityData.lastSnapshotAt).toLocaleDateString() : '—'}
          </span>
        </div>
      </div>

      {/* Recent Checkpoints Feed */}
      <div className="space-y-2 pt-2 border-t border-surface-100 dark:border-surface-800">
        <h4 className="text-xs font-semibold text-surface-700 dark:text-surface-300 uppercase tracking-wider flex items-center gap-1.5">
          <Clock className="w-3.5 h-3.5" />
          Recent Snapshot Telemetry Events
        </h4>

        {timeline.length === 0 ? (
          <p className="text-xs text-surface-400 py-3 text-center">No snapshots recorded during this date window.</p>
        ) : (
          <div className="max-h-56 overflow-y-auto space-y-1.5 pr-1">
            {timeline.slice(0, 10).map((item, idx) => (
              <div
                key={idx}
                className="p-2.5 rounded-lg border border-surface-200/50 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/30 flex items-center justify-between text-xs"
              >
                <div className="flex items-center gap-2.5">
                  <span className="font-mono font-bold text-surface-900 dark:text-white uppercase text-[11px]">
                    {item.platform}
                  </span>
                  <span className="text-[11px] text-surface-500">@{item.username}</span>
                </div>

                <div className="flex items-center gap-4">
                  {item.solvedChange !== null && item.solvedChange > 0 && (
                    <span className="text-emerald-600 dark:text-emerald-400 font-bold text-[11px]">
                      +{item.solvedChange} solved
                    </span>
                  )}
                  {item.ratingChange !== null && item.ratingChange !== 0 && (
                    <span
                      className={`text-[11px] font-bold ${
                        item.ratingChange > 0 ? 'text-indigo-600 dark:text-indigo-400' : 'text-red-500'
                      }`}
                    >
                      {item.ratingChange > 0 ? `+${item.ratingChange}` : item.ratingChange} rating
                    </span>
                  )}
                  <span className="text-[10px] text-surface-400 font-mono">
                    {new Date(item.timestamp).toLocaleString([], {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
