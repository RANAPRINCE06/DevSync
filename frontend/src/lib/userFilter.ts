import { TeamMember } from '@/types/team';

/**
 * Determines whether a user or team member is an internal DevSync system/admin account
 * that should not be displayed in team rosters, leaderboards, or assignee pickers.
 */
export function isSystemOrInternalAccount(account: { name?: string; userName?: string; email?: string; userEmail?: string }): boolean {
  if (!account) return false;
  const name = (account.name || account.userName || '').toLowerCase().trim();
  const email = (account.email || account.userEmail || '').toLowerCase().trim();

  // Internal system/admin signatures
  const isDevSyncAdminEmail = email === 'admin@devsync.io' || email === 'system@devsync.io' || email === 'admin@example.com';
  const isDevSyncAdminName = name === 'devsync admin' || name === 'system user' || name === 'devsync system';

  return isDevSyncAdminEmail || isDevSyncAdminName;
}

/**
 * Filters a list of users to exclude system/admin internal accounts.
 */
export function filterRealUsers<T extends { name?: string; userName?: string; email?: string; userEmail?: string }>(list: T[]): T[] {
  if (!Array.isArray(list)) return [];
  return list.filter((item) => !isSystemOrInternalAccount(item));
}

/**
 * Filters team members to exclude system/admin internal accounts.
 */
export function filterRealTeamMembers(members: TeamMember[]): TeamMember[] {
  if (!Array.isArray(members)) return [];
  return members.filter((m) => !isSystemOrInternalAccount(m));
}
