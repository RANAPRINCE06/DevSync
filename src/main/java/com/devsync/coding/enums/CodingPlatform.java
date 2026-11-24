package com.devsync.coding.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Registry of supported competitive programming and developer platforms for the
 * Coding Intelligence module.
 */
public enum CodingPlatform {

    LEETCODE(
            "LeetCode",
            "https://leetcode.com/u/",
            true,
            EnumSet.of(
                    PlatformCapability.PROFILE_LINK_SUPPORTED,
                    PlatformCapability.AUTOMATED_SYNC_SUPPORTED,
                    PlatformCapability.CONTEST_RATING_SUPPORTED,
                    PlatformCapability.PROBLEM_COUNTS_SUPPORTED,
                    PlatformCapability.DIFFICULTY_BREAKDOWN_SUPPORTED
            )
    ),
    CODEFORCES(
            "Codeforces",
            "https://codeforces.com/profile/",
            true,
            EnumSet.of(
                    PlatformCapability.PROFILE_LINK_SUPPORTED,
                    PlatformCapability.AUTOMATED_SYNC_SUPPORTED,
                    PlatformCapability.CONTEST_RATING_SUPPORTED,
                    PlatformCapability.PROBLEM_COUNTS_SUPPORTED,
                    PlatformCapability.ACTIVITY_HISTORY_SUPPORTED
            )
    ),
    CODECHEF(
            "CodeChef",
            "https://www.codechef.com/users/",
            true,
            EnumSet.of(
                    PlatformCapability.PROFILE_LINK_SUPPORTED,
                    PlatformCapability.AUTHENTICATED_SYNC_REQUIRED
            )
    ),
    HACKERRANK(
            "HackerRank",
            "https://www.hackerrank.com/profile/",
            true,
            EnumSet.of(
                    PlatformCapability.PROFILE_LINK_SUPPORTED
            )
    ),
    GITHUB(
            "GitHub",
            "https://github.com/",
            true,
            EnumSet.of(
                    PlatformCapability.PROFILE_LINK_SUPPORTED,
                    PlatformCapability.AUTOMATED_SYNC_SUPPORTED,
                    PlatformCapability.ACTIVITY_HISTORY_SUPPORTED
            )
    ),
    ATCODER(
            "AtCoder",
            "https://atcoder.jp/users/",
            true,
            EnumSet.of(
                    PlatformCapability.PROFILE_LINK_SUPPORTED,
                    PlatformCapability.AUTOMATED_SYNC_SUPPORTED,
                    PlatformCapability.COMMUNITY_API_SUPPORTED,
                    PlatformCapability.PROBLEM_COUNTS_SUPPORTED
            )
    ),
    GEEKSFORGEEKS(
            "GeeksforGeeks",
            "https://www.geeksforgeeks.org/user/",
            true,
            EnumSet.of(
                    PlatformCapability.PROFILE_LINK_SUPPORTED,
                    PlatformCapability.AUTOMATED_SYNC_SUPPORTED,
                    PlatformCapability.COMMUNITY_API_SUPPORTED,
                    PlatformCapability.PROBLEM_COUNTS_SUPPORTED,
                    PlatformCapability.DIFFICULTY_BREAKDOWN_SUPPORTED
            )
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{1,100}$");

    private final String displayName;
    private final String profileUrlBase;
    private final boolean available;
    private final Set<PlatformCapability> capabilities;

    CodingPlatform(String displayName, String profileUrlBase, boolean available, Set<PlatformCapability> capabilities) {
        this.displayName = displayName;
        this.profileUrlBase = profileUrlBase;
        this.available = available;
        this.capabilities = Collections.unmodifiableSet(capabilities);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileUrlBase() {
        return profileUrlBase;
    }

    public boolean isAvailable() {
        return available;
    }

    public Set<PlatformCapability> getCapabilities() {
        return capabilities;
    }

    public boolean hasCapability(PlatformCapability capability) {
        return capabilities.contains(capability);
    }

    public boolean isAutomatedSyncSupported() {
        return hasCapability(PlatformCapability.AUTOMATED_SYNC_SUPPORTED);
    }

    public boolean isCommunityApiSupported() {
        return hasCapability(PlatformCapability.COMMUNITY_API_SUPPORTED);
    }

    /**
     * Builds the public profile URL for a given username based on platform-specific strategy.
     *
     * @param username the external platform username
     * @return the fully qualified public profile URL
     */
    public String buildProfileUrl(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        String cleanUsername = username.trim();
        return switch (this) {
            case LEETCODE, GEEKSFORGEEKS -> profileUrlBase + cleanUsername + "/";
            default -> profileUrlBase + cleanUsername;
        };
    }

    /**
     * Validates that the provided username matches valid character and length constraints.
     *
     * @param username external platform username
     * @return true if username is structurally valid
     */
    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() && USERNAME_PATTERN.matcher(username.trim()).matches();
    }
}
