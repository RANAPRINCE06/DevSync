package com.devsync.coding.analytics.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;
import java.util.List;

public class CodingAnalyticsOverviewResponse {

    private Integer totalProblemsSolved;
    private int connectedPlatformsCount;
    private Double highestRating;
    private CodingPlatform highestRatingPlatform;
    private Integer totalContestsParticipated;
    private long totalSnapshotsCollected;
    private Instant firstSnapshotAt;
    private Instant lastSnapshotAt;
    private List<PlatformOverviewItem> platformOverviews;

    public CodingAnalyticsOverviewResponse() {
    }

    public CodingAnalyticsOverviewResponse(Integer totalProblemsSolved, int connectedPlatformsCount,
                                          Double highestRating, CodingPlatform highestRatingPlatform,
                                          Integer totalContestsParticipated, long totalSnapshotsCollected,
                                          Instant firstSnapshotAt, Instant lastSnapshotAt,
                                          List<PlatformOverviewItem> platformOverviews) {
        this.totalProblemsSolved = totalProblemsSolved;
        this.connectedPlatformsCount = connectedPlatformsCount;
        this.highestRating = highestRating;
        this.highestRatingPlatform = highestRatingPlatform;
        this.totalContestsParticipated = totalContestsParticipated;
        this.totalSnapshotsCollected = totalSnapshotsCollected;
        this.firstSnapshotAt = firstSnapshotAt;
        this.lastSnapshotAt = lastSnapshotAt;
        this.platformOverviews = platformOverviews;
    }

    public Integer getTotalProblemsSolved() {
        return totalProblemsSolved;
    }

    public void setTotalProblemsSolved(Integer totalProblemsSolved) {
        this.totalProblemsSolved = totalProblemsSolved;
    }

    public int getConnectedPlatformsCount() {
        return connectedPlatformsCount;
    }

    public void setConnectedPlatformsCount(int connectedPlatformsCount) {
        this.connectedPlatformsCount = connectedPlatformsCount;
    }

    public Double getHighestRating() {
        return highestRating;
    }

    public void setHighestRating(Double highestRating) {
        this.highestRating = highestRating;
    }

    public CodingPlatform getHighestRatingPlatform() {
        return highestRatingPlatform;
    }

    public void setHighestRatingPlatform(CodingPlatform highestRatingPlatform) {
        this.highestRatingPlatform = highestRatingPlatform;
    }

    public Integer getTotalContestsParticipated() {
        return totalContestsParticipated;
    }

    public void setTotalContestsParticipated(Integer totalContestsParticipated) {
        this.totalContestsParticipated = totalContestsParticipated;
    }

    public long getTotalSnapshotsCollected() {
        return totalSnapshotsCollected;
    }

    public void setTotalSnapshotsCollected(long totalSnapshotsCollected) {
        this.totalSnapshotsCollected = totalSnapshotsCollected;
    }

    public Instant getFirstSnapshotAt() {
        return firstSnapshotAt;
    }

    public void setFirstSnapshotAt(Instant firstSnapshotAt) {
        this.firstSnapshotAt = firstSnapshotAt;
    }

    public Instant getLastSnapshotAt() {
        return lastSnapshotAt;
    }

    public void setLastSnapshotAt(Instant lastSnapshotAt) {
        this.lastSnapshotAt = lastSnapshotAt;
    }

    public List<PlatformOverviewItem> getPlatformOverviews() {
        return platformOverviews;
    }

    public void setPlatformOverviews(List<PlatformOverviewItem> platformOverviews) {
        this.platformOverviews = platformOverviews;
    }

    public static class PlatformOverviewItem {
        private CodingPlatform platform;
        private String username;
        private Integer problemsSolved;
        private Double currentRating;
        private Long globalRank;
        private Integer contestsParticipated;
        private Instant lastSyncedAt;

        public PlatformOverviewItem() {
        }

        public PlatformOverviewItem(CodingPlatform platform, String username, Integer problemsSolved,
                                    Double currentRating, Long globalRank, Integer contestsParticipated,
                                    Instant lastSyncedAt) {
            this.platform = platform;
            this.username = username;
            this.problemsSolved = problemsSolved;
            this.currentRating = currentRating;
            this.globalRank = globalRank;
            this.contestsParticipated = contestsParticipated;
            this.lastSyncedAt = lastSyncedAt;
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

        public Integer getProblemsSolved() {
            return problemsSolved;
        }

        public void setProblemsSolved(Integer problemsSolved) {
            this.problemsSolved = problemsSolved;
        }

        public Double getCurrentRating() {
            return currentRating;
        }

        public void setCurrentRating(Double currentRating) {
            this.currentRating = currentRating;
        }

        public Long getGlobalRank() {
            return globalRank;
        }

        public void setGlobalRank(Long globalRank) {
            this.globalRank = globalRank;
        }

        public Integer getContestsParticipated() {
            return contestsParticipated;
        }

        public void setContestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
        }

        public Instant getLastSyncedAt() {
            return lastSyncedAt;
        }

        public void setLastSyncedAt(Instant lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
        }
    }
}
