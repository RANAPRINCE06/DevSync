package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class LeetCodeAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeAdapter.class);

    private static final String LEETCODE_GRAPHQL_QUERY =
            "query getUserProfile($username: String!) {\n" +
            "  matchedUser(username: $username) {\n" +
            "    username\n" +
            "    profile {\n" +
            "      ranking\n" +
            "      reputation\n" +
            "      realName\n" +
            "      userAvatar\n" +
            "    }\n" +
            "    submitStatsGlobal {\n" +
            "      acSubmissionNum {\n" +
            "        difficulty\n" +
            "        count\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  userContestRanking(username: $username) {\n" +
            "    attendedContestsCount\n" +
            "    rating\n" +
            "    globalRanking\n" +
            "    totalParticipants\n" +
            "    topPercentage\n" +
            "    badge {\n" +
            "      name\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private final CodingPlatformClient client;
    private final String baseUrl;

    public LeetCodeAdapter(CodingPlatformClient client, CodingIntegrationProperties properties) {
        this.client = client;
        this.baseUrl = properties.getLeetcode().getBaseUrl();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.LEETCODE;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_-]{3,30}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid LeetCode username format: " + username);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("query", LEETCODE_GRAPHQL_QUERY);
        payload.put("variables", Map.of("username", username));

        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) DevSync-CodingIntelligence/1.0"
        );

        LeetCodeGraphQLResponse response;
        try {
            response = client.post(baseUrl, payload, LeetCodeGraphQLResponse.class, headers);
        } catch (Exception ex) {
            log.warn("Error calling LeetCode GraphQL API for {}: {}", username, ex.getMessage());
            throw new PlatformIntegrationException("LeetCode GraphQL request failed: " + ex.getMessage(), ex);
        }

        if (response == null || response.data == null || response.data.matchedUser == null) {
            if (response != null && response.errors != null && !response.errors.isEmpty()) {
                String errorMsg = response.errors.getFirst().message;
                throw new PlatformIntegrationException("LeetCode GraphQL error: " + errorMsg);
            }
            throw new PlatformUserNotFoundException("LeetCode user not found: " + username);
        }

        LeetCodeMatchedUser matchedUser = response.data.matchedUser;
        LeetCodeContestRanking contestRanking = response.data.userContestRanking;

        // Parse solved problems by difficulty
        Integer totalSolved = null;
        Integer easySolved = null;
        Integer mediumSolved = null;
        Integer hardSolved = null;

        if (matchedUser.submitStatsGlobal != null && matchedUser.submitStatsGlobal.acSubmissionNum != null) {
            for (LeetCodeSubmissionCount item : matchedUser.submitStatsGlobal.acSubmissionNum) {
                if ("All".equalsIgnoreCase(item.difficulty)) {
                    totalSolved = item.count;
                } else if ("Easy".equalsIgnoreCase(item.difficulty)) {
                    easySolved = item.count;
                } else if ("Medium".equalsIgnoreCase(item.difficulty)) {
                    mediumSolved = item.count;
                } else if ("Hard".equalsIgnoreCase(item.difficulty)) {
                    hardSolved = item.count;
                }
            }
        }

        // Parse contest statistics (strictly nullable if user has not entered contests)
        Double currentRating = null;
        Long globalRank = null;
        Integer contestsParticipated = null;
        String platformRank = null;

        Map<String, Object> additionalStats = new LinkedHashMap<>();

        if (contestRanking != null) {
            currentRating = contestRanking.rating != null ? Math.round(contestRanking.rating * 100.0) / 100.0 : null;
            globalRank = contestRanking.globalRanking != null ? contestRanking.globalRanking.longValue() : null;
            contestsParticipated = contestRanking.attendedContestsCount;
            if (contestRanking.badge != null) {
                platformRank = contestRanking.badge.name;
            }
            if (contestRanking.topPercentage != null) {
                additionalStats.put("topPercentage", contestRanking.topPercentage);
            }
            if (contestRanking.totalParticipants != null) {
                additionalStats.put("totalContestParticipants", contestRanking.totalParticipants);
            }
        }

        // Profile rankings from matchedUser
        if (matchedUser.profile != null) {
            if (globalRank == null && matchedUser.profile.ranking != null) {
                globalRank = matchedUser.profile.ranking.longValue();
            }
            if (matchedUser.profile.reputation != null) {
                additionalStats.put("reputation", matchedUser.profile.reputation);
            }
            if (matchedUser.profile.realName != null && !matchedUser.profile.realName.trim().isEmpty()) {
                additionalStats.put("displayName", matchedUser.profile.realName.trim());
            }
            if (matchedUser.profile.userAvatar != null && !matchedUser.profile.userAvatar.trim().isEmpty()) {
                additionalStats.put("avatarUrl", matchedUser.profile.userAvatar.trim());
            }
        }

        String profileUrl = CodingPlatform.LEETCODE.buildProfileUrl(username);

        return ExternalProfileData.builder()
                .platform(CodingPlatform.LEETCODE)
                .username(username)
                .profileUrl(profileUrl)
                .currentRating(currentRating)
                .maximumRating(currentRating) // LeetCode GraphQL exposes active contest rating
                .globalRank(globalRank)
                .countryRank(null)
                .problemsSolved(totalSolved)
                .easySolved(easySolved)
                .mediumSolved(mediumSolved)
                .hardSolved(hardSolved)
                .contestsParticipated(contestsParticipated)
                .platformRank(platformRank)
                .additionalStats(additionalStats.isEmpty() ? null : additionalStats)
                .fetchedAt(Instant.now())
                .build();
    }

    // =========================================================================
    // LeetCode GraphQL Response DTOs
    // =========================================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeGraphQLResponse {
        public LeetCodeData data;
        public List<LeetCodeError> errors;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeData {
        public LeetCodeMatchedUser matchedUser;
        public LeetCodeContestRanking userContestRanking;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeMatchedUser {
        public String username;
        public LeetCodeProfile profile;
        public LeetCodeSubmitStats submitStatsGlobal;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeProfile {
        public Integer ranking;
        public Integer reputation;
        public String realName;
        public String userAvatar;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeSubmitStats {
        public List<LeetCodeSubmissionCount> acSubmissionNum;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeSubmissionCount {
        public String difficulty;
        public Integer count;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeContestRanking {
        public Integer attendedContestsCount;
        public Double rating;
        public Integer globalRanking;
        public Integer totalParticipants;
        public Double topPercentage;
        public LeetCodeBadge badge;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeBadge {
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeError {
        public String message;
    }
}
