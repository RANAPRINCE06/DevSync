import React, { useState } from 'react';
import { RatingHistoryResponse } from '@/types/codingIntelligence';
import { LineChart as ChartIcon, Trophy, Award, TrendingUp, TrendingDown, Minus } from 'lucide-react';

interface RatingProgressionSectionProps {
  ratingData?: RatingHistoryResponse;
  isLoading: boolean;
}

export const RatingProgressionSection: React.FC<RatingProgressionSectionProps> = ({
  ratingData,
  isLoading,
}) => {
  const [selectedPlatformIndex, setSelectedPlatformIndex] = useState<number>(0);
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  if (isLoading) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm h-72 flex items-center justify-center">
        <div className="flex items-center gap-2 text-surface-500">
          <div className="w-5 h-5 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm">Loading rating histories...</span>
        </div>
      </div>
    );
  }

  const platforms = ratingData?.platforms || [];
  const activeSeries = platforms[selectedPlatformIndex] || platforms[0];

  if (!platforms.length || !activeSeries) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm text-center py-12">
        <ChartIcon className="w-10 h-10 text-surface-400 mx-auto mb-2 opacity-50" />
        <h4 className="text-sm font-semibold text-surface-900 dark:text-surface-100">No Rating History Available</h4>
        <p className="text-xs text-surface-500 mt-1 max-w-sm mx-auto">
          Connect rated competitive platforms (Codeforces, LeetCode, CodeChef, or AtCoder) to track contest rating progressions over time.
        </p>
      </div>
    );
  }

  const history = activeSeries.history || [];
  const ratings = history.map((h) => h.rating).filter((r): r is number => r !== null);
  const minRating = ratings.length > 0 ? Math.floor(Math.min(...ratings) / 50) * 50 : 1000;
  const maxRating = ratings.length > 0 ? Math.ceil(Math.max(...ratings) / 50) * 50 : 2000;
  const ratingRange = Math.max(maxRating - minRating, 100);

  // SVG dimensions
  const width = 700;
  const height = 220;
  const paddingX = 45;
  const paddingY = 25;
  const innerWidth = width - paddingX * 2;
  const innerHeight = height - paddingY * 2;

  const points = history.map((item, i) => {
    const x =
      paddingX + (history.length === 1 ? innerWidth / 2 : (i / (history.length - 1)) * innerWidth);
    const yVal = item.rating;
    const y = height - paddingY - ((yVal - minRating) / ratingRange) * innerHeight;
    return { x, y, ...item };
  });

  const polylineStr = points.map((p) => `${p.x},${p.y}`).join(' ');

  const getDeltaBadge = (change: number | null) => {
    if (change === null) {
      return (
        <span className="flex items-center gap-1 text-xs text-surface-500">
          <Minus className="w-3.5 h-3.5" /> Baseline
        </span>
      );
    }
    if (change > 0) {
      return (
        <span className="flex items-center gap-1 text-xs font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-md">
          <TrendingUp className="w-3.5 h-3.5" /> +{change} pts
        </span>
      );
    }
    if (change < 0) {
      return (
        <span className="flex items-center gap-1 text-xs font-bold text-red-600 dark:text-red-400 bg-red-500/10 px-2 py-0.5 rounded-md">
          <TrendingDown className="w-3.5 h-3.5" /> {change} pts
        </span>
      );
    }
    return (
      <span className="flex items-center gap-1 text-xs text-surface-500 bg-surface-100 dark:bg-surface-800 px-2 py-0.5 rounded-md">
        <Minus className="w-3.5 h-3.5" /> 0 pts
      </span>
    );
  };

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
            <Trophy className="w-5 h-5 text-indigo-500" />
            Competitive Rating Progression
          </h3>
          <p className="text-xs text-surface-500">
            Independent platform rating curves. Platforms are never artificially combined.
          </p>
        </div>

        {/* Platform switcher tabs */}
        <div className="flex items-center gap-1 bg-surface-100 dark:bg-surface-800 p-1 rounded-xl">
          {platforms.map((s, idx) => (
            <button
              key={s.platform}
              onClick={() => {
                setSelectedPlatformIndex(idx);
                setHoveredIndex(null);
              }}
              className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all ${
                selectedPlatformIndex === idx
                  ? 'bg-white dark:bg-surface-900 text-surface-900 dark:text-white shadow-xs'
                  : 'text-surface-500 hover:text-surface-900 dark:hover:text-surface-200'
              }`}
            >
              {s.platform}
            </button>
          ))}
        </div>
      </div>

      {/* Metric Cards Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-1">
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">Current Rating</span>
          <span className="text-xl font-bold text-surface-900 dark:text-white">
            {activeSeries.currentRating !== null ? Math.round(activeSeries.currentRating) : '—'}
          </span>
        </div>
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">Personal Best</span>
          <span className="text-xl font-bold text-indigo-600 dark:text-indigo-400 flex items-center gap-1">
            <Award className="w-4 h-4 text-amber-500" />
            {activeSeries.personalBestRating !== null ? Math.round(activeSeries.personalBestRating) : '—'}
          </span>
        </div>
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">Period Change</span>
          <div className="mt-0.5">{getDeltaBadge(activeSeries.ratingChange)}</div>
        </div>
        <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-900/50 border border-surface-200/60 dark:border-surface-800">
          <span className="text-[11px] text-surface-500 uppercase font-semibold block">Snapshots in Range</span>
          <span className="text-xl font-bold text-surface-900 dark:text-white">
            {history.length}
          </span>
        </div>
      </div>

      {/* SVG Line Chart */}
      {history.length === 0 ? (
        <div className="h-52 flex items-center justify-center bg-surface-50 dark:bg-surface-900/30 rounded-xl border border-dashed border-surface-200 dark:border-surface-800">
          <p className="text-xs text-surface-500">No rating data points found in the selected date range for {activeSeries.platform}.</p>
        </div>
      ) : (
        <div className="bg-surface-50 dark:bg-surface-900/40 p-4 rounded-xl border border-surface-200/60 dark:border-surface-800 relative">
          <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-52 overflow-visible">
            {/* Grid lines */}
            {[minRating, Math.round((minRating + maxRating) / 2), maxRating].map((val) => {
              const y = height - paddingY - ((val - minRating) / ratingRange) * innerHeight;
              return (
                <g key={val}>
                  <line
                    x1={paddingX}
                    y1={y}
                    x2={width - paddingX}
                    y2={y}
                    stroke="currentColor"
                    className="text-surface-200 dark:text-surface-800 stroke-[1]"
                    strokeDasharray="4 4"
                  />
                  <text
                    x={paddingX - 8}
                    y={y + 3}
                    textAnchor="end"
                    className="text-[10px] fill-surface-400 font-mono"
                  >
                    {val}
                  </text>
                </g>
              );
            })}

            {/* Polyline area gradient fill */}
            <defs>
              <linearGradient id="ratingGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#6366f1" stopOpacity="0.25" />
                <stop offset="100%" stopColor="#6366f1" stopOpacity="0.0" />
              </linearGradient>
            </defs>

            {points.length > 1 && (
              <polygon
                points={`${points[0].x},${height - paddingY} ${polylineStr} ${
                  points[points.length - 1].x
                },${height - paddingY}`}
                fill="url(#ratingGradient)"
              />
            )}

            {/* Main Polyline */}
            {points.length > 1 && (
              <polyline
                fill="none"
                stroke="#6366f1"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                points={polylineStr}
              />
            )}

            {/* Data points */}
            {points.map((p, idx) => {
              const isPB =
                activeSeries.personalBestRating !== null &&
                Math.round(p.rating) === Math.round(activeSeries.personalBestRating);
              const isHovered = hoveredIndex === idx;

              return (
                <g key={idx}>
                  <circle
                    cx={p.x}
                    cy={p.y}
                    r={isHovered ? 6 : isPB ? 5 : 3.5}
                    className={`transition-all cursor-pointer ${
                      isPB ? 'fill-amber-400 stroke-amber-600 stroke-2' : 'fill-indigo-600 stroke-white dark:stroke-surface-900 stroke-2'
                    }`}
                    onMouseEnter={() => setHoveredIndex(idx)}
                    onMouseLeave={() => setHoveredIndex(null)}
                  />
                  {/* Tooltip on hover */}
                  {isHovered && (
                    <g>
                      <rect
                        x={p.x - 45}
                        y={p.y - 36}
                        width="90"
                        height="26"
                        rx="6"
                        className="fill-surface-900 dark:fill-surface-100 shadow-md"
                      />
                      <text
                        x={p.x}
                        y={p.y - 19}
                        textAnchor="middle"
                        className="text-[11px] font-bold fill-white dark:fill-surface-900"
                      >
                        {Math.round(p.rating)} {isPB ? '★ (PB)' : ''}
                      </text>
                    </g>
                  )}
                </g>
              );
            })}
          </svg>
          <div className="flex justify-between items-center text-[10px] text-surface-400 mt-2 px-6 font-mono">
            <span>{history[0] ? new Date(history[0].timestamp).toLocaleDateString() : ''}</span>
            <span>{history.length > 1 ? new Date(history[history.length - 1].timestamp).toLocaleDateString() : ''}</span>
          </div>
        </div>
      )}
    </div>
  );
};
