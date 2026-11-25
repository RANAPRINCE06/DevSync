package com.devsync.coding.intelligence.entity;

import com.devsync.coding.entity.PlatformAccount;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "github_repository_records")
public class GitHubRepositoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_account_id", nullable = false)
    private PlatformAccount platformAccount;

    @Column(name = "repo_name", nullable = false, length = 200)
    private String repoName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "primary_language", length = 100)
    private String primaryLanguage;

    @Column(name = "stars_count", nullable = false)
    private Integer starsCount = 0;

    @Column(name = "forks_count", nullable = false)
    private Integer forksCount = 0;

    @Column(name = "recent_commits_count", nullable = false)
    private Integer recentCommitsCount = 0;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Column(name = "repo_url", nullable = false, length = 500)
    private String repoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public GitHubRepositoryRecord() {
    }

    public GitHubRepositoryRecord(UUID id, PlatformAccount platformAccount, String repoName,
                                  String primaryLanguage, Integer starsCount, Integer forksCount,
                                  Integer recentCommitsCount, Instant lastUpdatedAt,
                                  String repoUrl, Instant createdAt) {
        this.id = id;
        this.platformAccount = platformAccount;
        this.repoName = repoName;
        this.primaryLanguage = primaryLanguage;
        this.starsCount = starsCount != null ? starsCount : 0;
        this.forksCount = forksCount != null ? forksCount : 0;
        this.recentCommitsCount = recentCommitsCount != null ? recentCommitsCount : 0;
        this.lastUpdatedAt = lastUpdatedAt;
        this.repoUrl = repoUrl;
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

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public Integer getStarsCount() {
        return starsCount;
    }

    public void setStarsCount(Integer starsCount) {
        this.starsCount = starsCount;
    }

    public Integer getForksCount() {
        return forksCount;
    }

    public void setForksCount(Integer forksCount) {
        this.forksCount = forksCount;
    }

    public Integer getRecentCommitsCount() {
        return recentCommitsCount;
    }

    public void setRecentCommitsCount(Integer recentCommitsCount) {
        this.recentCommitsCount = recentCommitsCount;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
