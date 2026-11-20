import React, { useState } from 'react';
import { PlatformStatSnapshot } from '@/types/codingIntelligence';
import { LineChart as ChartIcon, Sparkles } from 'lucide-react';

interface HistoricalStatsChartProps {
  snapshots?: PlatformStatSnapshot[];
  isLoading?: boolean;
  platformName?: string;
}

export const HistoricalStatsChart: React.FC<HistoricalStatsChartProps> = ({
  snapshots = [],
  isLoading = false,
  platformName = 'Platform',
}) => {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  if (isLoading) {
    return (
      <div className="h-64 flex items-center justify-center bg-surface-50 dark:bg-surface-900/40 rounded-xl border border-surface-200 dark:border-surface-800">
        <div className="flex items-center gap-2 text-surface-500">
          <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Loading historical statistics...</span>
        </div>
      </div>
    );
  }

  if (!snapshots || snapshots.length === 0) {
    return (
      <div className="h-64 flex flex-col items-center justify-center bg-surface-50 dark:bg-surface-900/30 rounded-xl border border-dashed border-surface-300 dark:border-surface-800 p-6 text-center">
        <div className="w-12 h-12 rounded-xl bg-surface-100 dark:bg-surface-800 flex items-center justify-center text-surface-400 mb-3">
          <ChartIcon className="w-6 h-6" />
        </div>
        <h4 className="text-sm font-semibold text-surface-900 dark:text-surface-100 mb-1">
          No Historical Snapshot Data Yet
        </h4>
        <p className="text-xs text-surface-500 max-w-sm">
          Snapshots will be automatically aggregated and plotted once synchronized in Step 5B.
        </p>
      </div>
    );
  }

  // Extract rating and problems solved data
  const dataPoints = snapshots.map((s, idx) => ({
    id: s.id || `pt-${idx}`,
    date: new Date(s.snapshotAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
    rating: s.currentRating ?? null,
    solved: s.problemsSolved ?? null,
  }));

  const ratings = dataPoints.map((d) => d.rating).filter((r): r is number => r !== null);
  const minRating = ratings.length > 0 ? Math.min(...ratings) : 1000;
  const maxRating = ratings.length > 0 ? Math.max(...ratings) : 2000;
  const ratingRange = Math.max(maxRating - minRating, 100);

  // SVG dimensions
  const width = 600;
  const height = 200;
  const paddingX = 40;
  const paddingY = 30;
  const innerWidth = width - paddingX * 2;
  const innerHeight = height - paddingY * 2;

  // Calculate coordinates for rating line
  const points = dataPoints.map((d, i) => {
    const x = paddingX + (dataPoints.length === 1 ? innerWidth / 2 : (i / (dataPoints.length - 1)) * innerWidth);
    const yVal = d.rating !== null ? d.rating : minRating;
    const y = height - paddingY - ((yVal - minRating) / ratingRange) * innerHeight;
    return { x, y, ...d };
  });

  const polylinePoints = points.map((p) => `${p.x},${p.y}`).join(' ');

  return (
    <div className="bg-surface-50 dark:bg-surface-900/40 p-5 rounded-xl border border-surface-200 dark:border-surface-800">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h4 className="text-sm font-semibold text-surface-900 dark:text-surface-100 flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-primary-500" />
            {platformName} Performance History
          </h4>
          <p className="text-xs text-surface-500">
            Historical rating progression and snapshot timeline
          </p>
        </div>
        <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-primary-500/10 text-primary-600 dark:text-primary-400">
          {snapshots.length} Snapshot{snapshots.length === 1 ? '' : 's'}
        </span>
      </div>

      <div className="relative w-full overflow-hidden">
        <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-48 select-none">
          {/* Grid lines */}
          <line
            x1={paddingX}
            y1={paddingY}
            x2={width - paddingX}
            y2={paddingY}
            className="stroke-surface-200 dark:stroke-surface-800"
            strokeDasharray="4 4"
          />
          <line
            x1={paddingX}
            y1={height / 2}
            x2={width - paddingX}
            y2={height / 2}
            className="stroke-surface-200 dark:stroke-surface-800"
            strokeDasharray="4 4"
          />
          <line
            x1={paddingX}
            y1={height - paddingY}
            x2={width - paddingX}
            y2={height - paddingY}
            className="stroke-surface-200 dark:stroke-surface-800"
          />

          {/* Rating Line */}
          {ratings.length > 1 && (
            <polyline
              fill="none"
              stroke="#6366f1"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              points={polylinePoints}
            />
          )}

          {/* Data Points */}
          {points.map((p, idx) => (
            <g key={p.id}>
              <circle
                cx={p.x}
                cy={p.y}
                r={hoveredIndex === idx ? 6 : 4}
                className="fill-primary-500 stroke-white dark:stroke-surface-900 transition-all cursor-pointer"
                strokeWidth="2"
                onMouseEnter={() => setHoveredIndex(idx)}
                onMouseLeave={() => setHoveredIndex(null)}
              />
              <text
                x={p.x}
                y={height - 10}
                textAnchor="middle"
                fontSize="10"
                className="fill-surface-400 font-medium"
              >
                {p.date}
              </text>
            </g>
          ))}
        </svg>

        {/* Hover Tooltip */}
        {hoveredIndex !== null && points[hoveredIndex] && (
          <div
            className="absolute top-2 left-1/2 -translate-x-1/2 bg-surface-900/95 dark:bg-surface-800/95 text-white text-xs px-3 py-1.5 rounded-lg shadow-lg border border-surface-700 pointer-events-none flex items-center gap-3 animate-in fade-in duration-150"
          >
            <span>Date: <strong>{points[hoveredIndex].date}</strong></span>
            {points[hoveredIndex].rating !== null && (
              <span>Rating: <strong className="text-primary-400">{points[hoveredIndex].rating}</strong></span>
            )}
            {points[hoveredIndex].solved !== null && (
              <span>Solved: <strong className="text-emerald-400">{points[hoveredIndex].solved}</strong></span>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
