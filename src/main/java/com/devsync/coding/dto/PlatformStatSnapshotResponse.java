package com.devsync.coding.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PlatformStatSnapshotResponse {

    private UUID id;
    private Double currentRating;
    private Double maximumRating;
    private Long globalRank;
    private Long countryRank;
    private Integer problemsSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer contestsParticipated;
    private Map<String, Object> additionalStats;
    private Instant snapshotAt;

    public PlatformStatSnapshotResponse() {
    }

    public PlatformStatSnapshotResponse(UUID id, Double currentRating, Double maximumRating, Long globalRank,
                                        Long countryRank, Integer problemsSolved, Integer easySolved,
                                        Integer mediumSolved, Integer hardSolved, Integer contestsParticipated,
                                        Map<String, Object> additionalStats, Instant snapshotAt) {
        this.id = id;
        this.currentRating = currentRating;
        this.maximumRating = maximumRating;
        this.globalRank = globalRank;
        this.countryRank = countryRank;
        this.problemsSolved = problemsSolved;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.contestsParticipated = contestsParticipated;
        this.additionalStats = additionalStats;
        this.snapshotAt = snapshotAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Map<String, Object> getAdditionalStats() {
        return additionalStats;
    }

    public void setAdditionalStats(Map<String, Object> additionalStats) {
        this.additionalStats = additionalStats;
    }

    public Instant getSnapshotAt() {
        return snapshotAt;
    }

    public void setSnapshotAt(Instant snapshotAt) {
        this.snapshotAt = snapshotAt;
    }
}
