package com.devsync.coding.analytics.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;
import java.util.List;

public class RatingHistoryResponse {

    private String period;
    private Instant from;
    private Instant to;
    private List<PlatformRatingSeries> platforms;

    public RatingHistoryResponse() {
    }

    public RatingHistoryResponse(String period, Instant from, Instant to, List<PlatformRatingSeries> platforms) {
        this.period = period;
        this.from = from;
        this.to = to;
        this.platforms = platforms;
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

    public List<PlatformRatingSeries> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<PlatformRatingSeries> platforms) {
        this.platforms = platforms;
    }

    public static class PlatformRatingSeries {
        private CodingPlatform platform;
        private String username;
        private Double currentRating;
        private Double maximumRating;
        private Double personalBestRating;
        private Double ratingChange;
        private boolean dataAvailable;
        private List<RatingPoint> history;

        public PlatformRatingSeries() {
        }

        public PlatformRatingSeries(CodingPlatform platform, String username, Double currentRating,
                                   Double maximumRating, Double personalBestRating, Double ratingChange,
                                   boolean dataAvailable, List<RatingPoint> history) {
            this.platform = platform;
            this.username = username;
            this.currentRating = currentRating;
            this.maximumRating = maximumRating;
            this.personalBestRating = personalBestRating;
            this.ratingChange = ratingChange;
            this.dataAvailable = dataAvailable;
            this.history = history;
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

        public Double getPersonalBestRating() {
            return personalBestRating;
        }

        public void setPersonalBestRating(Double personalBestRating) {
            this.personalBestRating = personalBestRating;
        }

        public Double getRatingChange() {
            return ratingChange;
        }

        public void setRatingChange(Double ratingChange) {
            this.ratingChange = ratingChange;
        }

        public boolean isDataAvailable() {
            return dataAvailable;
        }

        public void setDataAvailable(boolean dataAvailable) {
            this.dataAvailable = dataAvailable;
        }

        public List<RatingPoint> getHistory() {
            return history;
        }

        public void setHistory(List<RatingPoint> history) {
            this.history = history;
        }
    }

    public static class RatingPoint {
        private Instant timestamp;
        private Double rating;
        private Long globalRank;

        public RatingPoint() {
        }

        public RatingPoint(Instant timestamp, Double rating, Long globalRank) {
            this.timestamp = timestamp;
            this.rating = rating;
            this.globalRank = globalRank;
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

        public Long getGlobalRank() {
            return globalRank;
        }

        public void setGlobalRank(Long globalRank) {
            this.globalRank = globalRank;
        }
    }
}
