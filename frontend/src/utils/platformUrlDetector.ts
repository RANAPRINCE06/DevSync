import { CodingPlatform } from '@/types/codingIntelligence';

export interface ClientDetectedUrl {
  platform: CodingPlatform;
  platformDisplayName: string;
  username: string;
  canonicalProfileUrl: string;
  isValid: boolean;
  errorMessage?: string;
}

const LEETCODE_RESERVED = new Set([
  'problems', 'contest', 'discuss', 'explore', 'tag', 'submissions',
  'developer', 'playground', 'assessment', 'interview', 'subscribe',
  'support', 'terms', 'privacy', 'login', 'register', 'accounts', 'profile',
  'studyplan', 'problemset', 'company', 'circle'
]);

const GITHUB_RESERVED = new Set([
  'features', 'pricing', 'enterprise', 'explore', 'topics', 'trending',
  'collections', 'events', 'security', 'login', 'join', 'search', 'settings',
  'marketplace', 'organizations', 'pulls', 'issues', 'notifications',
  'discussions', 'sponsors', 'about', 'site', 'contact', 'privacy', 'terms',
  'orgs', 'new', 'stars', 'codespaces', 'account', 'logout', 'readme',
  'dashboard', 'apps', 'sessions'
]);

const HACKERRANK_RESERVED = new Set([
  'dashboard', 'domains', 'challenges', 'contests', 'leaderboard',
  'administration', 'settings', 'jobs', 'work', 'community', 'login',
  'auth', 'signup', 'certificates', 'support', 'scoring', 'faq'
]);

const LEETCODE_REGEX = /^[a-zA-Z0-9_-]{3,30}$/;
const CODEFORCES_REGEX = /^[a-zA-Z0-9_.-]{3,24}$/;
const GITHUB_REGEX = /^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$/;
const ATCODER_REGEX = /^[a-zA-Z0-9_]{3,16}$/;
const CODECHEF_REGEX = /^[a-zA-Z0-9_]{3,30}$/;
const HACKERRANK_REGEX = /^[a-zA-Z0-9_]{3,30}$/;

/**
 * Parses user input to detect coding platform and extract clean username on the client.
 */
export function detectPlatformFromUrl(input: string): ClientDetectedUrl | null {
  if (!input || !input.trim()) return null;

  let cleaned = input.trim();
  if (!cleaned.startsWith('http://') && !cleaned.startsWith('https://')) {
    cleaned = 'https://' + cleaned;
  }

  let parsed: URL;
  try {
    parsed = new URL(cleaned);
  } catch {
    return null;
  }

  let host = parsed.hostname.toLowerCase();
  if (host.startsWith('www.')) {
    host = host.slice(4);
  }

  const rawSegments = parsed.pathname
    .split('/')
    .map((s) => decodeURIComponent(s).trim())
    .filter(Boolean);

  if (rawSegments.length === 0) return null;

  switch (host) {
    case 'leetcode.com': {
      const first = rawSegments[0];
      if (LEETCODE_RESERVED.has(first.toLowerCase())) {
        return {
          platform: 'LEETCODE',
          platformDisplayName: 'LeetCode',
          username: '',
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: `'${first}' is a platform-reserved path, not a user profile`,
        };
      }
      let username = '';
      if (rawSegments.length === 2 && first.toLowerCase() === 'u') {
        username = rawSegments[1];
      } else if (rawSegments.length === 1) {
        username = first;
      } else {
        return {
          platform: 'LEETCODE',
          platformDisplayName: 'LeetCode',
          username: '',
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: 'Expected structure: https://leetcode.com/u/{username}/',
        };
      }

      const isValid = LEETCODE_REGEX.test(username);
      return {
        platform: 'LEETCODE',
        platformDisplayName: 'LeetCode',
        username,
        canonicalProfileUrl: `https://leetcode.com/u/${username}/`,
        isValid,
        errorMessage: isValid ? undefined : 'Username must be 3-30 characters (alphanumeric, -, _)',
      };
    }

    case 'codeforces.com': {
      if (rawSegments.length !== 2 || rawSegments[0].toLowerCase() !== 'profile') {
        return {
          platform: 'CODEFORCES',
          platformDisplayName: 'Codeforces',
          username: '',
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: 'Expected structure: https://codeforces.com/profile/{username}',
        };
      }
      const username = rawSegments[1];
      const isValid = CODEFORCES_REGEX.test(username);
      return {
        platform: 'CODEFORCES',
        platformDisplayName: 'Codeforces',
        username,
        canonicalProfileUrl: `https://codeforces.com/profile/${username}`,
        isValid,
        errorMessage: isValid ? undefined : 'Handle must be 3-24 characters (alphanumeric, _, ., -)',
      };
    }

    case 'github.com': {
      if (rawSegments.length > 1) {
        return {
          platform: 'GITHUB',
          platformDisplayName: 'GitHub',
          username: '',
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: 'Repository URLs are not supported. Expected: https://github.com/{username}',
        };
      }
      const username = rawSegments[0];
      if (GITHUB_RESERVED.has(username.toLowerCase())) {
        return {
          platform: 'GITHUB',
          platformDisplayName: 'GitHub',
          username,
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: `'${username}' is a GitHub reserved path`,
        };
      }
      const isValid = GITHUB_REGEX.test(username);
      return {
        platform: 'GITHUB',
        platformDisplayName: 'GitHub',
        username,
        canonicalProfileUrl: `https://github.com/${username}`,
        isValid,
        errorMessage: isValid ? undefined : 'GitHub username must be alphanumeric or single hyphens',
      };
    }

    case 'atcoder.jp': {
      if (rawSegments.length !== 2 || rawSegments[0].toLowerCase() !== 'users') {
        return {
          platform: 'ATCODER',
          platformDisplayName: 'AtCoder',
          username: '',
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: 'Expected structure: https://atcoder.jp/users/{username}',
        };
      }
      const username = rawSegments[1];
      const isValid = ATCODER_REGEX.test(username);
      return {
        platform: 'ATCODER',
        platformDisplayName: 'AtCoder',
        username,
        canonicalProfileUrl: `https://atcoder.jp/users/${username}`,
        isValid,
        errorMessage: isValid ? undefined : 'AtCoder username must be 3-16 characters (alphanumeric, _)',
      };
    }

    case 'codechef.com': {
      if (rawSegments.length !== 2 || rawSegments[0].toLowerCase() !== 'users') {
        return {
          platform: 'CODECHEF',
          platformDisplayName: 'CodeChef',
          username: '',
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: 'Expected structure: https://www.codechef.com/users/{username}',
        };
      }
      const username = rawSegments[1];
      const isValid = CODECHEF_REGEX.test(username);
      return {
        platform: 'CODECHEF',
        platformDisplayName: 'CodeChef',
        username,
        canonicalProfileUrl: `https://www.codechef.com/users/${username}`,
        isValid,
        errorMessage: isValid ? undefined : 'CodeChef handle must be 3-30 characters',
      };
    }

    case 'hackerrank.com': {
      let username = '';
      if (rawSegments.length === 2 && rawSegments[0].toLowerCase() === 'profile') {
        username = rawSegments[1];
      } else if (rawSegments.length === 1) {
        if (HACKERRANK_RESERVED.has(rawSegments[0].toLowerCase())) {
          return {
            platform: 'HACKERRANK',
            platformDisplayName: 'HackerRank',
            username: '',
            canonicalProfileUrl: '',
            isValid: false,
            errorMessage: `'${rawSegments[0]}' is a reserved platform path`,
          };
        }
        username = rawSegments[0];
      } else {
        return {
          platform: 'HACKERRANK',
          platformDisplayName: 'HackerRank',
          username: '',
          canonicalProfileUrl: '',
          isValid: false,
          errorMessage: 'Expected structure: https://www.hackerrank.com/profile/{username}',
        };
      }
      const isValid = HACKERRANK_REGEX.test(username);
      return {
        platform: 'HACKERRANK',
        platformDisplayName: 'HackerRank',
        username,
        canonicalProfileUrl: `https://www.hackerrank.com/profile/${username}`,
        isValid,
        errorMessage: isValid ? undefined : 'HackerRank username must be 3-30 characters',
      };
    }

    default:
      return null;
  }
}
