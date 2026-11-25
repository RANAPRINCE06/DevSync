package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class CodeforcesAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(CodeforcesAdapter.class);

    private final CodingPlatformClient client;
    private final String baseUrl;

    public CodeforcesAdapter(CodingPlatformClient client, CodingIntegrationProperties properties) {
        this.client = client;
        this.baseUrl = properties.getCodeforces().getBaseUrl();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.CODEFORCES;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_.-]{3,24}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid Codeforces username format: " + username);
        }

        // 1. Fetch User Information (user.info)
        String userInfoUrl = baseUrl + "/user.info?handles=" + username;
        CodeforcesUserInfoResponse userInfoResponse = client.get(userInfoUrl, CodeforcesUserInfoResponse.class, null);

        if (userInfoResponse == null || !"OK".equalsIgnoreCase(userInfoResponse.status)) {
            String comment = userInfoResponse != null ? userInfoResponse.comment : "Empty response from Codeforces";
            if (comment != null && comment.toLowerCase().contains("not found")) {
                throw new PlatformUserNotFoundException("Codeforces user handle not found: " + username);
            }
            throw new PlatformIntegrationException("Codeforces API error: " + comment);
        }

        if (userInfoResponse.result == null || userInfoResponse.result.isEmpty()) {
            throw new PlatformUserNotFoundException("Codeforces user result empty for handle: " + username);
        }

        CodeforcesUser user = userInfoResponse.result.getFirst();

        // 2. Fetch User Rating History (user.rating) for contest count and progression
        Integer contestsParticipated = null;
        List<Map<String, Object>> cfContestHistory = new ArrayList<>();
        try {
            String userRatingUrl = baseUrl + "/user.rating?handle=" + username;
            CodeforcesRatingResponse ratingResponse = client.get(userRatingUrl, CodeforcesRatingResponse.class, null);
            if (ratingResponse != null && "OK".equalsIgnoreCase(ratingResponse.status) && ratingResponse.result != null) {
                contestsParticipated = ratingResponse.result.size();
                for (CodeforcesRatingChange change : ratingResponse.result) {
                    Map<String, Object> cm = new LinkedHashMap<>();
                    cm.put("contestId", String.valueOf(change.contestId));
                    cm.put("contestName", change.contestName != null ? change.contestName : "Codeforces Round #" + change.contestId);
                    if (change.ratingUpdateTimeSeconds != null) {
                        cm.put("contestDate", Instant.ofEpochSecond(change.ratingUpdateTimeSeconds).toString());
                    } else {
                        cm.put("contestDate", Instant.now().toString());
                    }
                    cm.put("contestRank", change.rank);
                    cm.put("previousRating", change.oldRating != null ? change.oldRating.doubleValue() : null);
                    cm.put("ratingChange", (change.newRating != null && change.oldRating != null) ? (double)(change.newRating - change.oldRating) : 0.0);
                    cm.put("newRating", change.newRating != null ? change.newRating.doubleValue() : 0.0);
                    cfContestHistory.add(cm);
                }
            }
        } catch (Exception ex) {
            log.debug("Could not retrieve Codeforces contest history for {}: {}", username, ex.getMessage());
        }

        // 3. Fetch Recent Submissions (user.status) for verified unique accepted problems
        Integer uniqueProblemsSolved = null;
        Integer rawAcceptedCount = null;
        Integer submissionsChecked = null;
        try {
            String userStatusUrl = baseUrl + "/user.status?handle=" + username + "&from=1&count=500";
            CodeforcesStatusResponse statusResponse = client.get(userStatusUrl, CodeforcesStatusResponse.class, null);
            if (statusResponse != null && "OK".equalsIgnoreCase(statusResponse.status) && statusResponse.result != null) {
                Set<String> uniqueSolvedKeys = new HashSet<>();
                int okCount = 0;
                submissionsChecked = statusResponse.result.size();

                for (CodeforcesSubmission sub : statusResponse.result) {
                    if ("OK".equalsIgnoreCase(sub.verdict)) {
                        okCount++;
                        if (sub.problem != null) {
                            String key = sub.problem.contestId != null
                                    ? sub.problem.contestId + "-" + sub.problem.index
                                    : sub.problem.name;
                            if (key != null) {
                                uniqueSolvedKeys.add(key);
                            }
                        }
                    }
                }
                uniqueProblemsSolved = uniqueSolvedKeys.size();
                rawAcceptedCount = okCount;
            }
        } catch (Exception ex) {
            log.debug("Could not retrieve Codeforces submissions for {}: {}", username, ex.getMessage());
        }

        // 4. Construct Additional Stats Map
        Map<String, Object> additionalStats = new LinkedHashMap<>();
        if (user.contribution != null) additionalStats.put("contribution", user.contribution);
        if (user.maxRank != null) additionalStats.put("maxRank", user.maxRank);
        if (user.country != null) additionalStats.put("country", user.country);
        if (user.city != null) additionalStats.put("city", user.city);
        if (user.organization != null) additionalStats.put("organization", user.organization);
        if (rawAcceptedCount != null) additionalStats.put("rawAcceptedSubmissions", rawAcceptedCount);
        if (submissionsChecked != null) additionalStats.put("submissionsChecked", submissionsChecked);
        if (user.titlePhoto != null && !user.titlePhoto.trim().isEmpty()) {
            additionalStats.put("avatarUrl", user.titlePhoto.trim());
        } else if (user.avatar != null && !user.avatar.trim().isEmpty()) {
            additionalStats.put("avatarUrl", user.avatar.trim());
        }
        String fullName = ((user.firstName != null ? user.firstName : "") + " " +
                           (user.lastName != null ? user.lastName : "")).trim();
        if (!fullName.isEmpty()) {
            additionalStats.put("displayName", fullName);
        }

        // 5. Construct Codeforces Badges & Achievements
        List<Map<String, Object>> cfBadges = new ArrayList<>();
        if (user.rank != null && !user.rank.trim().isEmpty()) {
            Map<String, Object> rankBadge = new LinkedHashMap<>();
            rankBadge.put("id", "cf-rank-" + user.rank.toLowerCase().replace(" ", "-"));
            rankBadge.put("name", capitalize(user.rank));
            rankBadge.put("description", "Codeforces Official Rank: " + capitalize(user.rank));
            rankBadge.put("category", "RANK_TITLE");
            rankBadge.put("level", capitalize(user.rank));
            cfBadges.add(rankBadge);
        }
        if (user.maxRank != null && !user.maxRank.equalsIgnoreCase(user.rank)) {
            Map<String, Object> maxRankBadge = new LinkedHashMap<>();
            maxRankBadge.put("id", "cf-max-rank-" + user.maxRank.toLowerCase().replace(" ", "-"));
            maxRankBadge.put("name", "Peak: " + capitalize(user.maxRank));
            maxRankBadge.put("description", "Codeforces Historical Peak Rank: " + capitalize(user.maxRank));
            maxRankBadge.put("category", "RANK_TITLE");
            maxRankBadge.put("level", capitalize(user.maxRank));
            cfBadges.add(maxRankBadge);
        }
        if (user.rating != null && user.rating >= 1200) {
            Map<String, Object> ratingBadge = new LinkedHashMap<>();
            String tier = user.rating >= 2100 ? "Master" : (user.rating >= 1900 ? "Candidate Master" : (user.rating >= 1600 ? "Expert" : (user.rating >= 1400 ? "Specialist" : "Pupil")));
            ratingBadge.put("id", "cf-rating-" + tier.toLowerCase().replace(" ", "-"));
            ratingBadge.put("name", user.rating + "+ Rating");
            ratingBadge.put("description", "Achieved " + user.rating + " rating on Codeforces (" + tier + ")");
            ratingBadge.put("category", "RATING_MILESTONE");
            ratingBadge.put("level", tier);
            cfBadges.add(ratingBadge);
        }
        if (contestsParticipated != null && contestsParticipated >= 5) {
            Map<String, Object> contestBadge = new LinkedHashMap<>();
            String cLevel = contestsParticipated >= 50 ? "Master Competitor" : (contestsParticipated >= 20 ? "Veteran Competitor" : "Active Competitor");
            contestBadge.put("id", "cf-contests-" + (contestsParticipated >= 50 ? "50" : (contestsParticipated >= 20 ? "20" : "5")));
            contestBadge.put("name", contestsParticipated + "+ Rounds");
            contestBadge.put("description", "Participated in " + contestsParticipated + " official Codeforces rounds");
            contestBadge.put("category", "CONTEST_MILESTONE");
            contestBadge.put("level", cLevel);
            cfBadges.add(contestBadge);
        }
        if (!cfContestHistory.isEmpty()) {
            additionalStats.put("contestHistory", cfContestHistory);
        }
        if (!cfBadges.isEmpty()) {
            additionalStats.put("badges", cfBadges);
        }

        String profileUrl = CodingPlatform.CODEFORCES.buildProfileUrl(username);

        return ExternalProfileData.builder()
                .platform(CodingPlatform.CODEFORCES)
                .username(username)
                .profileUrl(profileUrl)
                .currentRating(user.rating != null ? user.rating.doubleValue() : null)
                .maximumRating(user.maxRating != null ? user.maxRating.doubleValue() : null)
                .platformRank(user.rank)
                .globalRank(null) // Codeforces API does not provide global rank in user.info
                .countryRank(null)
                .problemsSolved(uniqueProblemsSolved)
                .easySolved(null)
                .mediumSolved(null)
                .hardSolved(null)
                .contestsParticipated(contestsParticipated)
                .additionalStats(additionalStats.isEmpty() ? null : additionalStats)
                .fetchedAt(Instant.now())
                .build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    // =========================================================================
    // Codeforces Response DTOs
    // =========================================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesUserInfoResponse {
        public String status;
        public String comment;
        public List<CodeforcesUser> result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesUser {
        public String handle;
        public Integer rating;
        public Integer maxRating;
        public String rank;
        public String maxRank;
        public Integer contribution;
        public String country;
        public String city;
        public String organization;
        public String titlePhoto;
        public String avatar;
        public String firstName;
        public String lastName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesRatingResponse {
        public String status;
        public String comment;
        public List<CodeforcesRatingChange> result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesRatingChange {
        public Integer contestId;
        public String contestName;
        public Integer rank;
        public Integer oldRating;
        public Integer newRating;
        public Long ratingUpdateTimeSeconds;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesStatusResponse {
        public String status;
        public String comment;
        public List<CodeforcesSubmission> result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesSubmission {
        public Long id;
        public Integer contestId;
        public String verdict;
        public CodeforcesProblem problem;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesProblem {
        public Integer contestId;
        public String index;
        public String name;
        public Integer rating;
        public List<String> tags;
    }
}
