package com.devsync.coding.enums;

import java.util.regex.Pattern;

/**
 * Registry of supported competitive programming and developer platforms for the
 * Coding Intelligence module.
 */
public enum CodingPlatform {

    LEETCODE("LeetCode", "https://leetcode.com/u/", true),
    CODEFORCES("Codeforces", "https://codeforces.com/profile/", true),
    CODECHEF("CodeChef", "https://www.codechef.com/users/", true),
    HACKERRANK("HackerRank", "https://www.hackerrank.com/profile/", true),
    GITHUB("GitHub", "https://github.com/", true),
    ATCODER("AtCoder", "https://atcoder.jp/users/", true);

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{1,100}$");

    private final String displayName;
    private final String profileUrlBase;
    private final boolean available;

    CodingPlatform(String displayName, String profileUrlBase, boolean available) {
        this.displayName = displayName;
        this.profileUrlBase = profileUrlBase;
        this.available = available;
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
            case LEETCODE -> profileUrlBase + cleanUsername + "/";
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
