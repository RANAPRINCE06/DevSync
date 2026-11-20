package com.devsync.coding.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.util.List;

public class CodingStatisticsResponse {

    private Integer totalProblemsSolved;
    private Integer totalContestsParticipated;
    private Double highestRating;
    private CodingPlatform highestRatingPlatform;
    private int connectedPlatformsCount;
    private List<PlatformStatSnapshotResponse> platformSnapshots;

    public CodingStatisticsResponse() {
    }

    public CodingStatisticsResponse(Integer totalProblemsSolved, Integer totalContestsParticipated, Double highestRating,
                                    CodingPlatform highestRatingPlatform, int connectedPlatformsCount,
                                    List<PlatformStatSnapshotResponse> platformSnapshots) {
        this.totalProblemsSolved = totalProblemsSolved;
        this.totalContestsParticipated = totalContestsParticipated;
        this.highestRating = highestRating;
        this.highestRatingPlatform = highestRatingPlatform;
        this.connectedPlatformsCount = connectedPlatformsCount;
        this.platformSnapshots = platformSnapshots;
    }

    public Integer getTotalProblemsSolved() {
        return totalProblemsSolved;
    }

    public void setTotalProblemsSolved(Integer totalProblemsSolved) {
        this.totalProblemsSolved = totalProblemsSolved;
    }

    public Integer getTotalContestsParticipated() {
        return totalContestsParticipated;
    }

    public void setTotalContestsParticipated(Integer totalContestsParticipated) {
        this.totalContestsParticipated = totalContestsParticipated;
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

    public int getConnectedPlatformsCount() {
        return connectedPlatformsCount;
    }

    public void setConnectedPlatformsCount(int connectedPlatformsCount) {
        this.connectedPlatformsCount = connectedPlatformsCount;
    }

    public List<PlatformStatSnapshotResponse> getPlatformSnapshots() {
        return platformSnapshots;
    }

    public void setPlatformSnapshots(List<PlatformStatSnapshotResponse> platformSnapshots) {
        this.platformSnapshots = platformSnapshots;
    }
}
