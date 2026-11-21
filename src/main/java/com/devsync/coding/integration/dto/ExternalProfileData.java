package com.devsync.coding.integration.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;
import java.util.Map;

/**
 * Normalized DTO capturing point-in-time statistics retrieved from an external
 * competitive programming or developer platform.
 *
 * Invariants:
 * - All statistical metrics are strictly nullable.
 * - Missing or unsupported statistics remain null and are NEVER replaced with fabricated zeroes.
 * - Ratings/ranks are platform-specific and must never be cross-compared.
 */
public class ExternalProfileData {

    private CodingPlatform platform;
    private String username;
    private String profileUrl;
    private Double currentRating;
    private Double maximumRating;
    private Long globalRank;
    private Long countryRank;
    private Integer problemsSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer contestsParticipated;
    private String platformRank;
    private Map<String, Object> additionalStats;
    private Instant fetchedAt;

    public ExternalProfileData() {
    }

    public ExternalProfileData(CodingPlatform platform, String username, String profileUrl,
                               Double currentRating, Double maximumRating, Long globalRank, Long countryRank,
                               Integer problemsSolved, Integer easySolved, Integer mediumSolved, Integer hardSolved,
                               Integer contestsParticipated, String platformRank,
                               Map<String, Object> additionalStats, Instant fetchedAt) {
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.currentRating = currentRating;
        this.maximumRating = maximumRating;
        this.globalRank = globalRank;
        this.countryRank = countryRank;
        this.problemsSolved = problemsSolved;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.contestsParticipated = contestsParticipated;
        this.platformRank = platformRank;
        this.additionalStats = additionalStats;
        this.fetchedAt = fetchedAt != null ? fetchedAt : Instant.now();
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Double getCurrentRating() {
        return currentRating;
    }

    public void setCurrentRating(Double currentRating) {
        this.currentRating = currentRating;
    }

    public Double getMaximumRating() {
        return maximumRating;
    }

    public void setMaximumRating(Double maximumRating) {
        this.maximumRating = maximumRating;
    }

    public Long getGlobalRank() {
        return globalRank;
    }

    public void setGlobalRank(Long globalRank) {
        this.globalRank = globalRank;
    }

    public Long getCountryRank() {
        return countryRank;
    }

    public void setCountryRank(Long countryRank) {
        this.countryRank = countryRank;
    }

    public Integer getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(Integer problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public Integer getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(Integer easySolved) {
        this.easySolved = easySolved;
    }

    public Integer getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(Integer mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public Integer getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(Integer hardSolved) {
        this.hardSolved = hardSolved;
    }

    public Integer getContestsParticipated() {
        return contestsParticipated;
    }

    public void setContestsParticipated(Integer contestsParticipated) {
        this.contestsParticipated = contestsParticipated;
    }

    public String getPlatformRank() {
        return platformRank;
    }

    public void setPlatformRank(String platformRank) {
        this.platformRank = platformRank;
    }

    public Map<String, Object> getAdditionalStats() {
        return additionalStats;
    }

    public void setAdditionalStats(Map<String, Object> additionalStats) {
        this.additionalStats = additionalStats;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CodingPlatform platform;
        private String username;
        private String profileUrl;
        private Double currentRating;
        private Double maximumRating;
        private Long globalRank;
        private Long countryRank;
        private Integer problemsSolved;
        private Integer easySolved;
        private Integer mediumSolved;
        private Integer hardSolved;
        private Integer contestsParticipated;
        private String platformRank;
        private Map<String, Object> additionalStats;
        private Instant fetchedAt;

        public Builder platform(CodingPlatform platform) {
            this.platform = platform;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public Builder currentRating(Double currentRating) {
            this.currentRating = currentRating;
            return this;
        }

        public Builder maximumRating(Double maximumRating) {
            this.maximumRating = maximumRating;
            return this;
        }

        public Builder globalRank(Long globalRank) {
            this.globalRank = globalRank;
            return this;
        }

        public Builder countryRank(Long countryRank) {
            this.countryRank = countryRank;
            return this;
        }

        public Builder problemsSolved(Integer problemsSolved) {
            this.problemsSolved = problemsSolved;
            return this;
        }

        public Builder easySolved(Integer easySolved) {
            this.easySolved = easySolved;
            return this;
        }

        public Builder mediumSolved(Integer mediumSolved) {
            this.mediumSolved = mediumSolved;
            return this;
        }

        public Builder hardSolved(Integer hardSolved) {
            this.hardSolved = hardSolved;
            return this;
        }

        public Builder contestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
            return this;
        }

        public Builder platformRank(String platformRank) {
            this.platformRank = platformRank;
            return this;
        }

        public Builder additionalStats(Map<String, Object> additionalStats) {
            this.additionalStats = additionalStats;
            return this;
        }

        public Builder fetchedAt(Instant fetchedAt) {
            this.fetchedAt = fetchedAt;
            return this;
        }

        public ExternalProfileData build() {
            return new ExternalProfileData(platform, username, profileUrl, currentRating, maximumRating,
                    globalRank, countryRank, problemsSolved, easySolved, mediumSolved, hardSolved,
                    contestsParticipated, platformRank, additionalStats, fetchedAt);
        }
    }
}
