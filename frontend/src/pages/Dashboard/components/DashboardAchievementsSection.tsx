import React, { useState, useMemo } from 'react';
import {
  Award,
  ShieldCheck,
  Search,
  ChevronRight,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { useAchievements } from '@/hooks/useCodingPerformance';
import { PlatformAchievementResponse } from '@/types/codingPerformance';

const PLATFORM_COLORS: Record<string, { color: string; label: string; badgeClass: string }> = {
  LEETCODE: { color: '#FFA116', label: 'LeetCode', badgeClass: 'bg-amber-500/10 text-amber-500 border-amber-500/20' },
  CODEFORCES: { color: '#1F8ACB', label: 'Codeforces', badgeClass: 'bg-blue-500/10 text-blue-500 border-blue-500/20' },
  GITHUB: { color: '#8b5cf6', label: 'GitHub', badgeClass: 'bg-purple-500/10 text-purple-400 border-purple-500/20' },
  ATCODER: { color: '#06b6d4', label: 'AtCoder', badgeClass: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20' },
  GEEKSFORGEEKS: { color: '#10b981', label: 'GeeksforGeeks', badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' },
  CODECHEF: { color: '#d97706', label: 'CodeChef', badgeClass: 'bg-orange-500/10 text-orange-400 border-orange-500/20' },
  HACKERRANK: { color: '#22c55e', label: 'HackerRank', badgeClass: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' },
};

export const DashboardAchievementsSection: React.FC = () => {
  const [selectedPlatform, setSelectedPlatform] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedBadge, setSelectedBadge] = useState<PlatformAchievementResponse | null>(null);
  const [isAllModalOpen, setIsAllModalOpen] = useState<boolean>(false);

  const { data: summary, isLoading } = useAchievements();

  const allAchievements = summary?.allAchievements || [];
  const totalEarned = summary?.totalEarned || 0;
  const platformBreakdown = summary?.platformBreakdown || {};

  // Filter achievements
  const filteredBadges = useMemo(() => {
    return allAchievements.filter((badge) => {
      const matchesPlatform = selectedPlatform === 'ALL' || badge.platform === selectedPlatform;
      const matchesSearch =
        searchQuery === '' ||
        badge.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (badge.description && badge.description.toLowerCase().includes(searchQuery.toLowerCase()));

      return matchesPlatform && matchesSearch;
    });
  }, [allAchievements, selectedPlatform, searchQuery]);

  return (
    <Card className="rounded-[3px] border border-[#cfd5dc] dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm overflow-hidden">
      <CardHeader className="p-4 sm:p-5 border-b border-[#cfd5dc] dark:border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-50/50 dark:bg-slate-800/40">
        <div>
          <div className="flex items-center gap-2">
            <Award className="w-5 h-5 text-indigo-500" />
            <CardTitle className="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wider">
              Cross-Platform Badges & Achievements
            </CardTitle>
            {totalEarned > 0 && (
              <span className="px-2 py-0.5 rounded-[2px] text-xs font-bold bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800">
                {totalEarned} Verified
              </span>
            )}
          </div>
          <p className="text-xs text-slate-700 dark:text-slate-300 mt-1">
            Aggregated milestones, problem badges, contest honors, and open source accolades across all linked platforms
          </p>
        </div>

        {/* View All Button */}
        {allAchievements.length > 8 && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsAllModalOpen(true)}
            className="rounded-[2px] text-xs self-start sm:self-auto"
          >
            View All ({allAchievements.length})
            <ChevronRight className="w-3.5 h-3.5 ml-1" />
          </Button>
        )}
      </CardHeader>

      <CardContent className="p-4 sm:p-5 space-y-5">
        {/* Filter Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          {/* Platform Filter Tabs */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
            <button
              onClick={() => setSelectedPlatform('ALL')}
              className={`px-3 py-1.5 text-xs font-medium rounded-[2px] border transition-colors whitespace-nowrap ${
                selectedPlatform === 'ALL'
                  ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900 border-slate-900 dark:border-white shadow-xs'
                  : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/60'
              }`}
            >
              All Platforms ({totalEarned})
            </button>

            {Object.keys(PLATFORM_COLORS).map((platKey) => {
              const meta = PLATFORM_COLORS[platKey];
              const count = platformBreakdown[platKey] || 0;
              if (count === 0 && selectedPlatform !== platKey) return null;

              const isSelected = selectedPlatform === platKey;

              return (
                <button
                  key={platKey}
                  onClick={() => setSelectedPlatform(platKey)}
                  className={`flex items-center gap-1.5 px-2.5 py-1.5 text-xs font-medium rounded-[2px] border transition-colors whitespace-nowrap ${
                    isSelected
                      ? 'bg-blue-600 text-white border-blue-600 shadow-xs'
                      : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/60'
                  }`}
                >
                  <span className="w-2 h-2 rounded-full" style={{ backgroundColor: meta.color }} />
                  <span>{meta.label}</span>
                  {count > 0 && (
                    <span
                      className={`text-[10px] px-1 py-0.2 rounded-[2px] font-bold ${
                        isSelected ? 'bg-blue-700 text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300'
                      }`}
                    >
                      {count}
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          {/* Search Box */}
          <div className="relative min-w-[200px] max-w-xs self-start sm:self-auto">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search badges..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 text-xs rounded-[2px] border border-[#cfd5dc] dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
        </div>

        {/* Badges Grid */}
        {isLoading ? (
          <div className="h-40 flex items-center justify-center text-xs text-slate-600 dark:text-slate-400">
            Loading verified platform achievements...
          </div>
        ) : filteredBadges.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            {filteredBadges.slice(0, 8).map((badge) => {
              const meta = PLATFORM_COLORS[badge.platform] || {
                color: '#64748b',
                label: badge.platformDisplayName,
                badgeClass: 'bg-slate-100 text-slate-600 border-slate-200',
              };

              return (
                <div
                  key={badge.id}
                  onClick={() => setSelectedBadge(badge)}
                  className="group relative p-3 bg-slate-50/70 dark:bg-slate-800/40 hover:bg-white dark:hover:bg-slate-800 rounded-[3px] border border-[#cfd5dc] dark:border-slate-800 hover:border-blue-400 dark:hover:border-blue-600 transition-all cursor-pointer shadow-2xs flex flex-col justify-between"
                >
                  <div className="flex items-start gap-3">
                    {/* Badge Icon */}
                    <div className="w-10 h-10 shrink-0 rounded-[2px] bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 flex items-center justify-center p-1 overflow-hidden shadow-2xs group-hover:scale-105 transition-transform">
                      {badge.imageUrl ? (
                        <img
                          src={badge.imageUrl}
                          alt={badge.name}
                          className="w-full h-full object-contain"
                          onError={(e) => {
                            (e.currentTarget as HTMLElement).style.display = 'none';
                          }}
                        />
                      ) : (
                        <Award className="w-5 h-5 text-indigo-500" />
                      )}
                    </div>

                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-1.5">
                        <span className={`text-[10px] px-1.5 py-0.2 rounded-[2px] font-semibold border ${meta.badgeClass}`}>
                          {meta.label}
                        </span>
                        {badge.level && (
                          <span className="text-[9px] uppercase font-bold tracking-wider text-slate-600 dark:text-slate-400">
                            {badge.level}
                          </span>
                        )}
                      </div>
                      <h5 className="font-semibold text-xs text-slate-900 dark:text-white mt-1 truncate">
                        {badge.name}
                      </h5>
                      <p className="text-[11px] text-slate-700 dark:text-slate-300 line-clamp-2 mt-0.5 leading-snug">
                        {badge.description || 'Verified coding achievement awarded for platform milestones.'}
                      </p>
                    </div>
                  </div>

                  <div className="mt-3 pt-2 border-t border-slate-200/60 dark:border-slate-700/60 flex items-center justify-between text-[10px] text-slate-600 dark:text-slate-400">
                    <span className="flex items-center gap-1">
                      <ShieldCheck className="w-3 h-3 text-emerald-500" />
                      Verified
                    </span>
                    <span>
                      {badge.earnedDate
                        ? new Date(badge.earnedDate).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
                        : 'Recorded'}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="h-32 flex flex-col items-center justify-center text-center p-4 border border-dashed border-[#cfd5dc] dark:border-slate-800 rounded-[3px]">
            <Award className="w-6 h-6 text-slate-400 dark:text-slate-600 mb-1.5" />
            <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">
              No achievements found matching your filter
            </p>
            <p className="text-[11px] text-slate-600 dark:text-slate-400 mt-0.5">
              Sync your connected coding profiles to automatically pull verified badges.
            </p>
          </div>
        )}
      </CardContent>

      {/* Badge Detail Modal */}
      {selectedBadge && (
        <Modal
          isOpen={!!selectedBadge}
          onClose={() => setSelectedBadge(null)}
          title={selectedBadge.name}
          description={`${selectedBadge.platformDisplayName} Verified Achievement`}
        >
          <div className="space-y-4">
            <div className="flex items-center gap-4 p-3 bg-slate-50 dark:bg-slate-800 rounded-[3px] border border-slate-200 dark:border-slate-700">
              <div className="w-14 h-14 shrink-0 rounded-[2px] bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 flex items-center justify-center p-1.5">
                {selectedBadge.imageUrl ? (
                  <img
                    src={selectedBadge.imageUrl}
                    alt={selectedBadge.name}
                    className="w-full h-full object-contain"
                  />
                ) : (
                  <Award className="w-8 h-8 text-indigo-500" />
                )}
              </div>
              <div>
                <h4 className="text-sm font-bold text-slate-900 dark:text-white">{selectedBadge.name}</h4>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-xs font-medium text-slate-600 dark:text-slate-400">
                    Platform: <strong className="text-slate-900 dark:text-white">{selectedBadge.platformDisplayName}</strong>
                  </span>
                  {selectedBadge.level && (
                    <span className="text-xs font-medium text-slate-600 dark:text-slate-400">
                      Tier: <strong className="text-slate-900 dark:text-white">{selectedBadge.level}</strong>
                    </span>
                  )}
                </div>
              </div>
            </div>

            <div>
              <h5 className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1">
                Description
              </h5>
              <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed bg-white dark:bg-slate-900 p-2.5 rounded-[2px] border border-slate-200 dark:border-slate-800">
                {selectedBadge.description || 'Verified coding achievement awarded for platform milestones.'}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="p-2.5 bg-slate-50 dark:bg-slate-800 rounded-[2px] border border-slate-200 dark:border-slate-700">
                <span className="text-slate-600 dark:text-slate-400 block text-[11px]">Earned Date</span>
                <span className="font-semibold text-slate-900 dark:text-white mt-0.5 block">
                  {selectedBadge.earnedDate
                    ? new Date(selectedBadge.earnedDate).toLocaleDateString('en-US', {
                        month: 'long',
                        day: 'numeric',
                        year: 'numeric',
                      })
                    : 'Recorded at profile sync'}
                </span>
              </div>
              <div className="p-2.5 bg-slate-50 dark:bg-slate-800 rounded-[2px] border border-slate-200 dark:border-slate-700">
                <span className="text-slate-600 dark:text-slate-400 block text-[11px]">Verification Status</span>
                <span className="font-semibold text-emerald-600 dark:text-emerald-400 mt-0.5 flex items-center gap-1">
                  <ShieldCheck className="w-3.5 h-3.5" /> Verified Public Record
                </span>
              </div>
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-200 dark:border-slate-700">
              <Button variant="outline" size="sm" onClick={() => setSelectedBadge(null)}>
                Close
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* View All Badges Modal */}
      {isAllModalOpen && (
        <Modal
          isOpen={isAllModalOpen}
          onClose={() => setIsAllModalOpen(false)}
          title={`All Verified Achievements (${allAchievements.length})`}
          description="Complete catalog of badges earned across connected coding platforms"
        >
          <div className="space-y-4 max-h-[60vh] overflow-y-auto pr-1">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {allAchievements.map((badge) => {
                const meta = PLATFORM_COLORS[badge.platform] || {
                  color: '#64748b',
                  label: badge.platformDisplayName,
                  badgeClass: 'bg-slate-100 text-slate-600 border-slate-200',
                };

                return (
                  <div
                    key={badge.id}
                    className="p-2.5 bg-slate-50 dark:bg-slate-800 rounded-[3px] border border-slate-200 dark:border-slate-700 flex items-start gap-2.5"
                  >
                    <div className="w-8 h-8 shrink-0 rounded-[2px] bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 flex items-center justify-center p-1">
                      {badge.imageUrl ? (
                        <img src={badge.imageUrl} alt={badge.name} className="w-full h-full object-contain" />
                      ) : (
                        <Award className="w-4 h-4 text-indigo-500" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-1">
                        <span className={`text-[9px] px-1 py-0.2 rounded-[2px] font-semibold border ${meta.badgeClass}`}>
                          {meta.label}
                        </span>
                      </div>
                      <h6 className="font-semibold text-xs text-slate-900 dark:text-white truncate mt-0.5">
                        {badge.name}
                      </h6>
                      <p className="text-[10px] text-slate-600 dark:text-slate-400 line-clamp-1 mt-0.5">
                        {badge.description || 'Verified coding achievement.'}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="flex justify-end pt-3 border-t border-slate-200 dark:border-slate-700">
              <Button variant="outline" size="sm" onClick={() => setIsAllModalOpen(false)}>
                Done
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </Card>
  );
};
