package com.devsync.coding.intelligence.entity;

import com.devsync.coding.entity.PlatformAccount;
import com.devsync.coding.enums.CodingPlatform;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "platform_contest_records")
public class PlatformContestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_account_id", nullable = false)
    private PlatformAccount platformAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private CodingPlatform platform;

    @Column(name = "contest_id", nullable = false, length = 150)
    private String contestId;

    @Column(name = "contest_name", nullable = false, length = 250)
    private String contestName;

    @Column(name = "contest_date", nullable = false)
    private Instant contestDate;

    @Column(name = "contest_rank")
    private Integer contestRank;

    @Column(name = "participants_count")
    private Integer participantsCount;

    @Column(name = "previous_rating")
    private Double previousRating;

    @Column(name = "rating_change")
    private Double ratingChange;

    @Column(name = "new_rating", nullable = false)
    private Double newRating;

    @Column(name = "problems_solved")
    private Integer problemsSolved;

    @Column(name = "performance_score")
    private Double performanceScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PlatformContestRecord() {
    }

    public PlatformContestRecord(UUID id, PlatformAccount platformAccount, CodingPlatform platform,
                                 String contestId, String contestName, Instant contestDate,
                                 Integer contestRank, Integer participantsCount, Double previousRating,
                                 Double ratingChange, Double newRating, Integer problemsSolved,
                                 Double performanceScore, Instant createdAt) {
        this.id = id;
        this.platformAccount = platformAccount;
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
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlatformAccount getPlatformAccount() {
        return platformAccount;
    }

    public void setPlatformAccount(PlatformAccount platformAccount) {
        this.platformAccount = platformAccount;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public Instant getContestDate() {
        return contestDate;
    }

    public void setContestDate(Instant contestDate) {
        this.contestDate = contestDate;
    }

    public Integer getContestRank() {
        return contestRank;
    }

    public void setContestRank(Integer contestRank) {
        this.contestRank = contestRank;
    }

    public Integer getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }

    public Double getPreviousRating() {
        return previousRating;
    }

    public void setPreviousRating(Double previousRating) {
        this.previousRating = previousRating;
    }

    public Double getRatingChange() {
        return ratingChange;
    }

    public void setRatingChange(Double ratingChange) {
        this.ratingChange = ratingChange;
    }

    public Double getNewRating() {
        return newRating;
    }

    public void setNewRating(Double newRating) {
        this.newRating = newRating;
    }

    public Integer getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(Integer problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public Double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(Double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
