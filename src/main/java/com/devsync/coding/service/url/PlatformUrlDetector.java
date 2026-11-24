package com.devsync.coding.service.url;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.common.exception.BadRequestException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Intelligent URL detector and parser for competitive programming and developer platforms.
 *
 * Supported platforms:
 * - LeetCode: leetcode.com/u/{username}/ or leetcode.com/{username}
 * - Codeforces: codeforces.com/profile/{username}
 * - GitHub: github.com/{username}
 * - AtCoder: atcoder.jp/users/{username}
 * - CodeChef: codechef.com/users/{username}
 * - HackerRank: hackerrank.com/profile/{username} or hackerrank.com/{username}
 *
 * Invariants & Security:
 * - Protects against SSRF: domains are validated against a strict compile-time allowlist.
 * - Sanitizes trailing slashes, query parameters, URL fragments, and URL encoding.
 * - Filters platform-reserved paths and multi-segment non-profile paths.
 */
public final class PlatformUrlDetector {

    public record DetectedProfileUrl(
            CodingPlatform platform,
            String username,
            String canonicalProfileUrl
    ) {}

    private static final Set<String> LEETCODE_RESERVED = Set.of(
            "problems", "contest", "discuss", "explore", "tag", "submissions",
            "developer", "playground", "assessment", "interview", "subscribe",
            "support", "terms", "privacy", "login", "register", "accounts", "profile",
            "studyplan", "problemset", "company", "circle"
    );

    private static final Set<String> GITHUB_RESERVED = Set.of(
            "features", "pricing", "enterprise", "explore", "topics", "trending",
            "collections", "events", "security", "login", "join", "search", "settings",
            "marketplace", "organizations", "pulls", "issues", "notifications",
            "discussions", "sponsors", "about", "site", "contact", "privacy", "terms",
            "orgs", "new", "stars", "codespaces", "account", "logout", "readme",
            "dashboard", "apps", "sessions"
    );

    private static final Set<String> HACKERRANK_RESERVED = Set.of(
            "dashboard", "domains", "challenges", "contests", "leaderboard",
            "administration", "settings", "jobs", "work", "community", "login",
            "auth", "signup", "certificates", "support", "scoring", "faq"
    );

    private static final Set<String> GEEKSFORGEEKS_RESERVED = Set.of(
            "explore", "courses", "jobs", "practice", "tutorials", "batch",
            "contest", "events", "login", "register", "settings", "account",
            "terms", "privacy", "contact", "about", "write", "contribute",
            "category", "tag", "search"
    );

    private static final Set<String> ATCODER_SUBPAGES = Set.of("history", "submissions", "contests", "custom_test");

    private static final Pattern LEETCODE_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,30}$");
    private static final Pattern CODEFORCES_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,24}$");
    private static final Pattern GITHUB_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$");
    private static final Pattern ATCODER_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    private static final Pattern CODECHEF_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern HACKERRANK_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern GEEKSFORGEEKS_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,50}$");

    private PlatformUrlDetector() {
    }

    /**
     * Parses and validates a public profile URL, identifying the platform, extracting the username,
     * and constructing the canonical profile URL.
     *
     * @param rawUrl the raw URL input string pasted by the user
     * @return DetectedProfileUrl with platform, username, and canonical URL
     * @throws BadRequestException if the URL is invalid, domain unsupported, or path malformed
     */
    public static DetectedProfileUrl detect(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new BadRequestException("Profile URL cannot be empty");
        }

        String cleaned = rawUrl.trim();

        // 1. Normalize protocol if missing
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            cleaned = "https://" + cleaned;
        }

        // 2. Parse URI
        URI uri;
        try {
            uri = URI.create(cleaned);
        } catch (Exception ex) {
            throw new BadRequestException("Malformed profile URL: " + ex.getMessage());
        }

        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new BadRequestException("Invalid URL: missing host domain");
        }

        String normalizedHost = host.toLowerCase().trim();
        if (normalizedHost.startsWith("www.")) {
            normalizedHost = normalizedHost.substring(4);
        }

        // 3. Extract and sanitize path segments
        String path = uri.getPath();
        if (path == null || path.trim().isEmpty() || path.equals("/")) {
            throw new BadRequestException("URL path cannot be empty. Please provide a full profile URL.");
        }

        List<String> segments = Arrays.stream(path.split("/"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return URLDecoder.decode(s, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        return s;
                    }
                })
                .toList();

        if (segments.isEmpty()) {
            throw new BadRequestException("URL path does not contain a profile handle");
        }

        // 4. Match supported domain & extract handle
        return switch (normalizedHost) {
            case "leetcode.com" -> parseLeetCode(segments);
            case "codeforces.com" -> parseCodeforces(segments);
            case "github.com" -> parseGitHub(segments);
            case "atcoder.jp" -> parseAtCoder(segments);
            case "codechef.com" -> parseCodeChef(segments);
            case "hackerrank.com" -> parseHackerRank(segments);
            case "geeksforgeeks.org", "auth.geeksforgeeks.org" -> parseGeeksforGeeks(segments);
            default -> throw new BadRequestException(
                    "Unsupported platform domain: '" + host + "'. Supported platforms: LeetCode, Codeforces, GitHub, AtCoder, CodeChef, HackerRank, GeeksforGeeks."
            );
        };
    }

    private static DetectedProfileUrl parseLeetCode(List<String> segments) {
        String first = segments.getFirst();
        if (LEETCODE_RESERVED.contains(first.toLowerCase())) {
            throw new BadRequestException("Invalid LeetCode profile URL: '" + first + "' is a reserved platform path");
        }

        String username;
        if (segments.size() == 2 && first.equalsIgnoreCase("u")) {
            username = segments.get(1);
        } else if (segments.size() == 1) {
            username = first;
        } else {
            throw new BadRequestException("Invalid LeetCode profile URL structure. Expected: https://leetcode.com/u/{username}/");
        }

        if (!LEETCODE_USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("Invalid LeetCode username format extracted from URL: '" + username + "'");
        }

        return new DetectedProfileUrl(CodingPlatform.LEETCODE, username, CodingPlatform.LEETCODE.buildProfileUrl(username));
    }

    private static DetectedProfileUrl parseCodeforces(List<String> segments) {
        if (segments.size() != 2 || !segments.getFirst().equalsIgnoreCase("profile")) {
            throw new BadRequestException("Invalid Codeforces profile URL structure. Expected: https://codeforces.com/profile/{username}");
        }

        String username = segments.get(1);
        if (!CODEFORCES_USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("Invalid Codeforces handle format extracted from URL: '" + username + "'");
        }

        return new DetectedProfileUrl(CodingPlatform.CODEFORCES, username, CodingPlatform.CODEFORCES.buildProfileUrl(username));
    }

    private static DetectedProfileUrl parseGitHub(List<String> segments) {
        if (segments.size() > 1) {
            throw new BadRequestException("GitHub repository or project URLs are not supported as personal profiles. Expected: https://github.com/{username}");
        }

        String username = segments.getFirst();
        if (GITHUB_RESERVED.contains(username.toLowerCase())) {
            throw new BadRequestException("Invalid GitHub profile URL: '" + username + "' is a reserved platform path");
        }

        if (!GITHUB_USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("Invalid GitHub username format extracted from URL: '" + username + "'");
        }

        return new DetectedProfileUrl(CodingPlatform.GITHUB, username, CodingPlatform.GITHUB.buildProfileUrl(username));
    }

    private static DetectedProfileUrl parseAtCoder(List<String> segments) {
        if (!segments.getFirst().equalsIgnoreCase("users") ||
                (segments.size() != 2 && !(segments.size() == 3 && ATCODER_SUBPAGES.contains(segments.get(2).toLowerCase())))) {
            throw new BadRequestException("Invalid AtCoder profile URL structure. Expected: https://atcoder.jp/users/{username}");
        }

        String username = segments.get(1);
        if (!ATCODER_USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("Invalid AtCoder username format extracted from URL: '" + username + "'");
        }

        return new DetectedProfileUrl(CodingPlatform.ATCODER, username, CodingPlatform.ATCODER.buildProfileUrl(username));
    }

    private static DetectedProfileUrl parseCodeChef(List<String> segments) {
        if (segments.size() != 2 || !segments.getFirst().equalsIgnoreCase("users")) {
            throw new BadRequestException("Invalid CodeChef profile URL structure. Expected: https://www.codechef.com/users/{username}");
        }

        String username = segments.get(1);
        if (!CODECHEF_USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("Invalid CodeChef username format extracted from URL: '" + username + "'");
        }

        return new DetectedProfileUrl(CodingPlatform.CODECHEF, username, CodingPlatform.CODECHEF.buildProfileUrl(username));
    }

    private static DetectedProfileUrl parseHackerRank(List<String> segments) {
        String username;
        if (segments.size() == 2 && segments.getFirst().equalsIgnoreCase("profile")) {
            username = segments.get(1);
        } else if (segments.size() == 1) {
            String seg = segments.getFirst();
            if (HACKERRANK_RESERVED.contains(seg.toLowerCase())) {
                throw new BadRequestException("Invalid HackerRank profile URL: '" + seg + "' is a reserved platform path");
            }
            username = seg;
        } else {
            throw new BadRequestException("Invalid HackerRank profile URL structure. Expected: https://www.hackerrank.com/profile/{username}");
        }

        if (!HACKERRANK_USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("Invalid HackerRank username format extracted from URL: '" + username + "'");
        }

        return new DetectedProfileUrl(CodingPlatform.HACKERRANK, username, CodingPlatform.HACKERRANK.buildProfileUrl(username));
    }

    private static DetectedProfileUrl parseGeeksforGeeks(List<String> segments) {
        String first = segments.getFirst();
        String username;

        if (segments.size() >= 2 && (first.equalsIgnoreCase("user") || first.equalsIgnoreCase("profile"))) {
            username = segments.get(1);
            if (segments.size() == 3 && !segments.get(2).equalsIgnoreCase("profile")) {
                throw new BadRequestException("Invalid GeeksforGeeks profile URL structure. Expected: https://www.geeksforgeeks.org/user/{username}/");
            } else if (segments.size() > 3) {
                throw new BadRequestException("Invalid GeeksforGeeks profile URL structure. Expected: https://www.geeksforgeeks.org/user/{username}/");
            }
        } else if (segments.size() == 1) {
            if (GEEKSFORGEEKS_RESERVED.contains(first.toLowerCase())) {
                throw new BadRequestException("Invalid GeeksforGeeks profile URL: '" + first + "' is a reserved platform path");
            }
            username = first;
        } else {
            throw new BadRequestException("Invalid GeeksforGeeks profile URL structure. Expected: https://www.geeksforgeeks.org/user/{username}/");
        }

        if (GEEKSFORGEEKS_RESERVED.contains(username.toLowerCase())) {
            throw new BadRequestException("Invalid GeeksforGeeks profile URL: '" + username + "' is a reserved platform path");
        }

        if (!GEEKSFORGEEKS_USERNAME_PATTERN.matcher(username).matches()) {
            throw new BadRequestException("Invalid GeeksforGeeks username format extracted from URL: '" + username + "'");
        }

        return new DetectedProfileUrl(CodingPlatform.GEEKSFORGEEKS, username, CodingPlatform.GEEKSFORGEEKS.buildProfileUrl(username));
    }
}
