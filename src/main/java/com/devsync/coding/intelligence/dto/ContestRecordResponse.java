package com.devsync.coding.intelligence.dto;

import com.devsync.coding.enums.CodingPlatform;
import java.time.Instant;
import java.util.UUID;

public class ContestRecordResponse {
    private UUID id;
    private CodingPlatform platform;
    private String contestId;
    private String contestName;
    private Instant contestDate;
    private Integer contestRank;
    private Integer participantsCount;
    private Double previousRating;
    private Double ratingChange;
    private Double newRating;
    private Integer problemsSolved;
    private Double performanceScore;

    public ContestRecordResponse() {}

    public ContestRecordResponse(UUID id, CodingPlatform platform, String contestId, String contestName,
                                 Instant contestDate, Integer contestRank, Integer participantsCount,
                                 Double previousRating, Double ratingChange, Double newRating,
                                 Integer problemsSolved, Double performanceScore) {
        this.id = id;
        this.platform = platform;
        this.contestId = contestId;
        this.contestName = contestName;
        this.contestDate = contestDate;
        this.contestRank = contestRank;
        this.participantsCount = participantsCount;
        this.previousRating = previousRating;
        this.ratingChange = ratingChange;
        this.newRating = newRating;
        this.problemsSolved = problemsSolved;
        this.performanceScore = performanceScore;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public CodingPlatform getPlatform() { return platform; }
    public void setPlatform(CodingPlatform platform) { this.platform = platform; }
    public String getContestId() { return contestId; }
    public void setContestId(String contestId) { this.contestId = contestId; }
    public String getContestName() { return contestName; }
    public void setContestName(String contestName) { this.contestName = contestName; }
    public Instant getContestDate() { return contestDate; }
    public void setContestDate(Instant contestDate) { this.contestDate = contestDate; }
    public Integer getContestRank() { return contestRank; }
    public void setContestRank(Integer contestRank) { this.contestRank = contestRank; }
    public Integer getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(Integer participantsCount) { this.participantsCount = participantsCount; }
    public Double getPreviousRating() { return previousRating; }
    public void setPreviousRating(Double previousRating) { this.previousRating = previousRating; }
    public Double getRatingChange() { return ratingChange; }
    public void setRatingChange(Double ratingChange) { this.ratingChange = ratingChange; }
    public Double getNewRating() { return newRating; }
    public void setNewRating(Double newRating) { this.newRating = newRating; }
    public Integer getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(Integer problemsSolved) { this.problemsSolved = problemsSolved; }
    public Double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(Double performanceScore) { this.performanceScore = performanceScore; }
}
