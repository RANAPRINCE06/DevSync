import React, { useState } from 'react';
import { Columns, Check, ExternalLink, Info } from 'lucide-react';
import { CodingPlatform, PlatformAccount, PlatformConfig } from '@/types/codingIntelligence';

interface PlatformComparisonSectionProps {
  platformConfigs: Record<CodingPlatform, PlatformConfig>;
  accounts: PlatformAccount[];
}

export const PlatformComparisonSection: React.FC<PlatformComparisonSectionProps> = ({
  platformConfigs,
  accounts,
}) => {
  const activeAccounts = accounts.filter((a) => a.active);

  // Default to selecting all connected active platforms (up to 4)
  const [selectedPlatforms, setSelectedPlatforms] = useState<CodingPlatform[]>(() =>
    activeAccounts.map((a) => a.platform)
  );

  const togglePlatform = (p: CodingPlatform) => {
    setSelectedPlatforms((prev) =>
      prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]
    );
  };

  if (activeAccounts.length < 2) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm text-center py-10">
        <Columns className="w-8 h-8 text-surface-400 mx-auto mb-2 opacity-50" />
        <h4 className="text-sm font-semibold text-surface-900 dark:text-surface-100">
          Platform Comparison Unavailable
        </h4>
        <p className="text-xs text-surface-500 mt-1 max-w-sm mx-auto">
          Connect at least two coding platforms to compare platform-specific metrics side-by-side.
        </p>
      </div>
    );
  }

  const comparedAccounts = activeAccounts.filter((a) => selectedPlatforms.includes(a.platform));

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
            <Columns className="w-5 h-5 text-primary-500" />
            Side-by-Side Platform Comparison
          </h3>
          <p className="text-xs text-surface-500">
            Compare metrics across your connected platforms. Ratings are kept strictly separate.
          </p>
        </div>

        {/* Platform Selection Toggles */}
        <div className="flex items-center gap-1.5 flex-wrap">
          <span className="text-xs text-surface-400 font-medium mr-1">Select:</span>
          {activeAccounts.map((acc) => {
            const isSelected = selectedPlatforms.includes(acc.platform);
            const cfg = platformConfigs[acc.platform];
            return (
              <button
                key={acc.platform}
                onClick={() => togglePlatform(acc.platform)}
                className={`px-2.5 py-1 rounded-lg text-xs font-medium border flex items-center gap-1.5 transition-all ${
                  isSelected
                    ? `${cfg.bgLight} font-semibold shadow-xs`
                    : 'bg-surface-100 dark:bg-surface-800 text-surface-500 border-transparent hover:text-surface-800 dark:hover:text-surface-200'
                }`}
              >
                {isSelected && <Check className="w-3 h-3" />}
                {cfg.displayName}
              </button>
            );
          })}
        </div>
      </div>

      {/* Comparison Table */}
      {comparedAccounts.length === 0 ? (
        <div className="p-8 text-center text-xs text-surface-400 border border-dashed border-surface-300 dark:border-surface-700 rounded-xl">
          Select at least one platform above to inspect its metrics.
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-surface-200 dark:border-surface-800 text-surface-400 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4 bg-surface-50/50 dark:bg-surface-800/30 rounded-tl-xl w-1/4">
                  Metric Category
                </th>
                {comparedAccounts.map((acc) => {
                  const cfg = platformConfigs[acc.platform];
                  return (
                    <th key={acc.id} className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <span className={`px-2 py-0.5 rounded text-[11px] font-bold border ${cfg.bgLight}`}>
                          {cfg.displayName}
                        </span>
                        <a
                          href={acc.profileUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="text-surface-400 hover:text-primary-500 flex items-center gap-0.5 font-normal"
                          title="Open public profile"
                        >
                          @{acc.username}
                          <ExternalLink className="w-2.5 h-2.5" />
                        </a>
                      </div>
                    </th>
                  );
                })}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-100 dark:divide-surface-800/60">
              {/* Row 1: Integration Status */}
              <tr>
                <td className="py-3 px-4 font-semibold text-surface-600 dark:text-surface-300 bg-surface-50/30 dark:bg-surface-800/20">
                  Integration Level
                </td>
                {comparedAccounts.map((acc) => {
                  const cfg = platformConfigs[acc.platform];
                  return (
                    <td key={acc.id} className="py-3 px-4">
                      {cfg.communityApi ? (
                        <span className="text-[11px] font-medium text-sky-600 dark:text-sky-400">
                          Community API (AtCoder Problems)
                        </span>
                      ) : cfg.syncSupported ? (
                        <span className="text-[11px] font-medium text-emerald-600 dark:text-emerald-400">
                          Official Automated Sync
                        </span>
                      ) : (
                        <span className="text-[11px] font-medium text-amber-600 dark:text-amber-400">
                          Profile Link Only
                        </span>
                      )}
                    </td>
                  );
                })}
              </tr>

              {/* Row 2: Solved Problems */}
              <tr>
                <td className="py-3 px-4 font-semibold text-surface-600 dark:text-surface-300 bg-surface-50/30 dark:bg-surface-800/20">
                  Problems Solved
                </td>
                {comparedAccounts.map((acc) => {
                  const snap = acc.latestSnapshot;
                  if (acc.platform === 'GITHUB') {
                    return (
                      <td key={acc.id} className="py-3 px-4 text-surface-400 italic">
                        Not Applicable (Git Host)
                      </td>
                    );
                  }
                  return (
                    <td key={acc.id} className="py-3 px-4">
                      {snap?.problemsSolved != null ? (
                        <span className="font-bold text-surface-900 dark:text-surface-100 text-sm">
                          {snap.problemsSolved}
                          <span className="text-[10px] text-surface-400 font-normal ml-1">
                            {acc.platform === 'LEETCODE' ? '(LC accepted)' : acc.platform === 'CODEFORCES' ? '(verified AC)' : '(community AC)'}
                          </span>
                        </span>
                      ) : (
                        <span className="text-surface-400 italic">Pending sync</span>
                      )}
                    </td>
                  );
                })}
              </tr>

              {/* Row 3: Rating (Strictly Platform-Specific) */}
              <tr>
                <td className="py-3 px-4 font-semibold text-surface-600 dark:text-surface-300 bg-surface-50/30 dark:bg-surface-800/20">
                  Contest Rating
                </td>
                {comparedAccounts.map((acc) => {
                  const snap = acc.latestSnapshot;
                  if (acc.platform === 'GITHUB' || acc.platform === 'ATCODER') {
                    return (
                      <td key={acc.id} className="py-3 px-4 text-surface-400 italic">
                        {acc.platform === 'ATCODER' ? 'Not exposed in Community API' : 'Not Applicable'}
                      </td>
                    );
                  }
                  return (
                    <td key={acc.id} className="py-3 px-4">
                      {snap?.currentRating != null ? (
                        <div>
                          <span className="font-bold text-surface-900 dark:text-white">
                            {Math.round(snap.currentRating)}
                          </span>
                          {snap.maximumRating != null && (
                            <span className="text-[10px] text-surface-400 ml-1">
                              (max: {Math.round(snap.maximumRating)})
                            </span>
                          )}
                        </div>
                      ) : (
                        <span className="text-surface-400 italic">Unrated / No contests</span>
                      )}
                    </td>
                  );
                })}
              </tr>

              {/* Row 4: Contests Participated */}
              <tr>
                <td className="py-3 px-4 font-semibold text-surface-600 dark:text-surface-300 bg-surface-50/30 dark:bg-surface-800/20">
                  Contest Rounds
                </td>
                {comparedAccounts.map((acc) => {
                  const snap = acc.latestSnapshot;
                  if (acc.platform === 'GITHUB' || acc.platform === 'ATCODER') {
                    return (
                      <td key={acc.id} className="py-3 px-4 text-surface-400 italic">
                        Not Applicable
                      </td>
                    );
                  }
                  return (
                    <td key={acc.id} className="py-3 px-4 font-medium text-surface-800 dark:text-surface-200">
                      {snap?.contestsParticipated != null ? `${snap.contestsParticipated} rounds` : '—'}
                    </td>
                  );
                })}
              </tr>

              {/* Row 5: GitHub Specific / Platform Telemetry */}
              <tr>
                <td className="py-3 px-4 font-semibold text-surface-600 dark:text-surface-300 bg-surface-50/30 dark:bg-surface-800/20">
                  Platform Specifics
                </td>
                {comparedAccounts.map((acc) => {
                  const snap = acc.latestSnapshot;
                  const add = snap?.additionalStats;
                  if (acc.platform === 'GITHUB') {
                    return (
                      <td key={acc.id} className="py-3 px-4 font-medium text-surface-800 dark:text-surface-200">
                        {add?.publicRepos != null ? `${add.publicRepos} repos` : '—'} • ★ {add?.totalStars != null ? String(add.totalStars) : '0'}
                      </td>
                    );
                  }
                  if (acc.platform === 'ATCODER') {
                    return (
                      <td key={acc.id} className="py-3 px-4 font-medium text-surface-800 dark:text-surface-200">
                        Rank #{add?.communityAcRank != null ? Number(add.communityAcRank).toLocaleString() : '—'} • {add?.longestStreakDays != null ? `${add.longestStreakDays}d streak` : '0d streak'}
                      </td>
                    );
                  }
                  if (acc.platform === 'LEETCODE' && snap?.easySolved != null) {
                    return (
                      <td key={acc.id} className="py-3 px-4 text-surface-700 dark:text-surface-300">
                        {snap.easySolved}E / {snap.mediumSolved ?? 0}M / {snap.hardSolved ?? 0}H
                      </td>
                    );
                  }
                  if (acc.platform === 'CODEFORCES' && snap?.globalRank != null) {
                    return (
                      <td key={acc.id} className="py-3 px-4 font-medium text-surface-800 dark:text-surface-200">
                        Rank #{snap.globalRank.toLocaleString()}
                      </td>
                    );
                  }
                  return (
                    <td key={acc.id} className="py-3 px-4 text-surface-400 italic">
                      Standard metrics
                    </td>
                  );
                })}
              </tr>

              {/* Row 6: Sync Health */}
              <tr>
                <td className="py-3 px-4 font-semibold text-surface-600 dark:text-surface-300 bg-surface-50/30 dark:bg-surface-800/20 rounded-bl-xl">
                  Sync Status
                </td>
                {comparedAccounts.map((acc) => (
                  <td key={acc.id} className="py-3 px-4">
                    <span
                      className={`font-semibold px-2 py-0.5 rounded text-[10px] ${
                        acc.syncStatus === 'SUCCESS'
                          ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400'
                          : acc.syncStatus === 'FAILED'
                          ? 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'
                          : 'bg-surface-100 text-surface-700 dark:bg-surface-800 dark:text-surface-300'
                      }`}
                    >
                      {acc.syncStatus}
                    </span>
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
        </div>
      )}

      {/* Invariant Explanation Banner */}
      <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800 text-[11px] text-surface-500 flex items-start gap-2">
        <Info className="w-4 h-4 text-primary-500 shrink-0 mt-0.5" />
        <span>
          <strong>Platform Invariant Rule:</strong> Competitive programming ratings use disparate mathematical models (Codeforces Elo vs LeetCode contest rating) and are never cross-compared. AtCoder community AC rank reflects solve frequency on AtCoder Problems, distinct from contest rating leaderboards.
        </span>
      </div>
    </div>
  );
};
