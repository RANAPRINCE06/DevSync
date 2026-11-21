package com.devsync.coding.analytics.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;
import java.util.List;

public class ContestPerformanceResponse {

    private String period;
    private Instant from;
    private Instant to;
    private Integer totalContestsParticipated;
    private boolean anyContestsSupported;
    private List<PlatformContestRecord> platformContests;

    public ContestPerformanceResponse() {
    }

    public ContestPerformanceResponse(String period, Instant from, Instant to,
                                    Integer totalContestsParticipated, boolean anyContestsSupported,
                                    List<PlatformContestRecord> platformContests) {
        this.period = period;
        this.from = from;
        this.to = to;
        this.totalContestsParticipated = totalContestsParticipated;
        this.anyContestsSupported = anyContestsSupported;
        this.platformContests = platformContests;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public Integer getTotalContestsParticipated() {
        return totalContestsParticipated;
    }

    public void setTotalContestsParticipated(Integer totalContestsParticipated) {
        this.totalContestsParticipated = totalContestsParticipated;
    }

    public boolean isAnyContestsSupported() {
        return anyContestsSupported;
    }

    public void setAnyContestsSupported(boolean anyContestsSupported) {
        this.anyContestsSupported = anyContestsSupported;
    }

    public List<PlatformContestRecord> getPlatformContests() {
        return platformContests;
    }

    public void setPlatformContests(List<PlatformContestRecord> platformContests) {
        this.platformContests = platformContests;
    }

    public static class PlatformContestRecord {
        private CodingPlatform platform;
        private String username;
        private boolean contestsSupported;
        private Integer contestsParticipated;
        private Double currentRating;
        private Double bestRating;
        private Double ratingChange;
        private List<ContestRatingPoint> contestHistory;

        public PlatformContestRecord() {
        }

        public PlatformContestRecord(CodingPlatform platform, String username, boolean contestsSupported,
                                     Integer contestsParticipated, Double currentRating, Double bestRating,
                                     Double ratingChange, List<ContestRatingPoint> contestHistory) {
            this.platform = platform;
            this.username = username;
            this.contestsSupported = contestsSupported;
            this.contestsParticipated = contestsParticipated;
            this.currentRating = currentRating;
            this.bestRating = bestRating;
            this.ratingChange = ratingChange;
            this.contestHistory = contestHistory;
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

        public boolean isContestsSupported() {
            return contestsSupported;
        }

        public void setContestsSupported(boolean contestsSupported) {
            this.contestsSupported = contestsSupported;
        }

        public Integer getContestsParticipated() {
            return contestsParticipated;
        }

        public void setContestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
        }

        public Double getCurrentRating() {
            return currentRating;
        }

        public void setCurrentRating(Double currentRating) {
            this.currentRating = currentRating;
        }

        public Double getBestRating() {
            return bestRating;
        }

        public void setBestRating(Double bestRating) {
            this.bestRating = bestRating;
        }

        public Double getRatingChange() {
            return ratingChange;
        }

        public void setRatingChange(Double ratingChange) {
            this.ratingChange = ratingChange;
        }

        public List<ContestRatingPoint> getContestHistory() {
            return contestHistory;
        }

        public void setContestHistory(List<ContestRatingPoint> contestHistory) {
            this.contestHistory = contestHistory;
        }
    }

    public static class ContestRatingPoint {
        private Instant timestamp;
        private Double rating;
        private Integer contestsCount;

        public ContestRatingPoint() {
        }

        public ContestRatingPoint(Instant timestamp, Double rating, Integer contestsCount) {
            this.timestamp = timestamp;
            this.rating = rating;
            this.contestsCount = contestsCount;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public Integer getContestsCount() {
            return contestsCount;
        }

        public void setContestsCount(Integer contestsCount) {
            this.contestsCount = contestsCount;
        }
    }
}
