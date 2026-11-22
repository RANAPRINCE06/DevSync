package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter integrating AtCoder competitive programming platform via the community-backed
 * AtCoder Problems API (https://kenkoooo.com/atcoder/atcoder-api/v3).
 *
 * In accordance with DevSync reliability and legal integrity specifications:
 * - Direct scraping of atcoder.jp is strictly avoided to comply with terms of service.
 * - Sourced metrics are labeled as community data.
 * - Solved problem counts, rated point sum, and problem streaks are tracked.
 * - Contest ratings and difficulty tiers remain null rather than fabricated.
 */
@Component
public class AtCoderAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(AtCoderAdapter.class);

    private final CodingPlatformClient client;
    private final String baseUrl;
    private final boolean enabled;

    public AtCoderAdapter(CodingIntegrationProperties properties) {
        this(null, properties);
    }

    @Autowired
    public AtCoderAdapter(CodingPlatformClient client, CodingIntegrationProperties properties) {
        this.client = client;
        this.baseUrl = properties.getAtcoder().getBaseUrl();
        this.enabled = properties.getAtcoder().isEnabled();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.ATCODER;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,16}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid AtCoder username format: " + username);
        }

        if (!enabled || client == null) {
            throw new PlatformUnsupportedException("AtCoder integration is currently disabled in configuration.");
        }

        // 1. Fetch User Accepted Problems Count and AC Rank
        String acUrl = baseUrl + "/user/ac_rank?user=" + username;
        AtCoderRankResponse acResponse;
        try {
            acResponse = client.get(acUrl, AtCoderRankResponse.class, null);
        } catch (PlatformUserNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PlatformIntegrationException("Failed to fetch AtCoder statistics for user: " + username + " - " + ex.getMessage(), ex);
        }

        if (acResponse == null) {
            throw new PlatformUserNotFoundException("AtCoder user not found or empty profile: " + username);
        }

        Integer problemsSolved = acResponse.getCount();
        Long acRank = acResponse.getRank() != null ? acResponse.getRank().longValue() : null;

        Map<String, Object> additionalStats = new HashMap<>();
        additionalStats.put("dataSource", "AtCoder Problems Community API (kenkoooo.com)");
        additionalStats.put("communityAcRank", acRank);

        // 2. Fetch Rated Point Sum Rank (optional/non-fatal)
        try {
            String pointUrl = baseUrl + "/user/rated_point_sum_rank?user=" + username;
            AtCoderRankResponse pointResponse = client.get(pointUrl, AtCoderRankResponse.class, null);
            if (pointResponse != null) {
                if (pointResponse.getCount() != null) {
                    additionalStats.put("ratedPointSum", pointResponse.getCount());
                }
                if (pointResponse.getRank() != null) {
                    additionalStats.put("ratedPointSumRank", pointResponse.getRank());
                }
            }
        } catch (Exception ex) {
            log.debug("Could not fetch AtCoder rated point sum for user {}: {}", username, ex.getMessage());
        }

        // 3. Fetch Streak Rank (optional/non-fatal)
        try {
            String streakUrl = baseUrl + "/user/streak_rank?user=" + username;
            AtCoderRankResponse streakResponse = client.get(streakUrl, AtCoderRankResponse.class, null);
            if (streakResponse != null) {
                if (streakResponse.getCount() != null) {
                    additionalStats.put("longestStreakDays", streakResponse.getCount());
                }
                if (streakResponse.getRank() != null) {
                    additionalStats.put("streakRank", streakResponse.getRank());
                }
            }
        } catch (Exception ex) {
            log.debug("Could not fetch AtCoder streak stats for user {}: {}", username, ex.getMessage());
        }

        return new ExternalProfileData(
                CodingPlatform.ATCODER,
                username,
                CodingPlatform.ATCODER.buildProfileUrl(username),
                null, // currentRating remains null (not available via community AC API without HTML scraping)
                null, // maximumRating
                acRank, // globalRank mapped to community AC rank
                null, // countryRank
                problemsSolved,
                null, // easySolved
                null, // mediumSolved
                null, // hardSolved
                null, // contestsParticipated
                null, // platformRank
                additionalStats,
                Instant.now()
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtCoderRankResponse {
        @JsonProperty("count")
        private Integer count;

        @JsonProperty("rank")
        private Integer rank;

        public AtCoderRankResponse() {
        }

        public AtCoderRankResponse(Integer count, Integer rank) {
            this.count = count;
            this.rank = rank;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }
    }
}
