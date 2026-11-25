package com.devsync.coding.intelligence.dto;

import java.time.Instant;
import java.util.UUID;

public class GitHubRepositoryResponse {
    private UUID id;
    private String repoName;
    private String primaryLanguage;
    private int starsCount;
    private int forksCount;
    private int recentCommitsCount;
    private Instant lastUpdatedAt;
    private String repoUrl;
    private String description;

    public GitHubRepositoryResponse() {}

    public GitHubRepositoryResponse(UUID id, String repoName, String primaryLanguage,
                                    int starsCount, int forksCount, int recentCommitsCount,
                                    Instant lastUpdatedAt, String repoUrl, String description) {
        this.id = id;
        this.repoName = repoName;
        this.primaryLanguage = primaryLanguage;
        this.starsCount = starsCount;
        this.forksCount = forksCount;
        this.recentCommitsCount = recentCommitsCount;
        this.lastUpdatedAt = lastUpdatedAt;
        this.repoUrl = repoUrl;
        this.description = description;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getPrimaryLanguage() { return primaryLanguage; }
    public void setPrimaryLanguage(String primaryLanguage) { this.primaryLanguage = primaryLanguage; }
    public int getStarsCount() { return starsCount; }
    public void setStarsCount(int starsCount) { this.starsCount = starsCount; }
    public int getForksCount() { return forksCount; }
    public void setForksCount(int forksCount) { this.forksCount = forksCount; }
    public int getRecentCommitsCount() { return recentCommitsCount; }
    public void setRecentCommitsCount(int recentCommitsCount) { this.recentCommitsCount = recentCommitsCount; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
