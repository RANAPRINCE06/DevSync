package com.devsync.coding.analytics.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;
import java.util.List;

public class CodingActivityResponse {

    private String period;
    private Instant from;
    private Instant to;
    private boolean dailySubmissionActivitySupported;
    private String coverageNotice;
    private int totalSnapshotsInPeriod;
    private int activeDaysWithSnapshots;
    private Instant firstSnapshotAt;
    private Instant lastSnapshotAt;
    private String syncCadenceDescription;
    private List<SnapshotActivityRecord> snapshotTimeline;

    public CodingActivityResponse() {
    }

    public CodingActivityResponse(String period, Instant from, Instant to,
                                  boolean dailySubmissionActivitySupported, String coverageNotice,
                                  int totalSnapshotsInPeriod, int activeDaysWithSnapshots,
                                  Instant firstSnapshotAt, Instant lastSnapshotAt,
                                  String syncCadenceDescription,
                                  List<SnapshotActivityRecord> snapshotTimeline) {
        this.period = period;
        this.from = from;
        this.to = to;
        this.dailySubmissionActivitySupported = dailySubmissionActivitySupported;
        this.coverageNotice = coverageNotice;
        this.totalSnapshotsInPeriod = totalSnapshotsInPeriod;
        this.activeDaysWithSnapshots = activeDaysWithSnapshots;
        this.firstSnapshotAt = firstSnapshotAt;
        this.lastSnapshotAt = lastSnapshotAt;
        this.syncCadenceDescription = syncCadenceDescription;
        this.snapshotTimeline = snapshotTimeline;
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

    public boolean isDailySubmissionActivitySupported() {
        return dailySubmissionActivitySupported;
    }

    public void setDailySubmissionActivitySupported(boolean dailySubmissionActivitySupported) {
        this.dailySubmissionActivitySupported = dailySubmissionActivitySupported;
    }

    public String getCoverageNotice() {
        return coverageNotice;
    }

    public void setCoverageNotice(String coverageNotice) {
        this.coverageNotice = coverageNotice;
    }

    public int getTotalSnapshotsInPeriod() {
        return totalSnapshotsInPeriod;
    }

    public void setTotalSnapshotsInPeriod(int totalSnapshotsInPeriod) {
        this.totalSnapshotsInPeriod = totalSnapshotsInPeriod;
    }

    public int getActiveDaysWithSnapshots() {
        return activeDaysWithSnapshots;
    }

    public void setActiveDaysWithSnapshots(int activeDaysWithSnapshots) {
        this.activeDaysWithSnapshots = activeDaysWithSnapshots;
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

    public String getSyncCadenceDescription() {
        return syncCadenceDescription;
    }

    public void setSyncCadenceDescription(String syncCadenceDescription) {
        this.syncCadenceDescription = syncCadenceDescription;
    }

    public List<SnapshotActivityRecord> getSnapshotTimeline() {
        return snapshotTimeline;
    }

    public void setSnapshotTimeline(List<SnapshotActivityRecord> snapshotTimeline) {
        this.snapshotTimeline = snapshotTimeline;
    }

    public static class SnapshotActivityRecord {
        private Instant timestamp;
        private CodingPlatform platform;
        private String username;
        private Integer problemsSolved;
        private Double currentRating;
        private Integer solvedChange;
        private Double ratingChange;

        public SnapshotActivityRecord() {
        }

        public SnapshotActivityRecord(Instant timestamp, CodingPlatform platform, String username,
                                      Integer problemsSolved, Double currentRating,
                                      Integer solvedChange, Double ratingChange) {
            this.timestamp = timestamp;
            this.platform = platform;
            this.username = username;
            this.problemsSolved = problemsSolved;
            this.currentRating = currentRating;
            this.solvedChange = solvedChange;
            this.ratingChange = ratingChange;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
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

        public Integer getSolvedChange() {
            return solvedChange;
        }

        public void setSolvedChange(Integer solvedChange) {
            this.solvedChange = solvedChange;
        }

        public Double getRatingChange() {
            return ratingChange;
        }

        public void setRatingChange(Double ratingChange) {
            this.ratingChange = ratingChange;
        }
    }
}
