import React from 'react';
import { Github, Star, GitFork, Users, FolderGit2, MapPin, Building2, ExternalLink } from 'lucide-react';
import { PlatformAccount } from '@/types/codingIntelligence';

interface GitHubInsightsSectionProps {
  gitHubAccount?: PlatformAccount;
  onConnectGitHub: () => void;
}

export const GitHubInsightsSection: React.FC<GitHubInsightsSectionProps> = ({
  gitHubAccount,
  onConnectGitHub,
}) => {
  if (!gitHubAccount || !gitHubAccount.active) {
    return (
      <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3.5">
          <div className="p-3 rounded-2xl bg-purple-500/10 text-purple-400">
            <Github className="w-6 h-6" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-surface-900 dark:text-white">
              GitHub Developer Telemetry Not Linked
            </h4>
            <p className="text-xs text-surface-500 mt-0.5">
              Connect your GitHub account to track public repositories, star recognition, followers, and open-source consistency.
            </p>
          </div>
        </div>
        <button
          onClick={onConnectGitHub}
          className="px-4 py-2 rounded-xl text-xs font-semibold bg-purple-600 hover:bg-purple-700 text-white transition-colors shrink-0"
        >
          Connect GitHub
        </button>
      </div>
    );
  }

  const snapshot = gitHubAccount.latestSnapshot;
  const stats = snapshot?.additionalStats || {};
  const repos = stats.publicRepos != null ? Number(stats.publicRepos) : null;
  const stars = stats.totalStars != null ? Number(stats.totalStars) : 0;
  const forks = stats.totalForks != null ? Number(stats.totalForks) : 0;
  const followers = stats.followers != null ? Number(stats.followers) : 0;
  const following = stats.following != null ? Number(stats.following) : 0;
  const company = typeof stats.company === 'string' ? stats.company : null;
  const location = typeof stats.location === 'string' ? stats.location : null;
  const bio = typeof stats.bio === 'string' ? stats.bio : null;

  return (
    <div className="p-6 rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 shadow-sm space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-400">
            <Github className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-bold text-surface-900 dark:text-white flex items-center gap-2">
              GitHub Developer Insights
              <a
                href={gitHubAccount.profileUrl}
                target="_blank"
                rel="noreferrer"
                className="text-xs font-normal text-primary-600 dark:text-primary-400 hover:underline flex items-center gap-1"
              >
                @{gitHubAccount.username}
                <ExternalLink className="w-3 h-3" />
              </a>
            </h3>
            <p className="text-xs text-surface-500">
              Verified open-source activity, repository portfolio, and developer recognition
            </p>
          </div>
        </div>

        {gitHubAccount.lastSyncedAt && (
          <span className="text-[11px] text-surface-400 self-start sm:self-auto">
            Synced: {new Date(gitHubAccount.lastSyncedAt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
          </span>
        )}
      </div>

      {bio && (
        <p className="text-xs text-surface-600 dark:text-surface-300 italic border-l-2 border-purple-500/40 pl-3">
          "{bio}"
        </p>
      )}

      {/* GitHub Metric Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-4 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800">
          <div className="flex items-center gap-2 text-surface-500 mb-1">
            <FolderGit2 className="w-3.5 h-3.5 text-purple-400" />
            <span className="text-[11px] font-medium">Public Repos</span>
          </div>
          <div className="text-xl font-bold text-surface-900 dark:text-white">
            {repos ?? '—'}
          </div>
        </div>

        <div className="p-4 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800">
          <div className="flex items-center gap-2 text-surface-500 mb-1">
            <Star className="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
            <span className="text-[11px] font-medium">Stars Received</span>
          </div>
          <div className="text-xl font-bold text-amber-500">
            {stars}
          </div>
        </div>

        <div className="p-4 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800">
          <div className="flex items-center gap-2 text-surface-500 mb-1">
            <GitFork className="w-3.5 h-3.5 text-blue-400" />
            <span className="text-[11px] font-medium">Forks</span>
          </div>
          <div className="text-xl font-bold text-surface-900 dark:text-white">
            {forks}
          </div>
        </div>

        <div className="p-4 rounded-xl bg-surface-50 dark:bg-surface-800/40 border border-surface-200 dark:border-surface-800">
          <div className="flex items-center gap-2 text-surface-500 mb-1">
            <Users className="w-3.5 h-3.5 text-emerald-400" />
            <span className="text-[11px] font-medium">Followers</span>
          </div>
          <div className="text-xl font-bold text-surface-900 dark:text-white">
            {followers}
            <span className="text-xs font-normal text-surface-400 ml-1.5 font-sans">
              ({following} following)
            </span>
          </div>
        </div>
      </div>

      {(company || location) && (
        <div className="flex items-center gap-4 text-xs text-surface-500 flex-wrap pt-1">
          {company && (
            <span className="flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-surface-400" />
              {company}
            </span>
          )}
          {location && (
            <span className="flex items-center gap-1.5">
              <MapPin className="w-3.5 h-3.5 text-surface-400" />
              {location}
            </span>
          )}
        </div>
      )}

      {/* Metric Invariant Clarification */}
      <p className="text-[11px] text-surface-400 leading-relaxed bg-surface-50 dark:bg-surface-800/30 p-2.5 rounded-lg border border-surface-200/50 dark:border-surface-800">
        <strong>Data Integrity Notice:</strong> GitHub metrics represent verified public repository counts and stargazer totals sourced from the official REST API. Algorithmic problem metrics do not apply to GitHub.
      </p>
    </div>
  );
};
