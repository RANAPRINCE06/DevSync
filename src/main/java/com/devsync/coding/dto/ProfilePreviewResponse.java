package com.devsync.coding.dto;

import com.devsync.coding.enums.CodingPlatform;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Preview information and live metrics retrieved for an external coding profile URL")
public class ProfilePreviewResponse {

    @Schema(description = "Identified coding platform")
    private CodingPlatform platform;

    @Schema(description = "User-facing display name of the platform")
    private String platformDisplayName;

    @Schema(description = "Extracted external username or handle")
    private String username;

    @Schema(description = "Canonical public profile URL on the target platform")
    private String canonicalProfileUrl;

    @Schema(description = "Whether the profile was verified to exist on the external platform")
    private boolean profileExists;

    @Schema(description = "Public display name, real name, or organization when available")
    private String displayName;

    @Schema(description = "Profile avatar image URL when available")
    private String avatarUrl;

    @Schema(description = "Whether automated metric synchronization is supported for this platform")
    private boolean autoSyncSupported;

    @Schema(description = "Platform-specific capability notes or restriction disclosures")
    private String capabilityNotice;

    @Schema(description = "Operational warning or rate-limit notification if any")
    private String warning;

    @Schema(description = "Whether the current user already has this platform connected")
    private boolean alreadyConnected;

    @Schema(description = "Preview statistics retrieved without database persistence")
    private PreviewMetrics metrics;

    public ProfilePreviewResponse() {
    }

    public ProfilePreviewResponse(CodingPlatform platform, String platformDisplayName, String username,
                                  String canonicalProfileUrl, boolean profileExists, String displayName,
                                  String avatarUrl, boolean autoSyncSupported, String capabilityNotice,
                                  String warning, boolean alreadyConnected, PreviewMetrics metrics) {
        this.platform = platform;
        this.platformDisplayName = platformDisplayName;
        this.username = username;
        this.canonicalProfileUrl = canonicalProfileUrl;
        this.profileExists = profileExists;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.autoSyncSupported = autoSyncSupported;
        this.capabilityNotice = capabilityNotice;
        this.warning = warning;
        this.alreadyConnected = alreadyConnected;
        this.metrics = metrics;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getPlatformDisplayName() {
        return platformDisplayName;
    }

    public void setPlatformDisplayName(String platformDisplayName) {
        this.platformDisplayName = platformDisplayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCanonicalProfileUrl() {
        return canonicalProfileUrl;
    }

    public void setCanonicalProfileUrl(String canonicalProfileUrl) {
        this.canonicalProfileUrl = canonicalProfileUrl;
    }

    public boolean isProfileExists() {
        return profileExists;
    }

    public void setProfileExists(boolean profileExists) {
        this.profileExists = profileExists;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isAutoSyncSupported() {
        return autoSyncSupported;
    }

    public void setAutoSyncSupported(boolean autoSyncSupported) {
        this.autoSyncSupported = autoSyncSupported;
    }

    public String getCapabilityNotice() {
        return capabilityNotice;
    }

    public void setCapabilityNotice(String capabilityNotice) {
        this.capabilityNotice = capabilityNotice;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public boolean isAlreadyConnected() {
        return alreadyConnected;
    }

    public void setAlreadyConnected(boolean alreadyConnected) {
        this.alreadyConnected = alreadyConnected;
    }

    public PreviewMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(PreviewMetrics metrics) {
        this.metrics = metrics;
    }

    @Schema(description = "Live statistics snapshot preview for the external profile")
    public static class PreviewMetrics {
        private Integer problemsSolved;
        private Integer easySolved;
        private Integer mediumSolved;
        private Integer hardSolved;
        private Double currentRating;
        private Double maximumRating;
        private Long globalRank;
        private String platformRank;
        private Integer contestsParticipated;
        private Integer publicRepos;
        private Integer totalStars;
        private Integer followers;
        private Map<String, Object> additionalStats;

        public PreviewMetrics() {
        }

        public PreviewMetrics(Integer problemsSolved, Integer easySolved, Integer mediumSolved, Integer hardSolved,
                              Double currentRating, Double maximumRating, Long globalRank, String platformRank,
                              Integer contestsParticipated, Integer publicRepos, Integer totalStars, Integer followers,
                              Map<String, Object> additionalStats) {
            this.problemsSolved = problemsSolved;
            this.easySolved = easySolved;
            this.mediumSolved = mediumSolved;
            this.hardSolved = hardSolved;
            this.currentRating = currentRating;
            this.maximumRating = maximumRating;
            this.globalRank = globalRank;
            this.platformRank = platformRank;
            this.contestsParticipated = contestsParticipated;
            this.publicRepos = publicRepos;
            this.totalStars = totalStars;
            this.followers = followers;
            this.additionalStats = additionalStats;
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

        public String getPlatformRank() {
            return platformRank;
        }

        public void setPlatformRank(String platformRank) {
            this.platformRank = platformRank;
        }

        public Integer getContestsParticipated() {
            return contestsParticipated;
        }

        public void setContestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
        }

        public Integer getPublicRepos() {
            return publicRepos;
        }

        public void setPublicRepos(Integer publicRepos) {
            this.publicRepos = publicRepos;
        }

        public Integer getTotalStars() {
            return totalStars;
        }

        public void setTotalStars(Integer totalStars) {
            this.totalStars = totalStars;
        }

        public Integer getFollowers() {
            return followers;
        }

        public void setFollowers(Integer followers) {
            this.followers = followers;
        }

        public Map<String, Object> getAdditionalStats() {
            return additionalStats;
        }

        public void setAdditionalStats(Map<String, Object> additionalStats) {
            this.additionalStats = additionalStats;
        }
    }
}
