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
            "    badges {\n" +
            "      id\n" +
            "      name\n" +
            "      displayName\n" +
            "      icon\n" +
            "      creationDate\n" +
            "      category\n" +
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
            "  userContestRankingHistory(username: $username) {\n" +
            "    attended\n" +
            "    rating\n" +
            "    ranking\n" +
            "    problemsSolved\n" +
            "    contest {\n" +
            "      title\n" +
            "      startTime\n" +
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

        // Extract badges
        if (matchedUser.badges != null && !matchedUser.badges.isEmpty()) {
            List<Map<String, Object>> badgesList = new ArrayList<>();
            for (LeetCodeUserBadge b : matchedUser.badges) {
                Map<String, Object> bm = new LinkedHashMap<>();
                bm.put("id", b.id != null ? b.id : b.name);
                bm.put("name", b.displayName != null ? b.displayName : b.name);
                bm.put("description", "LeetCode Achievement Badge");
                bm.put("icon", b.icon != null && b.icon.startsWith("http") ? b.icon : (b.icon != null ? "https://leetcode.com" + b.icon : null));
                bm.put("category", b.category != null ? b.category : "BADGE");
                bm.put("level", "Achievement");
                if (b.creationDate != null) {
                    bm.put("earnedDate", b.creationDate);
                }
                badgesList.add(bm);
            }
            if (!badgesList.isEmpty()) {
                additionalStats.put("badges", badgesList);
            }
        }

        // Extract contest history
        if (response.data != null && response.data.userContestRankingHistory != null && !response.data.userContestRankingHistory.isEmpty()) {
            List<Map<String, Object>> contestList = new ArrayList<>();
            Double prevRating = null;
            for (LeetCodeContestHistoryItem item : response.data.userContestRankingHistory) {
                if (Boolean.TRUE.equals(item.attended) && item.contest != null && item.rating != null) {
                    Map<String, Object> cm = new LinkedHashMap<>();
                    cm.put("contestId", item.contest.title);
                    cm.put("contestName", item.contest.title);
                    if (item.contest.startTime != null) {
                        cm.put("contestDate", Instant.ofEpochSecond(item.contest.startTime).toString());
                    } else {
                        cm.put("contestDate", Instant.now().toString());
                    }
                    cm.put("contestRank", item.ranking);
                    cm.put("previousRating", prevRating != null ? prevRating : item.rating);
                    cm.put("ratingChange", prevRating != null ? item.rating - prevRating : 0.0);
                    cm.put("newRating", item.rating);
                    cm.put("problemsSolved", item.problemsSolved != null ? item.problemsSolved : 0);
                    contestList.add(cm);
                    prevRating = item.rating;
                }
            }
            if (!contestList.isEmpty()) {
                additionalStats.put("contestHistory", contestList);
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
        public List<LeetCodeContestHistoryItem> userContestRankingHistory;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeMatchedUser {
        public String username;
        public LeetCodeProfile profile;
        public LeetCodeSubmitStats submitStatsGlobal;
        public List<LeetCodeUserBadge> badges;
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
    public static class LeetCodeUserBadge {
        public String id;
        public String name;
        public String displayName;
        public String icon;
        public String hoverText;
        public String category;
        public String creationDate;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeContestHistoryItem {
        public Boolean attended;
        public Double rating;
        public Integer ranking;
        public Integer problemsSolved;
        public LeetCodeContest contest;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeContest {
        public String title;
        public Long startTime;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeetCodeError {
        public String message;
    }
}
