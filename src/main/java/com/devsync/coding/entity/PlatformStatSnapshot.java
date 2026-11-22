package com.devsync.coding.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "platform_stat_snapshots")
public class PlatformStatSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_account_id", nullable = false)
    private PlatformAccount platformAccount;

    @Column(name = "current_rating")
    private Double currentRating;

    @Column(name = "maximum_rating")
    private Double maximumRating;

    @Column(name = "global_rank")
    private Long globalRank;

    @Column(name = "country_rank")
    private Long countryRank;

    @Column(name = "problems_solved")
    private Integer problemsSolved;

    @Column(name = "easy_solved")
    private Integer easySolved;

    @Column(name = "medium_solved")
    private Integer mediumSolved;

    @Column(name = "hard_solved")
    private Integer hardSolved;

    @Column(name = "contests_participated")
    private Integer contestsParticipated;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_stats")
    private Map<String, Object> additionalStats;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PlatformStatSnapshot() {
    }

    public PlatformStatSnapshot(UUID id, PlatformAccount platformAccount, Double currentRating, Double maximumRating,
                                Long globalRank, Long countryRank, Integer problemsSolved, Integer easySolved,
                                Integer mediumSolved, Integer hardSolved, Integer contestsParticipated,
                                Map<String, Object> additionalStats, Instant snapshotAt, Instant createdAt) {
        this.id = id;
        this.platformAccount = platformAccount;
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
        this.snapshotAt = snapshotAt != null ? snapshotAt : Instant.now();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.snapshotAt == null) {
            this.snapshotAt = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private PlatformAccount platformAccount;
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
        private Instant createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder platformAccount(PlatformAccount platformAccount) {
            this.platformAccount = platformAccount;
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

        public Builder additionalStats(Map<String, Object> additionalStats) {
            this.additionalStats = additionalStats;
            return this;
        }

        public Builder snapshotAt(Instant snapshotAt) {
            this.snapshotAt = snapshotAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PlatformStatSnapshot build() {
            return new PlatformStatSnapshot(id, platformAccount, currentRating, maximumRating, globalRank,
                    countryRank, problemsSolved, easySolved, mediumSolved, hardSolved, contestsParticipated,
                    additionalStats, snapshotAt, createdAt);
        }
    }
}
